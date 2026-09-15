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
    private static final int CACHE_REFRESH_TICKS = 40;

    private Object cachedLevel;
    private long cachedTick = Long.MIN_VALUE;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private int cachedConfigHash;
    private List<BlockTarget> cachedTargets = List.of();

    public List<BlockTarget> scan(Minecraft client, BlockESPModule module) {
        if (client.level == null || client.player == null || !module.enabled()) return List.of();

        long gameTime = client.level.getGameTime();
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        int configHash = configHash(module);

        boolean worldChanged = cachedLevel != client.level;
        boolean movedChunk = cachedChunkX != chunkX || cachedChunkZ != chunkZ;
        boolean configChanged = cachedConfigHash != configHash;
        boolean refreshDue = gameTime - cachedTick >= CACHE_REFRESH_TICKS || gameTime < cachedTick;

        if (!worldChanged && !movedChunk && !configChanged && !refreshDue) {
            return cachedTargets;
        }

        List<BlockTarget> result = scanWorld(client, module);
        cachedTargets = List.copyOf(result);
        cachedLevel = client.level;
        cachedTick = gameTime;
        cachedChunkX = chunkX;
        cachedChunkZ = chunkZ;
        cachedConfigHash = configHash;
        return cachedTargets;
    }

    private static List<BlockTarget> scanWorld(Minecraft client, BlockESPModule module) {
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        double range = Math.max(1.0, module.range());
        double rangeSquared = range * range;
        int radius = Math.max(1, Math.min(64, (int) Math.ceil(range)));
        BlockPos center = client.player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<BlockTarget> result = new ArrayList<>();

        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = Math.max(client.level.getMinY(), center.getY() - radius);
        int maxY = Math.min(client.level.getMaxY(), center.getY() + radius);
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            double dx = x + 0.5 - playerX;
            double dxSquared = dx * dx;
            if (dxSquared > rangeSquared) continue;

            for (int y = minY; y <= maxY; y++) {
                double dy = y + 0.5 - playerY;
                double partialDistanceSquared = dxSquared + dy * dy;
                if (partialDistanceSquared > rangeSquared) continue;

                for (int z = minZ; z <= maxZ; z++) {
                    double dz = z + 0.5 - playerZ;
                    double distanceSquared = partialDistanceSquared + dz * dz;
                    if (distanceSquared > rangeSquared) continue;

                    mutable.set(x, y, z);
                    BlockState state = client.level.getBlockState(mutable);
                    RenderStyleMatch match = match(state.getBlock(), module);
                    if (match == null) continue;

                    result.add(new BlockTarget(
                            x, y, z,
                            x + 1.0, y + 1.0, z + 1.0,
                            match.style(), Math.sqrt(distanceSquared)));
                }
            }
        }
        return result;
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
