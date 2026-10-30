package io.arson.client.render;

import io.arson.client.module.BlockESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Cached ore discovery with allocation-light scanning and chunk-aware reuse. */
public final class BlockScanner {
    private static final int POSITIONS_PER_CALL = 16_384;

    private Object cachedLevel;
    private long cachedTick = Long.MIN_VALUE;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private int cachedConfigHash;
    private List<BlockTarget> cachedTargets = List.of();

    private boolean scanning;
    private Object scanLevel;
    private int scanChunkX;
    private int scanChunkZ;
    private int scanConfigHash;
    private double scanPlayerX;
    private double scanPlayerY;
    private double scanPlayerZ;
    private double scanRangeSquared;
    private int minX, maxX, minY, maxY, minZ, maxZ;
    private int nextX, nextY, nextZ;
    private BlockPos.MutableBlockPos mutable;
    private List<BlockTarget> workingTargets;

    /**
     * Advances a bounded slice of the ore scan on each render call. The previous
     * completed snapshot remains visible until the replacement scan is complete.
     */
    public List<BlockTarget> scan(Minecraft client, BlockESPModule module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            scanning = false;
            cachedTargets = List.of();
            cachedLevel = null;
            cachedTick = Long.MIN_VALUE;
            return cachedTargets;
        }

        long gameTime = client.level.getGameTime();
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        int configHash = configHash(module);

        boolean worldChanged = cachedLevel != client.level;
        boolean movedChunk = cachedChunkX != chunkX || cachedChunkZ != chunkZ;
        boolean configChanged = cachedConfigHash != configHash;
        boolean scanInvalidated = scanning && (scanLevel != client.level
                || scanChunkX != chunkX || scanChunkZ != chunkZ || scanConfigHash != configHash);

        if (scanInvalidated) scanning = false;
        boolean refreshDue = shouldRefresh(gameTime, cachedTick, module.scanInterval());
        if (!scanning && (worldChanged || movedChunk || configChanged || refreshDue)) {
            beginScan(client, module, gameTime, chunkX, chunkZ, configHash);
        }

        if (scanning) advanceScan(client, module);
        return cachedTargets;
    }

    static boolean shouldRefresh(long gameTime, long cachedTick, int scanInterval) {
        return cachedTick == Long.MIN_VALUE || gameTime < cachedTick
                || gameTime - cachedTick >= Math.max(1, scanInterval);
    }

    private void beginScan(Minecraft client, BlockESPModule module, long gameTime,
                           int chunkX, int chunkZ, int configHash) {
        scanLevel = client.level;
        scanChunkX = chunkX;
        scanChunkZ = chunkZ;
        scanConfigHash = configHash;
        scanPlayerX = client.player.getX();
        scanPlayerY = client.player.getY();
        scanPlayerZ = client.player.getZ();
        double range = Math.max(1.0, module.range());
        scanRangeSquared = range * range;
        int radius = Math.max(1, Math.min(64, (int) Math.ceil(range)));
        BlockPos center = client.player.blockPosition();
        minX = center.getX() - radius;
        maxX = center.getX() + radius;
        minY = Math.max(client.level.getMinY(), center.getY() - radius);
        maxY = Math.min(client.level.getMaxY(), center.getY() + radius);
        minZ = center.getZ() - radius;
        maxZ = center.getZ() + radius;
        nextX = minX;
        nextY = minY;
        nextZ = minZ;
        mutable = new BlockPos.MutableBlockPos();
        workingTargets = new ArrayList<>();
        scanning = true;
    }

    private void advanceScan(Minecraft client, BlockESPModule module) {
        int visited = 0;
        while (scanning && visited < POSITIONS_PER_CALL && nextX <= maxX) {
            int x = nextX, y = nextY, z = nextZ;
            advanceCursor();
            visited++;

            double dx = x + 0.5 - scanPlayerX;
            double dy = y + 0.5 - scanPlayerY;
            double dz = z + 0.5 - scanPlayerZ;
            if (dx * dx + dy * dy + dz * dz > scanRangeSquared) continue;

            mutable.set(x, y, z);
            BlockState state = client.level.getBlockState(mutable);
            RenderStyleMatch match = match(state.getBlock(), module);
            if (match == null) continue;

            workingTargets.add(new BlockTarget(
                    x, y, z, x + 1.0, y + 1.0, z + 1.0, match.style()));
        }

        if (nextX > maxX) {
            cachedTargets = List.copyOf(workingTargets);
            cachedLevel = scanLevel;
            cachedTick = client.level.getGameTime();
            cachedChunkX = scanChunkX;
            cachedChunkZ = scanChunkZ;
            cachedConfigHash = scanConfigHash;
            workingTargets = null;
            mutable = null;
            scanning = false;
        }
    }

    private void advanceCursor() {
        nextZ++;
        if (nextZ > maxZ) {
            nextZ = minZ;
            nextY++;
            if (nextY > maxY) {
                nextY = minY;
                nextX++;
            }
        }
    }

    private static int configHash(BlockESPModule module) {
        int hash = 17;
        hash = 31 * hash + Double.hashCode(module.range());
        hash = 31 * hash + Boolean.hashCode(module.showDiamond());
        hash = 31 * hash + Boolean.hashCode(module.showEmerald());
        hash = 31 * hash + Boolean.hashCode(module.showGold());
        hash = 31 * hash + Boolean.hashCode(module.showIron());
        hash = 31 * hash + Boolean.hashCode(module.showCopper());
        hash = 31 * hash + Boolean.hashCode(module.showCoal());
        hash = 31 * hash + Boolean.hashCode(module.showRedstone());
        hash = 31 * hash + Boolean.hashCode(module.showLapis());
        hash = 31 * hash + Boolean.hashCode(module.showQuartz());
        hash = 31 * hash + Boolean.hashCode(module.showAncientDebris());
        hash = 31 * hash + styleHash(module.diamondStyle());
        hash = 31 * hash + styleHash(module.emeraldStyle());
        hash = 31 * hash + styleHash(module.goldStyle());
        hash = 31 * hash + styleHash(module.ironStyle());
        hash = 31 * hash + styleHash(module.copperStyle());
        hash = 31 * hash + styleHash(module.coalStyle());
        hash = 31 * hash + styleHash(module.redstoneStyle());
        hash = 31 * hash + styleHash(module.lapisStyle());
        hash = 31 * hash + styleHash(module.quartzStyle());
        hash = 31 * hash + styleHash(module.ancientDebrisStyle());
        return hash;
    }

    private static int styleHash(RenderStyle style) {
        int hash = style.color().argb();
        hash = 31 * hash + Boolean.hashCode(style.fill());
        hash = 31 * hash + Boolean.hashCode(style.outline());
        hash = 31 * hash + Float.hashCode(style.fillAlpha());
        hash = 31 * hash + Float.hashCode(style.outlineAlpha());
        hash = 31 * hash + Float.hashCode(style.lineWidth());
        return hash;
    }

    private static RenderStyleMatch match(Block block, BlockESPModule module) {
        if (module.showDiamond() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE))
            return new RenderStyleMatch(module.diamondStyle());
        if (module.showEmerald() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE))
            return new RenderStyleMatch(module.emeraldStyle());
        if (module.showGold() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE))
            return new RenderStyleMatch(module.goldStyle());
        if (module.showIron() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE))
            return new RenderStyleMatch(module.ironStyle());
        if (module.showCopper() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE))
            return new RenderStyleMatch(module.copperStyle());
        if (module.showCoal() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE))
            return new RenderStyleMatch(module.coalStyle());
        if (module.showRedstone() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE))
            return new RenderStyleMatch(module.redstoneStyle());
        if (module.showLapis() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE))
            return new RenderStyleMatch(module.lapisStyle());
        if (module.showQuartz() && block == Blocks.NETHER_QUARTZ_ORE)
            return new RenderStyleMatch(module.quartzStyle());
        if (module.showAncientDebris() && block == Blocks.ANCIENT_DEBRIS)
            return new RenderStyleMatch(module.ancientDebrisStyle());
        return null;
    }

    private record RenderStyleMatch(com.arson.client.render.RenderStyle style) {}
}
