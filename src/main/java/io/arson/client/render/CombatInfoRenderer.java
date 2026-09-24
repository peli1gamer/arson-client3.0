package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.CombatInfoModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/** Renders nearby combat target and weapon information without automating combat. */
public final class CombatInfoRenderer {
    private CombatInfoRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        CombatInfoModule module = (CombatInfoModule) ArsonClient.getInstance().modules().get("combat-info");
        if (module == null || !module.enabled()) return;

        LivingEntity target = findTarget(client, module);
        ItemStack held = client.player.getMainHandItem();
        String weapon = held.isEmpty() ? "Hand" : held.getHoverName().getString();
        String durability = module.showDurability() && held.isDamageableItem()
                ? "Durability " + (held.getMaxDamage() - held.getDamageValue()) + "/" + held.getMaxDamage()
                : null;
        String cooldown = module.showAttackCooldown()
                ? "Cooldown " + Math.round(client.player.getAttackStrengthScale(0.0f) * 100.0f) + "%"
                : null;

        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        int healthRow = -1;
        if (module.showTarget() && target != null) rows.add(target.getDisplayName().getString());
        if (target != null && module.showHealth()) {
            healthRow = rows.size();
            rows.add("HP " + format(target.getHealth()) + "/" + format(target.getMaxHealth()));
        }
        if (target != null && module.showAbsorption()) {
            rows.add(CombatInfoModule.formatAbsorption(target.getAbsorptionAmount()));
        }
        if (target != null && module.showArmor()) {
            rows.add(CombatInfoModule.formatArmor(target.getArmorValue()));
        }
        if (target != null && module.showTargetEquipment()) {
            rows.addAll(CombatInfoModule.formatTargetEquipment(
                    durabilityPercent(target.getItemBySlot(EquipmentSlot.HEAD)),
                    durabilityPercent(target.getItemBySlot(EquipmentSlot.CHEST)),
                    durabilityPercent(target.getItemBySlot(EquipmentSlot.LEGS)),
                    durabilityPercent(target.getItemBySlot(EquipmentSlot.FEET)),
                    target.getOffhandItem().isEmpty() ? "Empty" : target.getOffhandItem().getHoverName().getString()));
        }
        if (target != null && module.showEffects()) {
            var effects = target.getActiveEffects().stream()
                    .map(effect -> new CombatInfoModule.EffectSnapshot(
                            effect.getEffect().value().getDisplayName().getString(),
                            effect.getAmplifier(), effect.getDuration()))
                    .toList();
            rows.addAll(CombatInfoModule.formatEffects(effects, module.effectLimit()));
        }
        if (target != null && module.showDistance()) rows.add("Distance " + format((float) client.player.distanceTo(target)) + "m");
        if (module.showHeldItem()) rows.add("Held " + weapon);
        if (target != null && (module.showTargetItem() || module.showTargetDurability())) {
            ItemStack targetHeld = target.getMainHandItem();
            if (module.showTargetItem()) {
                rows.add("Target held " + (targetHeld.isEmpty() ? "Empty" : targetHeld.getHoverName().getString()));
            }
            if (module.showTargetDurability() && !targetHeld.isEmpty() && targetHeld.isDamageableItem()) {
                rows.add("Target durability " + (targetHeld.getMaxDamage() - targetHeld.getDamageValue())
                        + "/" + targetHeld.getMaxDamage());
            }
        }
        if (durability != null) rows.add(durability);
        if (cooldown != null) rows.add(cooldown);
        if (rows.isEmpty() && !(target != null && module.healthBar())) return;

        float scale = (float) module.infoScale();
        int x = module.x();
        int y = module.y();
        int line = client.font.lineHeight + module.rowGap();
        int padding = module.padding();
        int width = 0;
        for (String row : rows) width = Math.max(width, client.font.width(row));

        int contentHeight = rows.size() * line;
        boolean drawHealthBar = target != null && module.healthBar();
        int barGap = drawHealthBar && !rows.isEmpty() ? module.rowGap() : 0;
        int barHeight = drawHealthBar ? module.healthBarHeight() : 0;
        int totalHeight = contentHeight + barGap + barHeight;
        int panelWidth = Math.max(width, 48);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);

        if (module.background()) {
            graphics.fill(-padding, -padding, panelWidth + padding, totalHeight + padding,
                    module.backgroundColor());
        }

        for (int i = 0; i < rows.size(); i++) {
            int color = i == healthRow ? module.healthColor() : module.textColor();
            graphics.drawString(client.font, rows.get(i), 0, i * line, color, true);
        }

        if (drawHealthBar) {
            int barTop = contentHeight + barGap;
            if (module.healthBarBackground()) {
                graphics.fill(0, barTop, panelWidth, barTop + barHeight, module.healthBarBackgroundColor());
            }
            CombatInfoModule.HealthBarSegments segments = CombatInfoModule.healthBarSegments(
                    target.getHealth(), target.getMaxHealth(), target.getAbsorptionAmount());
            int healthEnd = Math.round(panelWidth * segments.health());
            int totalEnd = Math.round(panelWidth * (segments.health() + segments.absorption()));
            if (healthEnd > 0) graphics.fill(0, barTop, healthEnd, barTop + barHeight, module.healthColor());
            if (module.showAbsorption() && totalEnd > healthEnd) {
                graphics.fill(healthEnd, barTop, totalEnd, barTop + barHeight, module.absorptionColor());
            }
        }
        graphics.pose().popMatrix();
    }

    private static int durabilityPercent(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) return 100;
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        return Math.max(0, Math.min(100, Math.round(remaining * 100.0f / stack.getMaxDamage())));
    }

    private static LivingEntity findTarget(Minecraft client, CombatInfoModule module) {
        double range = module.range();
        double maxDistance = range * range;
        LivingEntity best = null;
        double bestDistance = maxDistance;
        for (LivingEntity entity : client.level.getEntitiesOfClass(LivingEntity.class,
                client.player.getBoundingBox().inflate(range),
                entity -> entity != client.player && entity.isAlive() && !entity.isSpectator() && allowed(entity, module))) {
            double distance = client.player.distanceToSqr(entity);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    private static boolean allowed(LivingEntity entity, CombatInfoModule module) {
        if (entity instanceof Player) return module.showPlayers();
        if (entity instanceof Animal) return module.showAnimals();
        if (entity instanceof Mob) return module.showMobs();
        return false;
    }

    private static String format(float value) {
        return String.valueOf(Math.round(value * 10.0f) / 10.0f);
    }
}
