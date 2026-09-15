package io.arson.client.render;

import com.arson.client.render.RenderColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/** Discovers nearby entities through a shared per-world-tick snapshot. */
public final class EntityScanner {
    private static final double SNAPSHOT_RANGE = 128.0;
    private static final double SNAPSHOT_RANGE_SQUARED = SNAPSHOT_RANGE * SNAPSHOT_RANGE;

    private static List<RawEntityTarget> snapshot = List.of();
    private static Object snapshotLevel;
    private static long snapshotTick = Long.MIN_VALUE;

    public List<EntityTarget> scan(Minecraft client, EntityScanConfig module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            return List.of();
        }

        refreshSnapshot(client);
        double range = Math.min(SNAPSHOT_RANGE, Math.max(1.0, module.range()));
        double maxDistanceSq = range * range;
        List<EntityTarget> result = new ArrayList<>();

        for (RawEntityTarget raw : snapshot) {
            if (raw.distanceSq() > maxDistanceSq) continue;
            if (raw.type() == EntityType.PLAYER && !module.showPlayers()) continue;
            if (raw.type() == EntityType.MOB && !module.showMobs()) continue;
            if (raw.type() == EntityType.ANIMAL && !module.showAnimals()) continue;
            if (raw.type() == EntityType.ITEM && !module.showItems()) continue;

            RenderColor color = fromArgb(switch (raw.type()) {
                case PLAYER -> module.playerColor();
                case MOB -> module.mobColor();
                case ANIMAL -> module.animalColor();
                case ITEM -> module.itemColor();
            });

            result.add(new EntityTarget(
                    raw.type(),
                    raw.minX(), raw.minY(), raw.minZ(),
                    raw.maxX(), raw.maxY(), raw.maxZ(),
                    raw.health(), raw.maxHealth(), color,
                    raw.displayName(), raw.itemCount(), Math.sqrt(raw.distanceSq())));
        }

        return List.copyOf(result);
    }

    private static void refreshSnapshot(Minecraft client) {
        long gameTime = client.level.getGameTime();
        if (snapshotLevel == client.level && snapshotTick == gameTime) return;

        List<RawEntityTarget> result = new ArrayList<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved()) continue;

            EntityType type;
            if (entity instanceof Player) type = EntityType.PLAYER;
            else if (entity instanceof Animal) type = EntityType.ANIMAL;
            else if (entity instanceof Mob) type = EntityType.MOB;
            else if (entity instanceof ItemEntity) type = EntityType.ITEM;
            else continue;

            double dx = entity.getX() - client.player.getX();
            double dy = entity.getY() - client.player.getY();
            double dz = entity.getZ() - client.player.getZ();
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq > SNAPSHOT_RANGE_SQUARED) continue;

            var box = entity.getBoundingBox();
            float health = entity instanceof net.minecraft.world.entity.LivingEntity living
                    ? living.getHealth() : 0.0f;
            float maxHealth = entity instanceof net.minecraft.world.entity.LivingEntity living
                    ? living.getMaxHealth() : 0.0f;
            int itemCount = entity instanceof ItemEntity item ? item.getItem().getCount() : 0;

            result.add(new RawEntityTarget(
                    type,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    health, maxHealth,
                    entity.getName().getString(), itemCount, distanceSq));
        }

        snapshot = List.copyOf(result);
        snapshotLevel = client.level;
        snapshotTick = gameTime;
    }

    private static RenderColor fromArgb(int argb) {
        return new RenderColor(
                (argb >> 16) & 0xFF,
                (argb >> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
    }

    private record RawEntityTarget(
            EntityType type,
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ,
            float health, float maxHealth,
            String displayName, int itemCount, double distanceSq) {}
}
