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
        int drawX = 6;
        int drawY = 6;
        int line = 11;
        int y = drawY;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        if (hud.showWatermark()) {
            graphics.drawString(client.font, "Arson V3", drawX, y, 0xFFFFFFFF, true);
            y += line;
        }

        if (hud.showCoordinates()) {
            String text = String.format(java.util.Locale.ROOT, "XYZ %d %d %d",
                    client.player.blockPosition().getX(),
                    client.player.blockPosition().getY(),
                    client.player.blockPosition().getZ());
            graphics.drawString(client.font, text, drawX, y, 0xFFD0D0D0, true);
            y += line;
        }

        if (hud.showFps()) {
            graphics.drawString(client.font, "FPS " + client.getFps(), drawX, y, 0xFFD0D0D0, true);
            y += line;
        }

        if (playerInfo != null && playerInfo.enabled()) {
            if (playerInfo.showHealth()) {
                String health = String.format(java.util.Locale.ROOT, "Health %.1f/%.1f",
                        client.player.getHealth(), client.player.getMaxHealth());
                graphics.drawString(client.font, health, drawX, y, 0xFFD0D0D0, true);
                y += line;
            }

            if (playerInfo.showHunger()) {
                graphics.drawString(client.font, "Food " + client.player.getFoodData().getFoodLevel(),
                        drawX, y, 0xFFD0D0D0, true);
                y += line;
            }

            if (playerInfo.showArmor()) {
                int equipped = 0;
                int durabilityTotal = 0;
                int durabilityMax = 0;
                for (ItemStack stack : client.player.getArmorSlots()) {
                    if (!stack.isEmpty()) {
                        equipped++;
                        if (stack.isDamageableItem()) {
                            durabilityTotal += stack.getMaxDamage() - stack.getDamageValue();
                            durabilityMax += stack.getMaxDamage();
                        }
                    }
                }
                String armor = durabilityMax > 0
                        ? String.format(java.util.Locale.ROOT, "Armor %d/4  Durability %d%%", equipped,
                        Math.round((durabilityTotal * 100.0f) / durabilityMax))
                        : "Armor " + equipped + "/4";
                graphics.drawString(client.font, armor, drawX, y, 0xFFD0D0D0, true);
                y += line;
            }

            if (playerInfo.showHeldItem()) {
                ItemStack stack = client.player.getMainHandItem();
                String held = stack.isEmpty() ? "Held Hand" : "Held " + stack.getHoverName().getString();
                graphics.drawString(client.font, held, drawX, y, 0xFFD0D0D0, true);
                y += line;
            }
        }

        if (worldInfo != null && worldInfo.enabled()) {
            if (worldInfo.showTime()) {
                long dayTime = Math.floorMod(client.level.getDayTime(), 24000L);
                long hours = (dayTime / 1000L + 6L) % 24L;
                long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
                if (minutes == 60L) {
                    minutes = 0L;
                    hours = (hours + 1L) % 24L;
                }
                graphics.drawString(client.font,
                        String.format(java.util.Locale.ROOT, "Time %02d:%02d", hours, minutes),
                        drawX, y, 0xFFD0D0D0, true);
                y += line;
            }

            if (worldInfo.showDimension()) {
                String dimension = client.level.dimension().location().toString();
                graphics.drawString(client.font, "Dimension " + dimension, drawX, y, 0xFFD0D0D0, true);
                y += line;
            }

            if (worldInfo.showWeather()) {
                String weather = client.level.isThundering() ? "Thunder" : client.level.isRaining() ? "Rain" : "Clear";
                graphics.drawString(client.font, "Weather " + weather, drawX, y, 0xFFD0D0D0, true);
            }
        }

        graphics.pose().popMatrix();
    }
}
