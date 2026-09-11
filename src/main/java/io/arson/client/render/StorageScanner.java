package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Collects nearby loaded storage block entities without doing a world-wide scan. */
public final class StorageScanner {
    public List<StorageOverlay.StorageTarget> scan(Minecraft client, double range) {
        if (client.level == null || client.player == null) {
            return List.of();
        }

        int radius = (int) Math.ceil(Math.max(1.0, Math.min(256.0, range)));
        BlockPos origin = client.player.blockPosition();
        List<StorageOverlay.StorageTarget> targets = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (origin.distSqr(pos) > range * range) continue;

            BlockEntity entity = client.level.getBlockEntity(pos);
            StorageType type = classify(entity);
            if (type == null) continue;

            double height = entity instanceof ChestBlockEntity ? 0.875 : 1.0;
            targets.add(new StorageOverlay.StorageTarget(
                    type, pos.getX(), pos.getY(), pos.getZ(), 1.0, height, 1.0));
        }
        return targets;
    }

    private StorageType classify(BlockEntity entity) {
        if (entity == null) return null;
        if (entity instanceof ChestBlockEntity) return StorageType.CHEST;
        if (entity instanceof ShulkerBoxBlockEntity) return StorageType.SHULKER;
        if (entity.getBlockState().is(Blocks.BARREL)) return StorageType.BARREL;
        if (entity.getBlockState().is(Blocks.ENDER_CHEST)) return StorageType.ENDER_CHEST;
        return null;
    }
}
