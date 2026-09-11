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

/** Discovers nearby entities for the shared visualization pipeline. */
public final class EntityScanner {
    public List<EntityTarget> scan(Minecraft client, EntityScanConfig module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            return List.of();
        }

        double range = module.range();
        double maxDistanceSq = range * range;
        List<EntityTarget> result = new ArrayList<>();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player || entity.isRemoved()) continue;

            double dx = entity.getX() - client.player.getX();
            double dy = entity.getY() - client.player.getY();
            double dz = entity.getZ() - client.player.getZ();
            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) continue;

            EntityType type;
            RenderColor color;
            if (entity instanceof Player) {
                if (!module.showPlayers()) continue;
                type = EntityType.PLAYER;
                color = fromArgb(module.playerColor());
            } else if (entity instanceof Animal) {
                if (!module.showAnimals()) continue;
                type = EntityType.ANIMAL;
                color = fromArgb(module.animalColor());
            } else if (entity instanceof Mob) {
                if (!module.showMobs()) continue;
                type = EntityType.MOB;
                color = fromArgb(module.mobColor());
            } else if (entity instanceof ItemEntity) {
                if (!module.showItems()) continue;
                type = EntityType.ITEM;
                color = fromArgb(module.itemColor());
            } else {
                continue;
            }

            var box = entity.getBoundingBox();
            float health = entity instanceof net.minecraft.world.entity.LivingEntity living
                    ? living.getHealth() : 0.0f;
            float maxHealth = entity instanceof net.minecraft.world.entity.LivingEntity living
                    ? living.getMaxHealth() : 0.0f;

            result.add(new EntityTarget(
                    type,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    health, maxHealth, color));
        }

        return List.copyOf(result);
    }

    private static RenderColor fromArgb(int argb) {
        return new RenderColor(
                (argb >> 16) & 0xFF,
                (argb >> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
    }
}
