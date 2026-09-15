package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.HudModule;
import io.arson.client.module.PlayerInfoModule;
import io.arson.client.module.WorldInfoModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Lightweight client HUD renderer driven by module settings. */
public final class HudRenderer {
    private HudRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        HudModule hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        if (hud == null || !hud.enabled()) return;

        PlayerInfoModule playerInfo = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        WorldInfoModule worldInfo = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        float scale = (float) hud.scale();

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        if (hud.showWatermark()) drawElement(graphics, client, hud, "watermark", hud.x(), hud.y(), new String[]{hud.watermarkText()}, hud.textColor(), hud.watermarkAlign());
        if (hud.showCoordinates()) drawElement(graphics, client, hud, "coordinates", hud.coordinatesX(), hud.coordinatesY(), new String[]{String.format(java.util.Locale.ROOT, "XYZ %d %d %d",
                client.player.blockPosition().getX(), client.player.blockPosition().getY(), client.player.blockPosition().getZ())}, hud.secondaryColor(), hud.coordinatesAlign());
        if (hud.showFps()) drawElement(graphics, client, hud, "fps", hud.fpsX(), hud.fpsY(), new String[]{"FPS " + client.getFps()}, hud.secondaryColor(), hud.fpsAlign());

        if (hud.showPlayerInfo() && playerInfo != null && playerInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (playerInfo.showHealth()) rows.add(String.format(java.util.Locale.ROOT, "Health %.1f/%.1f", client.player.getHealth(), client.player.getMaxHealth()));
            if (playerInfo.showHunger()) rows.add("Food " + client.player.getFoodData().getFoodLevel());
            if (playerInfo.showArmor()) rows.add(armor(client));
            if (playerInfo.showHeldItem()) {
                ItemStack stack = client.player.getMainHandItem();
                rows.add(stack.isEmpty() ? "Held Hand" : "Held " + stack.getHoverName().getString());
            }
            drawElement(graphics, client, hud, "player-info", hud.playerInfoX(), hud.playerInfoY(), rows.toArray(String[]::new), hud.secondaryColor(), hud.playerInfoAlign());
        }

        if (hud.showWorldInfo() && worldInfo != null && worldInfo.enabled()) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            if (worldInfo.showTime()) {
                long dayTime = Math.floorMod(client.level.getDayTime(), 24000L);
                long hours = (dayTime / 1000L + 6L) % 24L;
                long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
                if (minutes == 60) { minutes = 0; hours = (hours + 1) % 24; }
                rows.add(String.format(java.util.Locale.ROOT, "Time %02d:%02d", hours, minutes));
            }
            if (worldInfo.showDimension()) rows.add("Dimension " + client.level.dimension().location());
            if (worldInfo.showWeather()) rows.add("Weather " + (client.level.isThundering() ? "Thunder" : client.level.isRaining() ? "Rain" : "Clear"));
            drawElement(graphics, client, hud, "world-info", hud.worldInfoX(), hud.worldInfoY(), rows.toArray(String[]::new), hud.secondaryColor(), hud.worldInfoAlign());
        }
        graphics.pose().popMatrix();
    }

    private static void drawElement(GuiGraphics graphics, Minecraft client, HudModule hud, String element,
                                    int x, int y, String[] rows, int color, String alignment) {
        if (rows.length == 0) return;
        int maxWidth = 0;
        for (String row : rows) maxWidth = Math.max(maxWidth, client.font.width(row));
        int padding = hud.padding();
        int line = hud.lineSpacing();
        int drawX = alignedX(x, maxWidth, alignment);
        if (hud.showBackground()) {
            graphics.fill(drawX - padding, y - padding, drawX + maxWidth + padding,
                    y + rows.length * line + padding - 1, hud.backgroundColor());
        }
        for (int i = 0; i < rows.length; i++) {
            int rowWidth = client.font.width(rows[i]);
            int rowX = alignedX(x, rowWidth, alignment);
            int rowColor = i == 0 && "watermark".equals(element) ? hud.textColor() : color;
            graphics.drawString(client.font, rows[i], rowX, y + i * line, rowColor, hud.showShadow());
        }
    }

    private static int alignedX(int x, int width, String alignment) {
        return switch (alignment == null ? "left" : alignment.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "center" -> x - width / 2;
            case "right" -> x - width;
            default -> x;
        };
    }

    private static String armor(Minecraft client) {
        int equipped = 0, durabilityTotal = 0, durabilityMax = 0;
        for (ItemStack stack : client.player.getArmorSlots()) {
            if (!stack.isEmpty()) {
                equipped++;
                if (stack.isDamageableItem()) {
                    durabilityTotal += stack.getMaxDamage() - stack.getDamageValue();
                    durabilityMax += stack.getMaxDamage();
                }
            }
        }
        return durabilityMax > 0
                ? String.format(java.util.Locale.ROOT, "Armor %d/4  Durability %d%%", equipped, Math.round(durabilityTotal * 100.0f / durabilityMax))
                : "Armor " + equipped + "/4";
    }
}
