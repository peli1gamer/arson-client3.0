package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Collects nearby loaded storage block entities without scanning every block position. */
public final class StorageScanner {
    public List<StorageOverlay.StorageTarget> scan(Minecraft client, double range) {
        if (client.level == null || client.player == null) {
            return List.of();
        }

        double clampedRange = Math.max(1.0, Math.min(256.0, range));
        double rangeSquared = clampedRange * clampedRange;
        int chunkRadius = (int) Math.ceil(clampedRange / 16.0);
        int centerChunkX = client.player.blockPosition().getX() >> 4;
        int centerChunkZ = client.player.blockPosition().getZ() >> 4;
        List<StorageOverlay.StorageTarget> targets = new ArrayList<>();

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                if (!client.level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }

                LevelChunk chunk = client.level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity entity = entry.getValue();
                    StorageType type = classify(entity);
                    if (type == null) {
                        continue;
                    }

                    BlockPos pos = entry.getKey();
                    double dx = pos.getX() + 0.5 - client.player.getX();
                    double dy = pos.getY() + 0.5 - client.player.getY();
                    double dz = pos.getZ() + 0.5 - client.player.getZ();
                    if (dx * dx + dy * dy + dz * dz > rangeSquared) {
                        continue;
                    }

                    double height = entity instanceof ChestBlockEntity ? 0.875 : 1.0;
                    targets.add(new StorageOverlay.StorageTarget(
                            type, pos.getX(), pos.getY(), pos.getZ(), 1.0, height, 1.0));
                }
            }
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
