package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.CombatInfoModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Renders nearby combat target and weapon information without automating combat. */
public final class CombatInfoRenderer {
    private CombatInfoRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        CombatInfoModule module = (CombatInfoModule) ArsonClient.getInstance().modules().get("combat-info");
        if (module == null || !module.enabled()) return;

        LivingEntity target = findTarget(client, module.range());
        ItemStack held = client.player.getMainHandItem();
        String weapon = held.isEmpty() ? "Hand" : held.getHoverName().getString();
        String durability = module.showDurability() && held.isDamageableItem()
                ? "Durability " + (held.getMaxDamage() - held.getDamageValue()) + "/" + held.getMaxDamage()
                : null;
        String cooldown = module.showAttackCooldown()
                ? "Cooldown " + Math.round(client.player.getAttackStrengthScale(0.0f) * 100.0f) + "%"
                : null;

        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        if (module.showTarget() && target != null) rows.add(target.getDisplayName().getString());
        if (target != null && module.showHealth()) rows.add("HP " + format(target.getHealth()) + "/" + format(target.getMaxHealth()));
        if (target != null && module.showDistance()) rows.add("Distance " + format((float) client.player.distanceTo(target)) + "m");
        if (module.showHeldItem()) rows.add("Held " + weapon);
        if (durability != null) rows.add(durability);
        if (cooldown != null) rows.add(cooldown);
        if (rows.isEmpty()) return;

        float scale = (float) module.infoScale();
        int x = module.x();
        int y = module.y();
        int line = 11;
        int width = 0;
        for (String row : rows) width = Math.max(width, client.font.width(row));

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        if (module.background()) {
            graphics.fill(x - 4, y - 4, x + width + 4, y + rows.size() * line + 2, module.backgroundColor());
        }
        for (int i = 0; i < rows.size(); i++) {
            int color = (i == 1 && target != null && module.showHealth()) ? module.healthColor() : module.textColor();
            graphics.drawString(client.font, rows.get(i), x, y + i * line, color, true);
        }
        graphics.pose().popMatrix();
    }

    private static LivingEntity findTarget(Minecraft client, double range) {
        double maxDistance = range * range;
        LivingEntity best = null;
        double bestDistance = maxDistance;
        for (LivingEntity entity : client.level.getEntitiesOfClass(LivingEntity.class,
                client.player.getBoundingBox().inflate(range),
                entity -> entity != client.player && entity.isAlive() && !entity.isSpectator())) {
            double distance = client.player.distanceToSqr(entity);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private static String format(float value) {
        return String.valueOf(Math.round(value * 10.0f) / 10.0f);
    }
}
