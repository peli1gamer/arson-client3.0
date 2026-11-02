package io.arson.client.render;

import com.arson.client.render.RenderStyle;
import io.arson.client.module.BlockESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Incremental block discovery with a strict per-frame world-read budget. */
public final class BlockScanner {
    static final int MAX_BLOCKS_PER_FRAME = 8_192;
    private static final int MAX_RANGE = 64;

    private Object cachedLevel;
    private Object cachedPlayer;
    private BlockPos scanOrigin;
    private long lastCompletedTick = Long.MIN_VALUE;
    private int cachedConfigHash;
    private BlockScanCursor cursor;
    private List<BlockTarget> workingTargets;
    private List<BlockTarget> cachedTargets = List.of();
    private boolean refreshRequested = true;
    private int lastFrameInspected;
    private long lastFrameScanNanos;

    /** Clears every snapshot, for disconnects, module shutdown, or world replacement. */
    public void invalidate() {
        cachedLevel = null;
        cachedPlayer = null;
        scanOrigin = null;
        lastCompletedTick = Long.MIN_VALUE;
        cachedConfigHash = 0;
        cursor = null;
        workingTargets = null;
        cachedTargets = List.of();
        refreshRequested = true;
        lastFrameInspected = 0;
        lastFrameScanNanos = 0L;
    }

    /**
     * Requests a rescan after chunk load/unload without blanking the last complete snapshot.
     * Unloaded positions are filtered by the render stage while the replacement scan runs.
     */
    public void requestRefresh() {
        // Finish the current bounded pass before restarting; chunk streaming must not starve scans.
        refreshRequested = true;
    }

    /** Advances at most 8,192 block positions per render frame. */
    public List<BlockTarget> scan(Minecraft client, BlockESPModule module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            invalidate();
            return List.of();
        }

        long startedNanos = System.nanoTime();
        long gameTime = client.level.getGameTime();
        BlockPos playerPosition = client.player.blockPosition();
        int configHash = configHash(module);
        boolean worldChanged = cachedLevel != client.level;
        boolean playerChanged = cachedPlayer != client.player;
        boolean configChanged = cachedConfigHash != configHash;

        if (worldChanged || playerChanged || configChanged) {
            cachedTargets = List.of();
            cursor = null;
            workingTargets = null;
            lastCompletedTick = Long.MIN_VALUE;
            refreshRequested = true;
        }

        boolean scanActive = cursor != null && cursor.hasNext();
        boolean movedAfterCompletedScan = !scanActive
                && movedBeyondRefreshDistance(playerPosition, module.range());
        boolean intervalElapsed = !scanActive && (lastCompletedTick == Long.MIN_VALUE
                || gameTime < lastCompletedTick
                || gameTime - lastCompletedTick >= module.scanInterval());

        if (!scanActive && (cursor == null || refreshRequested
                || movedAfterCompletedScan || intervalElapsed)) {
            beginScan(client, module, playerPosition, configHash);
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        double range = Math.max(1.0, Math.min(MAX_RANGE, module.range()));
        double rangeSquared = range * range;
        int inspected = 0;

        while (cursor.hasNext() && inspected < MAX_BLOCKS_PER_FRAME) {
            cursor.setPosition(mutable);
            cursor.advance();
            inspected++;

            double dx = mutable.getX() + 0.5 - playerX;
            double dxSquared = dx * dx;
            if (dxSquared > rangeSquared) continue;

            double dy = mutable.getY() + 0.5 - playerY;
            double partialDistanceSquared = dxSquared + dy * dy;
            if (partialDistanceSquared > rangeSquared) continue;

            double dz = mutable.getZ() + 0.5 - playerZ;
            if (partialDistanceSquared + dz * dz > rangeSquared || !client.level.hasChunkAt(mutable)) continue;

            BlockState state = client.level.getBlockState(mutable);
            RenderStyle style = match(state, module);
            if (style == null) continue;

            int x = mutable.getX();
            int y = mutable.getY();
            int z = mutable.getZ();
            workingTargets.add(new BlockTarget(x, y, z, x + 1.0, y + 1.0, z + 1.0, style));
        }

        lastFrameInspected = inspected;
        lastFrameScanNanos = System.nanoTime() - startedNanos;

        if (!cursor.hasNext() && workingTargets != null) {
            cachedTargets = workingTargets.isEmpty() ? List.of() : List.copyOf(workingTargets);
            workingTargets = null;
            lastCompletedTick = gameTime;
        }

        return cachedTargets;
    }

    private boolean movedBeyondRefreshDistance(BlockPos current, double range) {
        if (scanOrigin == null) return true;
        long dx = (long) current.getX() - scanOrigin.getX();
        long dy = (long) current.getY() - scanOrigin.getY();
        long dz = (long) current.getZ() - scanOrigin.getZ();
        long refreshDistance = Math.max(4L, (long) Math.ceil(Math.max(1.0, range) * 0.5));
        return dx * dx + dy * dy + dz * dz >= refreshDistance * refreshDistance;
    }

    private void beginScan(Minecraft client, BlockESPModule module, BlockPos origin, int configHash) {
        int radius = (int) Math.ceil(Math.max(1.0, Math.min(MAX_RANGE, module.range())));
        int minY = Math.max(client.level.getMinY(), origin.getY() - radius);
        int maxY = Math.min(client.level.getMaxY() - 1, origin.getY() + radius);

        cachedLevel = client.level;
        cachedPlayer = client.player;
        scanOrigin = origin.immutable();
        cachedConfigHash = configHash;
        cursor = new BlockScanCursor(origin.getX() - radius, minY, origin.getZ() - radius,
                origin.getX() + radius, maxY, origin.getZ() + radius);
        workingTargets = new ArrayList<>();
        refreshRequested = false;
    }

    public boolean isScanning() { return cursor != null && cursor.hasNext(); }
    public double scanProgress() {
        if (cursor == null) return cachedTargets.isEmpty() ? 0.0 : 1.0;
        return cursor.total() == 0 ? 1.0 : (double) cursor.processed() / cursor.total();
    }
    public int lastFrameInspected() { return lastFrameInspected; }
    public long lastFrameScanNanos() { return lastFrameScanNanos; }
    public int cachedTargetCount() { return cachedTargets.size(); }

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
        hash = 31 * hash + Boolean.hashCode(module.showSpawners());
        hash = 31 * hash + Boolean.hashCode(module.showLightSources());
        hash = 31 * hash + Boolean.hashCode(module.showAmethyst());
        hash = 31 * hash + Boolean.hashCode(module.showDripstone());
        hash = 31 * hash + Boolean.hashCode(module.showBeehives());
        hash = 31 * hash + module.lightThreshold();
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
        hash = 31 * hash + styleHash(module.spawnerStyle());
        hash = 31 * hash + styleHash(module.amethystStyle());
        hash = 31 * hash + styleHash(module.dripstoneStyle());
        hash = 31 * hash + styleHash(module.beehiveStyle());
        return hash;
    }

    private static int styleHash(RenderStyle style) {
        var color = style.color();
        int hash = color.red();
        hash = 31 * hash + color.green();
        hash = 31 * hash + color.blue();
        hash = 31 * hash + color.alpha();
        hash = 31 * hash + Boolean.hashCode(style.fill());
        hash = 31 * hash + Boolean.hashCode(style.outline());
        hash = 31 * hash + Float.hashCode(style.fillAlpha());
        hash = 31 * hash + Float.hashCode(style.outlineAlpha());
        hash = 31 * hash + Float.hashCode(style.lineWidth());
        return hash;
    }

    private static RenderStyle match(BlockState state, BlockESPModule module) {
        Block block = state.getBlock();
        if (module.showDiamond() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return module.diamondStyle();
        if (module.showEmerald() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return module.emeraldStyle();
        if (module.showGold() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return module.goldStyle();
        if (module.showIron() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return module.ironStyle();
        if (module.showCopper() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)) return module.copperStyle();
        if (module.showCoal() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) return module.coalStyle();
        if (module.showRedstone() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return module.redstoneStyle();
        if (module.showLapis() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) return module.lapisStyle();
        if (module.showQuartz() && block == Blocks.NETHER_QUARTZ_ORE) return module.quartzStyle();
        if (module.showAncientDebris() && block == Blocks.ANCIENT_DEBRIS) return module.ancientDebrisStyle();
        if (module.showSpawners() && block == Blocks.SPAWNER) return module.spawnerStyle();
        if (module.showAmethyst() && (block == Blocks.AMETHYST_CLUSTER
                || block == Blocks.SMALL_AMETHYST_BUD
                || block == Blocks.MEDIUM_AMETHYST_BUD
                || block == Blocks.LARGE_AMETHYST_BUD)) return module.amethystStyle();
        if (module.showDripstone() && (block == Blocks.POINTED_DRIPSTONE
                || block == Blocks.DRIPSTONE_BLOCK)) return module.dripstoneStyle();
        if (module.showBeehives() && (block == Blocks.BEEHIVE
                || block == Blocks.BEE_NEST)) return module.beehiveStyle();
        int emission = state.getLightEmission();
        if (module.showLightSources() && emission >= module.lightThreshold()) return module.lightStyle(emission);
        return null;
    }
}