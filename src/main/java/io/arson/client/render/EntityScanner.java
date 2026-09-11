package io.arson.client.render;

import com.arson.client.render.RenderColor;
import io.arson.client.module.EntityESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/** Discovers nearby entities for the shared ESP pipeline. */
public final class EntityScanner {
    private static final RenderColor PLAYER_COLOR = new RenderColor(85, 170, 255, 210);
    private static final RenderColor MOB_COLOR = new RenderColor(255, 85, 85, 210);
    private static final RenderColor ANIMAL_COLOR = new RenderColor(85, 255, 120, 210);
    private static final RenderColor ITEM_COLOR = new RenderColor(255, 220, 70, 210);

    public List<EntityTarget> scan(Minecraft client, EntityESPModule module) {
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
                type = EntityType.PLAYERS;
                color = PLAYER_COLOR;
            } else if (entity instanceof Animal) {
                if (!module.showAnimals()) continue;
                type = EntityType.ANIMALS;
                color = ANIMAL_COLOR;
            } else if (entity instanceof Mob) {
                if (!module.showMobs()) continue;
                type = EntityType.MOBS;
                color = MOB_COLOR;
            } else if (entity instanceof ItemEntity) {
                if (!module.showItems()) continue;
                type = EntityType.ITEMS;
                color = ITEM_COLOR;
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
}
