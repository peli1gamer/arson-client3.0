package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

/** Cached storage discovery. Positions are reused while range filtering stays dynamic. */
public final class StorageScanner {
    private static final long CACHE_REFRESH_TICKS = 20L;

    private Object cachedLevel;
    private long cachedTick = Long.MIN_VALUE;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private int cachedRadius = Integer.MIN_VALUE;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();

    public List<StorageOverlay.StorageTarget> scan(MinecraftClient client, double range) {
        if (client.world == null || client.player == null) {
            return List.of();
        }

        double clampedRange = Math.max(1.0, Math.min(256.0, range));
        int radius = (int) Math.ceil(clampedRange / 16.0);
        int chunkX = client.player.getBlockPos().getX() >> 4;
        int chunkZ = client.player.getBlockPos().getZ() >> 4;
        long gameTime = client.world.getTime();

        boolean worldChanged = cachedLevel != client.world;
        boolean chunkChanged = cachedChunkX != chunkX || cachedChunkZ != chunkZ;
        boolean radiusChanged = cachedRadius != radius;
        boolean refreshDue = gameTime - cachedTick >= CACHE_REFRESH_TICKS || gameTime < cachedTick;

        if (worldChanged || chunkChanged || radiusChanged || refreshDue) {
            cachedTargets = scanLoadedStorage(client, radius);
            cachedLevel = client.world;
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

    private static List<StorageOverlay.StorageTarget> scanLoadedStorage(MinecraftClient client, int chunkRadius) {
        int centerChunkX = client.player.getBlockPos().getX() >> 4;
        int centerChunkZ = client.player.getBlockPos().getZ() >> 4;
        List<StorageOverlay.StorageTarget> targets = new ArrayList<>();
        Set<BlockPos> emittedChestPositions = new HashSet<>();

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                if (!client.world.isChunkLoaded(chunkX, chunkZ)) continue;

                WorldChunk chunk = client.world.getChunkManager().getWorldChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity entity = entry.getValue();
                    StorageType type = classify(entity);
                    if (type == null) continue;

                    BlockPos pos = entry.getKey();
                    if (entity instanceof ChestBlockEntity) {
                        if (emittedChestPositions.contains(pos)) continue;
                        targets.add(buildChestTarget(client, pos, emittedChestPositions));
                    } else {
                        targets.add(new StorageOverlay.StorageTarget(
                                type, pos.getX(), pos.getY(), pos.getZ(), 1.0, 1.0, 1.0));
                    }
                }
            }
        }
        return targets.isEmpty() ? List.of() : List.copyOf(targets);
    }

    private static StorageOverlay.StorageTarget buildChestTarget(MinecraftClient client, BlockPos pos,
                                                                  Set<BlockPos> emittedPositions) {
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos north = pos.north();

        BlockPos partner = null;
        if (canPairChest(client, pos, east)) partner = east;
        else if (canPairChest(client, pos, south)) partner = south;
        else if (canPairChest(client, pos, west)) partner = west;
        else if (canPairChest(client, pos, north)) partner = north;

        double x = pos.getX();
        double z = pos.getZ();
        double width = 1.0;
        double depth = 1.0;

        if (partner != null) {
            x = Math.min(pos.getX(), partner.getX());
            z = Math.min(pos.getZ(), partner.getZ());
            width = pos.getX() == partner.getX() ? 1.0 : 2.0;
            depth = pos.getZ() == partner.getZ() ? 1.0 : 2.0;
            emittedPositions.add(partner);
        }

        emittedPositions.add(pos);
        return new StorageOverlay.StorageTarget(
                StorageType.CHEST, x, pos.getY(), z, width, 0.875, depth);
    }

    private static boolean canPairChest(MinecraftClient client, BlockPos firstPos, BlockPos secondPos) {
        if (!client.world.isChunkLoaded(secondPos.getX() >> 4, secondPos.getZ() >> 4)) return false;
        return StorageChestPairing.canPair(client.world.getBlockState(firstPos), client.world.getBlockState(secondPos));
    }

    private static StorageType classify(BlockEntity entity) {
        if (entity == null) return null;
        if (entity instanceof ChestBlockEntity) return StorageType.CHEST;
        if (entity instanceof ShulkerBoxBlockEntity) return StorageType.SHULKER;
        if (entity.getCachedState().isOf(Blocks.BARREL)) return StorageType.BARREL;
        if (entity.getCachedState().isOf(Blocks.ENDER_CHEST)) return StorageType.ENDER_CHEST;
        if (entity instanceof HopperBlockEntity
                || entity instanceof DispenserBlockEntity
                || entity instanceof DropperBlockEntity) return StorageType.OTHER;
        return null;
    }
}
