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

/** Cached storage discovery. Positions are reused while range filtering stays dynamic. */
public final class StorageScanner {
    private static final long CACHE_REFRESH_TICKS = 20L;

    private Object cachedLevel;
    private long cachedTick = Long.MIN_VALUE;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private int cachedRadius = Integer.MIN_VALUE;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();

    public List<StorageOverlay.StorageTarget> scan(Minecraft client, double range) {
        if (client.level == null || client.player == null) {
            return List.of();
        }

        double clampedRange = Math.max(1.0, Math.min(256.0, range));
        int radius = (int) Math.ceil(clampedRange / 16.0);
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        long gameTime = client.level.getGameTime();

        boolean worldChanged = cachedLevel != client.level;
        boolean chunkChanged = cachedChunkX != chunkX || cachedChunkZ != chunkZ;
        boolean radiusChanged = cachedRadius != radius;
        boolean refreshDue = gameTime - cachedTick >= CACHE_REFRESH_TICKS || gameTime < cachedTick;

        if (worldChanged || chunkChanged || radiusChanged || refreshDue) {
            cachedTargets = scanLoadedStorage(client, radius);
            cachedLevel = client.level;
            cachedTick = gameTime;
            cachedChunkX = chunkX;
            cachedChunkZ = chunkZ;
            cachedRadius = radius;
        }

        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        double rangeSquared = clampedRange * clampedRange;
        List<StorageOverlay.StorageTarget> visible = new ArrayList<>(cachedTargets.size());

        for (StorageOverlay.StorageTarget target : cachedTargets) {
            double dx = target.x() + target.width() * 0.5 - playerX;
            double dy = target.y() + target.height() * 0.5 - playerY;
            double dz = target.z() + target.depth() * 0.5 - playerZ;
            if (dx * dx + dy * dy + dz * dz <= rangeSquared) {
                visible.add(target);
            }
        }
        return visible.isEmpty() ? List.of() : List.copyOf(visible);
    }

    private static List<StorageOverlay.StorageTarget> scanLoadedStorage(Minecraft client, int chunkRadius) {
        int centerChunkX = client.player.blockPosition().getX() >> 4;
        int centerChunkZ = client.player.blockPosition().getZ() >> 4;
        List<StorageOverlay.StorageTarget> targets = new ArrayList<>();

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                if (!client.level.hasChunk(chunkX, chunkZ)) continue;

                LevelChunk chunk = client.level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity entity = entry.getValue();
                    StorageType type = classify(entity);
                    if (type == null) continue;

                    BlockPos pos = entry.getKey();
                    double height = entity instanceof ChestBlockEntity ? 0.875 : 1.0;
                    targets.add(new StorageOverlay.StorageTarget(
                            type, pos.getX(), pos.getY(), pos.getZ(), 1.0, height, 1.0));
                }
            }
        }
        return targets.isEmpty() ? List.of() : List.copyOf(targets);
    }

    private static StorageType classify(BlockEntity entity) {
        if (entity == null) return null;
        if (entity instanceof ChestBlockEntity) return StorageType.CHEST;
        if (entity instanceof ShulkerBoxBlockEntity) return StorageType.SHULKER;
        if (entity.getBlockState().is(Blocks.BARREL)) return StorageType.BARREL;
        if (entity.getBlockState().is(Blocks.ENDER_CHEST)) return StorageType.ENDER_CHEST;
        return null;
    }
}
