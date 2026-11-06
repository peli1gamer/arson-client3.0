package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

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

    public boolean isValidTarget(LivingEntity entity, PlayerEntity self) {
        if (entity == null || entity == self) return false;
        if (ignoreDead.enabled() && (!entity.isAlive() || entity.isRemoved())) return false;
        if (ignoreInvisible.enabled() && entity.isInvisible()) return false;
        if (entity instanceof PlayerEntity player) {
            FriendsModule friends = friendsModule();
            if (friends != null && friends.isFriend(player)) return false;
            return players.enabled();
        }
        if (entity instanceof HostileEntity) return monsters.enabled();
        if (entity instanceof AnimalEntity) return animals.enabled();
        return false;
    }

    /** Returns the nearest valid living target, or null when none is in range. */
    public LivingEntity findTarget(MinecraftClient client) {
        if (client.world == null || client.player == null) return null;
        double maxDistanceSq = range.get() * range.get();
        return client.world.getEntitiesByClass(LivingEntity.class, client.player.getBoundingBox().expand(range.get()),
                        entity -> isValidTarget(entity, client.player) && client.player.squaredDistanceTo(entity) <= maxDistanceSq)
                .stream()
                .min(Comparator.comparingDouble(client.player::squaredDistanceTo))
                .orElse(null);
    }

    private FriendsModule friendsModule() {
        if (io.arson.client.ArsonClient.getInstance() == null) return null;
        Module module = io.arson.client.ArsonClient.getInstance().modules().get("friends");
        return module instanceof FriendsModule friends ? friends : null;
    }
}
