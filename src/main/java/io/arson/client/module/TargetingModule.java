package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

/** Shared, non-automating target selection foundation for combat modules. */
public final class TargetingModule extends Module {
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 16.0, 1.0, 64.0, 0.5));
    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting monsters = setting(new BooleanSetting("monsters", "Monsters", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting ignoreInvisible = setting(new BooleanSetting("ignore-invisible", "Ignore Invisible", true));
    private final BooleanSetting ignoreDead = setting(new BooleanSetting("ignore-dead", "Ignore Dead", true));

    public TargetingModule() {
        super("targeting", "Targeting", Category.COMBAT);
    }

    public double range() { return range.get(); }

    public boolean isValidTarget(LivingEntity entity, Player self) {
        if (entity == null || entity == self) return false;
        if (ignoreDead.enabled() && (!entity.isAlive() || entity.isRemoved())) return false;
        if (ignoreInvisible.enabled() && entity.isInvisible()) return false;
        if (entity instanceof Player) return players.enabled();
        if (entity instanceof Monster) return monsters.enabled();
        if (entity instanceof Animal) return animals.enabled();
        return false;
    }

    /** Returns the nearest valid living target, or null when none is in range. */
    public LivingEntity findTarget(Minecraft client) {
        if (client.level == null || client.player == null) return null;
        double maxDistanceSq = range.get() * range.get();
        return client.level.getEntitiesOfClass(LivingEntity.class, client.player.getBoundingBox().inflate(range.get()),
                        entity -> isValidTarget(entity, client.player) && client.player.distanceToSqr(entity) <= maxDistanceSq)
                .stream()
                .min(Comparator.comparingDouble(client.player::distanceToSqr))
                .orElse(null);
    }
}
