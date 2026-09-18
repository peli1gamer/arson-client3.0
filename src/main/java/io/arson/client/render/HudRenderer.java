package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.CameraInfoModule;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HudModule;
import io.arson.client.module.InventoryInfoModule;
import io.arson.client.module.PlayerEquipmentModule;
import io.arson.client.module.PlayerInfoModule;
import io.arson.client.module.PlayerMovementInfoModule;
import io.arson.client.module.PlayerPoseInfoModule;
import io.arson.client.module.PlayerVitalsModule;
import io.arson.client.module.RenderInfoModule;
import io.arson.client.module.RenderDisplayInfoModule;
import io.arson.client.module.RenderTargetInfoModule;
import io.arson.client.module.WorldDetailsModule;
import io.arson.client.module.WorldEnvironmentModule;
import io.arson.client.module.WorldInfoModule;
import io.arson.client.module.WorldPositionInfoModule;
import io.arson.client.module.WorldLightInfoModule;
import io.arson.client.notification.NotificationCenter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;

/** Lightweight client HUD renderer driven by module settings. */
public final class HudRenderer {
    private HudRenderer() {}
    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        renderNotifications(graphics, client);
        if (client.player == null || client.level == null) return;
        HudModule hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        if (hud == null || !hud.enabled()) return;
        HudLayoutModule layout = (HudLayoutModule) ArsonClient.getInstance().modules().get("hud-layout");
        PlayerInfoModule playerInfo = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        PlayerVitalsModule playerVitals = (PlayerVitalsModule) ArsonClient.getInstance().modules().get("player-vitals");
        PlayerEquipmentModule equipment = (PlayerEquipmentModule) ArsonClient.getInstance().modules().get("player-equipment");
        PlayerMovementInfoModule movementInfo = (PlayerMovementInfoModule) ArsonClient.getInstance().modules().get("player-movement-info");
        PlayerPoseInfoModule poseInfo = (PlayerPoseInfoModule) ArsonClient.getInstance().modules().get("player-pose-info");
        InventoryInfoModule inventoryInfo = (InventoryInfoModule) ArsonClient.getInstance().modules().get("inventory-info");
        WorldInfoModule worldInfo = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        WorldEnvironmentModule worldEnvironment = (WorldEnvironmentModule) ArsonClient.getInstance().modules().get("world-environment");
        WorldDetailsModule worldDetails = (WorldDetailsModule) ArsonClient.getInstance().modules().get("world-details");
        WorldPositionInfoModule worldPositionInfo = (WorldPositionInfoModule) ArsonClient.getInstance().modules().get("world-position-info");
        WorldLightInfoModule worldLightInfo = (WorldLightInfoModule) ArsonClient.getInstance().modules().get("world-light-info");
        RenderInfoModule renderInfo = (RenderInfoModule) ArsonClient.getInstance().modules().get("render-info");
        CameraInfoModule cameraInfo = (CameraInfoModule) ArsonClient.getInstance().modules().get("camera-info");
        RenderDisplayInfoModule displayInfo = (RenderDisplayInfoModule) ArsonClient.getInstance().modules().get("render-display-info");
        RenderTargetInfoModule targetInfo = (RenderTargetInfoModule) ArsonClient.getInstance().modules().get("render-target-info");
        float globalScale = (float) hud.scale();
        graphics.pose().pushMatrix(); graphics.pose().scale(globalScale, globalScale);
        if (hud.showWatermark()) drawElement(graphics, client, hud, layout, "watermark", hud.x(), hud.y(), new String[]{hud.watermarkText()});
        if (hud.showCoordinates()) drawElement(graphics, client, hud, layout, "coordinates", hud.coordinatesX(), hud.coordinatesY(), new String[]{String.format(java.util.Locale.ROOT, "XYZ %d %d %d", client.player.blockPosition().getX(), client.player.blockPosition().getY(), client.player.blockPosition().getZ())});
        if (hud.showFps()) drawElement(graphics, client, hud, layout, "fps", hud.fpsX(), hud.fpsY(), new String[]{"FPS " + client.getFps()});
        if (hud.showPlayerInfo() && playerInfo != null && playerInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (playerInfo.showHealth()) rows.add(String.format(java.util.Locale.ROOT, "Health %.1f/%.1f", client.player.getHealth(), client.player.getMaxHealth()));
            if (playerInfo.showHunger()) rows.add("Food " + client.player.getFoodData().getFoodLevel());
            if (playerInfo.showArmor()) rows.add(armor(client));
            if (playerInfo.showHeldItem()) { ItemStack stack = client.player.getMainHandItem(); rows.add(stack.isEmpty() ? "Held Hand" : "Held " + stack.getHoverName().getString()); }
            if (playerInfo.showInventory() && inventoryInfo != null && inventoryInfo.enabled()) rows.add(inventoryInfo.formatted() + "  Slot " + inventoryInfo.selectedSlot());
            if (playerVitals != null && playerVitals.enabled()) {
                rows.add(String.format(java.util.Locale.ROOT, "Saturation %.1f  Armor %d", playerVitals.saturation(), playerVitals.armor()));
                rows.add(playerVitals.formattedExperience()); rows.add(playerVitals.formattedAir());
                rows.add("State " + (playerVitals.sprinting() ? "Sprinting" : "Walking") + (playerVitals.sneaking() ? "  Sneaking" : ""));
            }
            if (equipment != null && equipment.enabled()) rows.add(equipment.formatted());
            if (movementInfo != null && movementInfo.enabled()) rows.add(movementInfo.formatted());
            if (poseInfo != null && poseInfo.enabled()) rows.add(poseInfo.formatted());
            drawElement(graphics, client, hud, layout, "player-info", hud.playerInfoX(), hud.playerInfoY(), rows.toArray(String[]::new));
        }
        if (hud.showWorldInfo() && worldInfo != null && worldInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (worldInfo.showTime()) {
                long dayTime = Math.floorMod(client.level.getDayTime(), 24000L); long hours = (dayTime / 1000L + 6L) % 24L; long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
                if (minutes == 60) { minutes = 0; hours = (hours + 1) % 24; }
                rows.add(String.format(java.util.Locale.ROOT, "Time %02d:%02d", hours, minutes));
            }
            if (worldInfo.showDimension()) rows.add("Dimension " + client.level.dimension().identifier());
            if (worldInfo.showWeather()) rows.add("Weather " + (client.level.isThundering() ? "Thunder" : client.level.isRaining() ? "Rain" : "Clear"));
            if (worldEnvironment != null && worldEnvironment.enabled()) {
                rows.add("Biome " + worldEnvironment.biome());
                if (worldEnvironment.showWeather()) rows.add("Local Weather " + worldEnvironment.weather());
                if (worldEnvironment.showDifficulty()) rows.add("Difficulty " + worldEnvironment.difficulty());
                if (worldEnvironment.showChunk()) rows.add("Chunk " + worldEnvironment.chunkX() + ", " + worldEnvironment.chunkZ());
                if (worldEnvironment.showLight()) rows.add("Block Light " + worldEnvironment.blockLight());
            }
            if (worldDetails != null && worldDetails.enabled()) rows.add(worldDetails.formatted());
            if (worldPositionInfo != null && worldPositionInfo.enabled()) rows.add(worldPositionInfo.formatted());
            if (renderInfo != null && renderInfo.enabled()) rows.add(renderInfo.formatted());
            if (cameraInfo != null && cameraInfo.enabled()) rows.add(cameraInfo.formatted());
            if (displayInfo != null && displayInfo.enabled()) rows.add(displayInfo.formatted());
            if (worldLightInfo != null && worldLightInfo.enabled()) rows.add(worldLightInfo.formatted());
            if (targetInfo != null && targetInfo.enabled()) rows.add(targetInfo.formatted());
            drawElement(graphics, client, hud, layout, "world-info", hud.worldInfoX(), hud.worldInfoY(), rows.toArray(String[]::new));
        }
        graphics.pose().popMatrix();
    }
    private static void renderNotifications(GuiGraphics graphics, Minecraft client) {
        var list = NotificationCenter.active(System.currentTimeMillis()); int screenW = client.getWindow().getGuiScaledWidth(), screenH = client.getWindow().getGuiScaledHeight(); int index = 0;
        for (NotificationCenter.Notification n : list) { int width = Math.max(180, Math.max(client.font.width(n.title()), client.font.width(n.message())) + 24), height = 40; float p = n.progress(System.currentTimeMillis()), eased = p * p * (3f - 2f * p); int slide = Math.round((1f - eased) * 18f); boolean left = NotificationCenter.position() == NotificationCenter.Position.TOP_LEFT || NotificationCenter.position() == NotificationCenter.Position.BOTTOM_LEFT; boolean bottom = NotificationCenter.position() == NotificationCenter.Position.BOTTOM_LEFT || NotificationCenter.position() == NotificationCenter.Position.BOTTOM_RIGHT; int x = left ? 10 : screenW - width - 10, y = bottom ? screenH - 10 - height - index * 45 : 10 + index * 45; x += left ? -slide : slide; int outline = switch (n.priority()) { case HIGH -> 0xFFFF5555; case LOW -> 0xFF777777; case NORMAL -> 0xFF5555AA; }; graphics.fill(x, y, x + width, y + height, 0xE0181820); graphics.renderOutline(x, y, width, height, outline); graphics.drawString(client.font, n.title(), x + 8, y + 6, 0xFFFFFFFF); graphics.drawString(client.font, n.message(), x + 8, y + 20, 0xFFD0D0D0); graphics.fill(x + 1, y + height - 3, x + 1 + Math.round((width - 2) * eased), y + height - 1, outline); index++; }
    }
    private static void drawElement(GuiGraphics graphics, Minecraft client, HudModule hud, HudLayoutModule layout, String element, int fallbackX, int fallbackY, String[] rows) {
        if (rows.length == 0) return; double elementScale = hud.elementScale(element); int maxWidth = 0; for (String row : rows) maxWidth = Math.max(maxWidth, client.font.width(row)); int line = hud.lineSpacing(); double x = fallbackX, y = fallbackY;
        if (layout != null) { double logicalViewportW = client.getWindow().getGuiScaledWidth() / Math.max(0.001, hud.scale()), logicalViewportH = client.getWindow().getGuiScaledHeight() / Math.max(0.001, hud.scale()); double[] resolved = layout.resolve(element, logicalViewportW, logicalViewportH, maxWidth * elementScale, rows.length * line * elementScale); x = resolved[0]; y = resolved[1]; }
        graphics.pose().pushMatrix(); graphics.pose().translate((float)x, (float)y); graphics.pose().scale((float) elementScale, (float) elementScale); int padding = hud.padding(); String alignment = hud.elementAlignment(element); int left = alignedX(0, maxWidth, alignment);
        if (hud.showBackground() && hud.elementBackground(element)) graphics.fill(left - padding, -padding, left + maxWidth + padding, rows.length * line + padding - 1, hud.elementBackgroundColor());
        for (int i = 0; i < rows.length; i++) { int rowWidth = client.font.width(rows[i]); int rowX = alignedX(0, rowWidth, alignment); graphics.drawString(client.font, rows[i], rowX, i * line, hud.elementColor(element), hud.showShadow()); }
        graphics.pose().popMatrix();
    }
    private static int alignedX(int x, int width, String alignment) { return switch (alignment == null ? "left" : alignment.trim().toLowerCase(java.util.Locale.ROOT)) { case "center" -> x - width / 2; case "right" -> x - width; default -> x; }; }
    private static String armor(Minecraft client) { int equipped = 0, durabilityTotal = 0, durabilityMax = 0; EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}; for (EquipmentSlot slot : slots) { ItemStack stack = client.player.getItemBySlot(slot); if (!stack.isEmpty()) { equipped++; if (stack.isDamageableItem()) { durabilityTotal += stack.getMaxDamage() - stack.getDamageValue(); durabilityMax += stack.getMaxDamage(); } } } return durabilityMax > 0 ? String.format(java.util.Locale.ROOT, "Armor %d/4  Durability %d%%", equipped, Math.round(durabilityTotal * 100.0f / durabilityMax)) : "Armor " + equipped + "/4"; }
}
