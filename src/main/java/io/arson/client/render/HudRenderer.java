package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.CameraInfoModule;
import io.arson.client.module.ClientInfoModule;
import io.arson.client.module.ArrayListModule;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ClientPerformanceInfoModule;
import io.arson.client.module.HotkeysModule;
import io.arson.client.module.Module;
import io.arson.client.module.PerformanceModule;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HudModule;
import io.arson.client.module.InventoryInfoModule;
import io.arson.client.module.PlayerEquipmentModule;
import io.arson.client.module.PlayerExperienceInfoModule;
import io.arson.client.module.PlayerEffectInfoModule;
import io.arson.client.module.PlayerInfoModule;
import io.arson.client.module.PlayerMovementInfoModule;
import io.arson.client.module.PlayerPoseInfoModule;
import io.arson.client.module.PlayerVitalsModule;
import io.arson.client.module.PlayerVelocityInfoModule;
import io.arson.client.module.PlayerInputInfoModule;
import io.arson.client.module.PlayerInteractionInfoModule;
import io.arson.client.module.WorldEntityCountInfoModule;
import io.arson.client.module.RenderGuiInfoModule;
import io.arson.client.module.PlayerAirInfoModule;
import io.arson.client.module.WorldWeatherInfoModule;
import io.arson.client.module.RenderFrameInfoModule;
import io.arson.client.module.RenderInfoModule;
import io.arson.client.module.RenderTargetInfoModule;
import io.arson.client.module.RenderDisplayInfoModule;
import io.arson.client.module.RenderViewportInfoModule;
import io.arson.client.module.RenderCameraInfoModule;
import io.arson.client.module.WorldDetailsModule;
import io.arson.client.module.WorldEnvironmentModule;
import io.arson.client.module.WorldInfoModule;
import io.arson.client.module.WorldPositionInfoModule;
import io.arson.client.module.WorldHeightInfoModule;
import io.arson.client.module.WorldLightInfoModule;
import io.arson.client.module.WorldBorderInfoModule;
import io.arson.client.module.WorldSpawnInfoModule;
import io.arson.client.module.RenderResolutionInfoModule;
import io.arson.client.module.WorldMoonInfoModule;
import io.arson.client.module.WorldChunkInfoModule;
import io.arson.client.notification.NotificationCenter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** Lightweight client HUD renderer driven by module settings. */
public final class HudRenderer {
    private static int lastLayoutWidth=-1,lastLayoutHeight=-1,lastArrayWidth=-1,lastArrayHeight=-1;
    private static int lastArrayX=Integer.MIN_VALUE,lastArrayY=Integer.MIN_VALUE,lastLayoutSignature;
    private static float lastLayoutScale=Float.NaN;
    private HudRenderer() {}
    public static void render(DrawContext graphics, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        renderNotifications(graphics, client);
        if (client.player == null || client.world == null) return;
        HudModule hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        if (hud == null || !hud.enabled()) return;
        HudLayoutModule layout = (HudLayoutModule) ArsonClient.getInstance().modules().get("hud-layout");
        ArrayListModule arrayList = (ArrayListModule) ArsonClient.getInstance().modules().get("array-list");
        PlayerInfoModule playerInfo = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        PlayerVitalsModule playerVitals = (PlayerVitalsModule) ArsonClient.getInstance().modules().get("player-vitals");
        PlayerVelocityInfoModule velocityInfo = (PlayerVelocityInfoModule) ArsonClient.getInstance().modules().get("player-velocity-info");
        PlayerInputInfoModule inputInfo = (PlayerInputInfoModule) ArsonClient.getInstance().modules().get("player-input-info");
        PlayerInteractionInfoModule interactionInfo = (PlayerInteractionInfoModule) ArsonClient.getInstance().modules().get("player-interaction-info");
        WorldEntityCountInfoModule entityCountInfo = (WorldEntityCountInfoModule) ArsonClient.getInstance().modules().get("world-entity-count-info");
        RenderGuiInfoModule guiInfo = (RenderGuiInfoModule) ArsonClient.getInstance().modules().get("render-gui-info");
        PlayerEquipmentModule equipment = (PlayerEquipmentModule) ArsonClient.getInstance().modules().get("player-equipment");
        PlayerExperienceInfoModule experienceInfo = (PlayerExperienceInfoModule) ArsonClient.getInstance().modules().get("player-experience-info");
        PlayerEffectInfoModule effectInfo = (PlayerEffectInfoModule) ArsonClient.getInstance().modules().get("player-effect-info");
        PlayerMovementInfoModule movementInfo = (PlayerMovementInfoModule) ArsonClient.getInstance().modules().get("player-movement-info");
        PlayerPoseInfoModule poseInfo = (PlayerPoseInfoModule) ArsonClient.getInstance().modules().get("player-pose-info");
        InventoryInfoModule inventoryInfo = (InventoryInfoModule) ArsonClient.getInstance().modules().get("inventory-info");
        WorldInfoModule worldInfo = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        WorldEnvironmentModule worldEnvironment = (WorldEnvironmentModule) ArsonClient.getInstance().modules().get("world-environment");
        WorldDetailsModule worldDetails = (WorldDetailsModule) ArsonClient.getInstance().modules().get("world-details");
        WorldPositionInfoModule worldPositionInfo = (WorldPositionInfoModule) ArsonClient.getInstance().modules().get("world-position-info");
        WorldHeightInfoModule worldHeightInfo = (WorldHeightInfoModule) ArsonClient.getInstance().modules().get("world-height-info");
        WorldLightInfoModule worldLightInfo = (WorldLightInfoModule) ArsonClient.getInstance().modules().get("world-light-info");
        WorldBorderInfoModule borderInfo = (WorldBorderInfoModule) ArsonClient.getInstance().modules().get("world-border-info");
        WorldSpawnInfoModule spawnInfo = (WorldSpawnInfoModule) ArsonClient.getInstance().modules().get("world-spawn-info");
        WorldMoonInfoModule moonInfo = (WorldMoonInfoModule) ArsonClient.getInstance().modules().get("world-moon-info");
        WorldChunkInfoModule chunkInfo = (WorldChunkInfoModule) ArsonClient.getInstance().modules().get("world-chunk-info");
        RenderInfoModule renderInfo = (RenderInfoModule) ArsonClient.getInstance().modules().get("render-info");
        RenderTargetInfoModule targetInfo = (RenderTargetInfoModule) ArsonClient.getInstance().modules().get("render-target-info");
        CameraInfoModule cameraInfo = (CameraInfoModule) ArsonClient.getInstance().modules().get("camera-info");
        RenderDisplayInfoModule displayInfo = (RenderDisplayInfoModule) ArsonClient.getInstance().modules().get("render-display-info");
        RenderViewportInfoModule viewportInfo = (RenderViewportInfoModule) ArsonClient.getInstance().modules().get("render-viewport-info");
        PlayerAirInfoModule airInfo = (PlayerAirInfoModule) ArsonClient.getInstance().modules().get("player-air-info");
        WorldWeatherInfoModule weatherInfo = (WorldWeatherInfoModule) ArsonClient.getInstance().modules().get("world-weather-info");
        RenderFrameInfoModule frameInfo = (RenderFrameInfoModule) ArsonClient.getInstance().modules().get("render-frame-info");
        RenderCameraInfoModule cameraInfo2 = (RenderCameraInfoModule) ArsonClient.getInstance().modules().get("render-camera-info");
        ClientPerformanceInfoModule performanceInfo = (ClientPerformanceInfoModule) ArsonClient.getInstance().modules().get("client-performance-info");
        PerformanceModule performance = (PerformanceModule) ArsonClient.getInstance().modules().get("performance");
        HotkeysModule hotkeys = (HotkeysModule) ArsonClient.getInstance().modules().get("hotkeys");
        ClientInfoModule clientInfo = (ClientInfoModule) ArsonClient.getInstance().modules().get("client-info");
        RenderResolutionInfoModule resolutionInfo = (RenderResolutionInfoModule) ArsonClient.getInstance().modules().get("render-resolution-info");
        float globalScale = (float) hud.scale();
        if(layout != null){
            int viewportWidth=client.getWindow().getScaledWidth();
            int viewportHeight=client.getWindow().getScaledHeight();
            int[] arraySize=estimateArrayListSize(client,arrayList);
            layout.updateElementSize("array-list", arraySize[0], arraySize[1]);
            int arrayX=arrayList==null?6:arrayList.x();
            int arrayY=arrayList==null?6:arrayList.y();
            java.util.Set<String> activeHudElements = new java.util.HashSet<>();
            for (String element : new String[]{"watermark", "coordinates", "fps", "player-info", "world-info"}) {
                if (hud.elementVisible(element)) activeHudElements.add(element);
            }
            if (playerInfo == null || !playerInfo.enabled()) activeHudElements.remove("player-info");
            if (worldInfo == null || !worldInfo.enabled()) activeHudElements.remove("world-info");
            int signature=31 * layoutSignature(hud,layout,arrayList) + activeHudElements.hashCode();
            boolean needsRepair=viewportWidth!=lastLayoutWidth||viewportHeight!=lastLayoutHeight
                    ||Float.compare(globalScale,lastLayoutScale)!=0||arraySize[0]!=lastArrayWidth
                    ||arraySize[1]!=lastArrayHeight||arrayX!=lastArrayX||arrayY!=lastArrayY
                    ||signature!=lastLayoutSignature;
            if(needsRepair){
                layout.setArrayListPreferredPosition(arrayX,arrayY);
                if(layout.repairNoOverlap(viewportWidth,viewportHeight,globalScale,arraySize[0],arraySize[1],arrayList,hud,activeHudElements))
                    ConfigManager.save(client,ArsonClient.getInstance().modules());
                lastLayoutWidth=viewportWidth;lastLayoutHeight=viewportHeight;lastLayoutScale=globalScale;
                lastArrayWidth=arraySize[0];lastArrayHeight=arraySize[1];
                lastArrayX=arrayList==null?6:arrayList.x();lastArrayY=arrayList==null?6:arrayList.y();
                lastLayoutSignature=layoutSignature(hud,layout,arrayList);
            }
        }
        graphics.getMatrices().pushMatrix(); graphics.getMatrices().scale(globalScale, globalScale);
        if (hud.showWatermark()) drawElement(graphics, client, hud, layout, "watermark", hud.x(), hud.y(), new String[]{hud.watermarkText()});
        if (hud.showCoordinates()) {
            var position = client.player.getBlockPos();
            String current = String.format(java.util.Locale.ROOT, "XYZ %d %d %d", position.getX(), position.getY(), position.getZ());
            String[] coordinateRows = new String[]{current};
            if (hud.showNetherCoordinates()) {
                String dimension = client.world.getRegistryKey().getValue().getPath();
                if ("overworld".equals(dimension)) {
                    HudCoordinates.Position nether = HudCoordinates.fromOverworld(position.getX(), position.getY(), position.getZ());
                    coordinateRows = new String[]{current, String.format(java.util.Locale.ROOT, "Nether %d %d %d", nether.x(), nether.y(), nether.z())};
                } else if ("the_nether".equals(dimension)) {
                    HudCoordinates.Position overworld = HudCoordinates.fromNether(position.getX(), position.getY(), position.getZ());
                    coordinateRows = new String[]{current, String.format(java.util.Locale.ROOT, "Overworld %d %d %d", overworld.x(), overworld.y(), overworld.z())};
                }
            }
            drawElement(graphics, client, hud, layout, "coordinates", hud.coordinatesX(), hud.coordinatesY(), coordinateRows);
        }
        if (hud.showFps()) drawElement(graphics, client, hud, layout, "fps", hud.fpsX(), hud.fpsY(), new String[]{"FPS " + client.getCurrentFps()});
        if (hud.showPlayerInfo() && playerInfo != null && playerInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (playerInfo.showHealth()) rows.add(String.format(java.util.Locale.ROOT, "Health %.1f/%.1f", client.player.getHealth(), client.player.getMaxHealth()));
            if (playerInfo.showHunger()) rows.add("Food " + client.player.getHungerManager().getFoodLevel());
            if (playerInfo.showArmor()) rows.add(armor(client));
            if (playerInfo.showHeldItem()) { ItemStack stack = client.player.getMainHandStack(); rows.add(stack.isEmpty() ? "Held Hand" : "Held " + stack.getName().getString()); }
            if (playerInfo.showInventory() && inventoryInfo != null && inventoryInfo.enabled()) rows.add(inventoryInfo.formatted() + "  Slot " + inventoryInfo.selectedSlot());
            if (playerVitals != null && playerVitals.enabled()) {
                rows.add(String.format(java.util.Locale.ROOT, "Saturation %.1f  Armor %d", playerVitals.saturation(), playerVitals.armor()));
                rows.add(playerVitals.formattedExperience()); rows.add(playerVitals.formattedAir());
                rows.add("State " + (playerVitals.sprinting() ? "Sprinting" : "Walking") + (playerVitals.sneaking() ? "  Sneaking" : ""));
            }
            if (equipment != null && equipment.enabled()) rows.add(equipment.formatted());
            if (experienceInfo != null && experienceInfo.enabled()) rows.add(experienceInfo.formatted());
            if (effectInfo != null && effectInfo.enabled()) rows.add(effectInfo.formatted());
            if (movementInfo != null && movementInfo.enabled()) rows.add(movementInfo.formatted());
            if (poseInfo != null && poseInfo.enabled()) rows.add(poseInfo.formatted());
            if (velocityInfo != null && velocityInfo.enabled()) rows.add(velocityInfo.formatted());
            if (inputInfo != null && inputInfo.enabled()) rows.add(inputInfo.formatted());
            if (interactionInfo != null && interactionInfo.enabled()) rows.add(interactionInfo.formatted());
            if (airInfo != null && airInfo.enabled()) rows.add(airInfo.formatted());
            drawElement(graphics, client, hud, layout, "player-info", hud.playerInfoX(), hud.playerInfoY(), rows.toArray(String[]::new));
        }
        if (hud.showWorldInfo() && worldInfo != null && worldInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (worldInfo.showTime()) {
                long dayTime = Math.floorMod(client.world.getTimeOfDay(), 24000L); long hours = (dayTime / 1000L + 6L) % 24L; long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
                if (minutes == 60) { minutes = 0; hours = (hours + 1) % 24; }
                rows.add(String.format(java.util.Locale.ROOT, "Time %02d:%02d", hours, minutes));
            }
            if (worldInfo.showLocalClock()) rows.add("Clock " + worldInfo.formattedLocalClock());
            if (worldInfo.showDimension()) rows.add("Dimension " + client.world.getRegistryKey().getValue());
            if (worldInfo.showWeather()) rows.add("Weather " + (client.world.isThundering() ? "Thunder" : client.world.isRaining() ? "Rain" : "Clear"));
            if (hud.showPing()) {
                var network = client.getNetworkHandler();
                var localEntry = network == null ? null : network.getPlayerListEntry(client.player.getUuid());
                if (localEntry != null) rows.add("Ping " + Math.max(0, localEntry.getLatency()) + " ms");
            }
            if (worldEnvironment != null && worldEnvironment.enabled()) {
                rows.add("Biome " + worldEnvironment.biome());
                if (worldEnvironment.showWeather()) rows.add("Local Weather " + worldEnvironment.weather());
                if (worldEnvironment.showDifficulty()) rows.add("Difficulty " + worldEnvironment.difficulty());
                if (worldEnvironment.showChunk()) rows.add("Chunk " + worldEnvironment.chunkX() + ", " + worldEnvironment.chunkZ());
                if (worldEnvironment.showLight()) rows.add("Block Light " + worldEnvironment.blockLight());
            }
            if (worldDetails != null && worldDetails.enabled()) rows.add(worldDetails.formatted());
            if (worldPositionInfo != null && worldPositionInfo.enabled()) rows.add(worldPositionInfo.formatted());
            if (worldLightInfo != null && worldLightInfo.enabled()) rows.add(worldLightInfo.formatted());
            if (borderInfo != null && borderInfo.enabled()) rows.add(borderInfo.formatted());
            if (spawnInfo != null && spawnInfo.enabled()) rows.add(spawnInfo.formatted());
            if (moonInfo != null && moonInfo.enabled()) rows.add(moonInfo.formatted());
            if (chunkInfo != null && chunkInfo.enabled()) rows.add(chunkInfo.formatted());
            if (worldHeightInfo != null && worldHeightInfo.enabled()) rows.add(worldHeightInfo.formatted());
            if (renderInfo != null && renderInfo.enabled()) rows.add(renderInfo.formatted());
            if (viewportInfo != null && viewportInfo.enabled()) rows.add(viewportInfo.formatted());
            if (cameraInfo != null && cameraInfo.enabled()) rows.add(cameraInfo.formatted());
            if (cameraInfo2 != null && cameraInfo2.enabled()) rows.add(cameraInfo2.formatted());
            if (targetInfo != null && targetInfo.enabled()) rows.add(targetInfo.formatted());
            if (displayInfo != null && displayInfo.enabled()) rows.add(displayInfo.formatted());
            if (performanceInfo != null && performanceInfo.enabled()) rows.add(performanceInfo.formatted());
            if (clientInfo != null && clientInfo.enabled()) rows.add(clientInfo.formatted());
            if (hotkeys != null && hotkeys.enabled()) {
                rows.add("HOTKEYS");
                var boundModules = hotkeys.boundModules(ArsonClient.getInstance().modules().all());
                if (boundModules.isEmpty()) rows.add("No keys bound");
                else for (Module boundModule : boundModules) {
                    rows.add(boundModule.name() + "  " + boundModule.keybind());
                }
                int hidden = hotkeys.boundModuleCount(ArsonClient.getInstance().modules().all()) - boundModules.size();
                if (hidden > 0) rows.add("+" + hidden + " more");
            }
            if (performance != null && performance.enabled() && performance.showStats()) {
                ChunkFinderScanner scanner = ArsonClient.getInstance().chunkFinderScanner();
                if (scanner != null) rows.add(performance.formattedStats(
                        scanner.lastScanNanos(), scanner.lastProcessedChunks(), scanner.pendingCount()));
            }
            if (resolutionInfo != null && resolutionInfo.enabled()) rows.add(resolutionInfo.formatted());
            if (weatherInfo != null && weatherInfo.enabled()) rows.add(weatherInfo.formatted());
            if (entityCountInfo != null && entityCountInfo.enabled()) rows.add(entityCountInfo.formatted());
            if (guiInfo != null && guiInfo.enabled()) rows.add(guiInfo.formatted());
            if (frameInfo != null && frameInfo.enabled()) rows.add(frameInfo.formatted());
            drawElement(graphics, client, hud, layout, "world-info", hud.worldInfoX(), hud.worldInfoY(), rows.toArray(String[]::new));
        }
        graphics.getMatrices().popMatrix();
    }
    private static int layoutSignature(HudModule hud,HudLayoutModule layout,ArrayListModule arrayList){
        int hash=Float.hashCode((float)hud.scale());
        hash=31*hash+Boolean.hashCode(layout.clamp());
        hash=31*hash+layout.boundsSignature();
        for(String element:new String[]{"watermark","coordinates","fps","player-info","world-info"}){
            hash=31*hash+Boolean.hashCode(hud.elementVisible(element));
            hash=31*hash+Double.hashCode(hud.elementScale(element));
            hash=31*hash+layout.anchor(element).ordinal();
            hash=31*hash+Double.hashCode(layout.offsetX(element));
            hash=31*hash+Double.hashCode(layout.offsetY(element));
        }
        if(arrayList!=null){
            hash=31*hash+Boolean.hashCode(arrayList.enabled());
            hash=31*hash+arrayList.x();hash=31*hash+arrayList.y();
        }
        return hash;
    }

    private static int[] estimateArrayListSize(MinecraftClient client, ArrayListModule module) {
        if(module==null || !module.enabled()) return new int[]{1,1};
        int maxWidth=1;
        int count=0;
        for(var candidate:ArsonClient.getInstance().modules().all()){
            if(candidate.id().equals("array-list")) continue;
            if(candidate.enabled()){
                count++;
                String label=candidate.name()+(module.showCategory()?" ["+candidate.category().displayName()+"]":"");
                maxWidth=Math.max(maxWidth,client.textRenderer.getWidth(label));
            }
        }
        count=Math.min(module.maxModules(),Math.max(1,count));
        return new int[]{Math.max(1,(int)Math.ceil((maxWidth+module.padding()*2)*module.scale())),
                Math.max(1,(int)Math.ceil((count*module.spacing()+module.padding()*2)*module.scale()))};
    }

    private static void renderNotifications(DrawContext graphics, MinecraftClient client) {
        var list = NotificationCenter.active(System.currentTimeMillis()); int screenW = client.getWindow().getScaledWidth(), screenH = client.getWindow().getScaledHeight(); int index = 0;
        for (NotificationCenter.Notification n : list) { int width = Math.max(180, Math.max(client.textRenderer.getWidth(n.title()), client.textRenderer.getWidth(n.message())) + 24), height = 40; float p = n.progress(System.currentTimeMillis()), eased = p * p * (3f - 2f * p); int slide = Math.round((1f - eased) * 18f); boolean left = NotificationCenter.position() == NotificationCenter.Position.TOP_LEFT || NotificationCenter.position() == NotificationCenter.Position.BOTTOM_LEFT; boolean bottom = NotificationCenter.position() == NotificationCenter.Position.BOTTOM_LEFT || NotificationCenter.position() == NotificationCenter.Position.BOTTOM_RIGHT; width = Math.min(width, Math.max(1, screenW - 20)); int x = left ? 10 : screenW - width - 10, y = bottom ? screenH - 10 - height - index * 45 : 10 + index * 45; x += left ? -slide : slide; x = Math.max(0, Math.min(Math.max(0, screenW - width), x)); y = Math.max(0, Math.min(Math.max(0, screenH - height), y)); int outline = switch (n.priority()) { case HIGH -> 0xFFFF5555; case LOW -> 0xFF777777; case NORMAL -> 0xFF5555AA; }; graphics.fill(x, y, x + width, y + height, 0xE0181820); graphics.drawStrokedRectangle(x, y, width, height, outline); graphics.drawTextWithShadow(client.textRenderer, n.title(), x + 8, y + 6, 0xFFFFFFFF); graphics.drawTextWithShadow(client.textRenderer, n.message(), x + 8, y + 20, 0xFFD0D0D0); graphics.fill(x + 1, y + height - 3, x + 1 + Math.round((width - 2) * eased), y + height - 1, outline); index++; }
    }
    private static void drawElement(DrawContext graphics, MinecraftClient client, HudModule hud, HudLayoutModule layout, String element, int fallbackX, int fallbackY, String[] rows) {
        if (rows.length == 0) return;
        if (hud.rowFormat() == HudModule.RowFormat.COMPACT && rows.length > 1) rows = new String[]{String.join("  |  ", rows)};
        double elementScale = hud.elementScale(element); int maxWidth = 0; for (String row : rows) maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(row)); int line = hud.rowFormat() == HudModule.RowFormat.DENSE ? Math.max(8, hud.lineSpacing() - 2) : hud.lineSpacing();
double rawW = Math.max(1, client.getWindow().getScaledWidth() - 12);
double rawH = Math.max(1, client.getWindow().getScaledHeight() - 12);
double fitW = maxWidth == 0 ? elementScale : rawW / Math.max(1.0, maxWidth * hud.scale());
double fitH = rows.length == 0 ? elementScale : rawH / Math.max(1.0, rows.length * line * hud.scale());
elementScale = Math.min(elementScale, Math.max(0.05, Math.min(fitW, fitH)));
        if (layout != null) layout.updateElementSize(element, maxWidth * elementScale, rows.length * line * elementScale);
        double x = fallbackX, y = fallbackY;
        if (layout != null) { double logicalViewportW = client.getWindow().getScaledWidth() / Math.max(0.001, hud.scale()), logicalViewportH = client.getWindow().getScaledHeight() / Math.max(0.001, hud.scale()); double[] resolved = layout.resolve(element, logicalViewportW, logicalViewportH, maxWidth * elementScale, rows.length * line * elementScale); x = resolved[0]; y = resolved[1]; }
        graphics.getMatrices().pushMatrix(); graphics.getMatrices().translate((float)x, (float)y); graphics.getMatrices().scale((float) elementScale, (float) elementScale); int padding = hud.padding(); String alignment = hud.elementAlignment(element); int left = alignedX(0, maxWidth, alignment);
        if (hud.showBackground() && hud.elementBackground(element)) graphics.fill(left - padding, -padding, left + maxWidth + padding, rows.length * line + padding - 1, hud.elementBackgroundColor());
        for (int i = 0; i < rows.length; i++) { int rowWidth = client.textRenderer.getWidth(rows[i]); int rowX = alignedX(0, rowWidth, alignment); graphics.drawText(client.textRenderer, rows[i], rowX, i * line, hud.elementColor(element), hud.showShadow()); }
        graphics.getMatrices().popMatrix();
    }
    private static int alignedX(int x, int width, String alignment) { return switch (alignment == null ? "left" : alignment.trim().toLowerCase(java.util.Locale.ROOT)) { case "center" -> x - width / 2; case "right" -> x - width; default -> x; }; }
    private static String armor(MinecraftClient client) { int equipped = 0, durabilityTotal = 0, durabilityMax = 0; EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}; for (EquipmentSlot slot : slots) { ItemStack stack = client.player.getEquippedStack(slot); if (!stack.isEmpty()) { equipped++; if (stack.isDamageable()) { durabilityTotal += stack.getMaxDamage() - stack.getDamage(); durabilityMax += stack.getMaxDamage(); } } } return durabilityMax > 0 ? String.format(java.util.Locale.ROOT, "Armor %d/4  Durability %d%%", equipped, Math.round(durabilityTotal * 100.0f / durabilityMax)) : "Armor " + equipped + "/4"; }
}
