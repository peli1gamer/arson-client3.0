package io.arson.client.render;

import io.arson.client.module.StructureESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Incremental loaded-chunk-only structure scan with per-frame candidate and deduplication bounds. */
public final class StructureScanner {
    static final int MAX_CANDIDATES_PER_FRAME = 256;
    private static final int MAX_RANGE = 32;
    private static final int MAX_VERTICAL_RANGE = 16;

    private Object cachedLevel;
    private Object cachedPlayer;
    private BlockPos scanOrigin;
    private int cachedConfigHash;
    private long lastCompletedTick = Long.MIN_VALUE;
    private BlockScanCursor cursor;
    private List<StructureDetector.StructureTarget> workingTargets;
    private List<StructureDetector.StructureTarget> cachedTargets = List.of();
    private Set<BlockPos> coveredPositions;
    private boolean refreshRequested = true;
    private int lastFrameInspected;
    private long lastFrameScanNanos;

    public void invalidate() {
        cachedLevel = null;
        cachedPlayer = null;
        scanOrigin = null;
        cachedConfigHash = 0;
        lastCompletedTick = Long.MIN_VALUE;
        cursor = null;
        workingTargets = null;
        cachedTargets = List.of();
        coveredPositions = null;
        refreshRequested = true;
        lastFrameInspected = 0;
        lastFrameScanNanos = 0L;
    }

    public void requestRefresh() {
        refreshRequested = true;
    }

    public List<StructureDetector.StructureTarget> scan(Minecraft client, StructureESPModule module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            invalidate();
            return List.of();
        }

        long startedNanos = System.nanoTime();
        long gameTime = client.level.getGameTime();
        BlockPos playerPosition = client.player.blockPosition();
        int configHash = configHash(module);
        boolean identityChanged = cachedLevel != client.level || cachedPlayer != client.player;
        boolean configChanged = cachedConfigHash != configHash;

        if (identityChanged || configChanged) {
            cachedTargets = List.of();
            cursor = null;
            workingTargets = null;
            coveredPositions = null;
            lastCompletedTick = Long.MIN_VALUE;
            refreshRequested = true;
        }

        boolean scanActive = cursor != null && cursor.hasNext();
        boolean moved = !scanActive && movedBeyondRefreshDistance(playerPosition,
                module.range(), module.verticalRange());
        boolean refreshDue = !scanActive && (lastCompletedTick == Long.MIN_VALUE
                || gameTime < lastCompletedTick
                || gameTime - lastCompletedTick >= module.scanInterval());

        if (!scanActive && (cursor == null || refreshRequested || moved || refreshDue)) {
            beginScan(client, module, playerPosition, configHash);
        }

        advanceScan(client, module, gameTime);
        lastFrameScanNanos = System.nanoTime() - startedNanos;
        return cachedTargets;
    }

    private void beginScan(Minecraft client, StructureESPModule module, BlockPos origin, int configHash) {
        int horizontal = clampedRange(module.range());
        int vertical = clampedVerticalRange(module.verticalRange());
        int minY = Math.max(client.level.getMinY() + 1, origin.getY() - vertical);
        int maxY = Math.min(client.level.getMaxY() - 3, origin.getY() + vertical);

        cachedLevel = client.level;
        cachedPlayer = client.player;
        scanOrigin = origin.immutable();
        cachedConfigHash = configHash;
        workingTargets = new ArrayList<>();
        coveredPositions = new HashSet<>();
        refreshRequested = false;
        cursor = new BlockScanCursor(origin.getX() - horizontal, minY, origin.getZ() - horizontal,
                origin.getX() + horizontal, maxY, origin.getZ() + horizontal);
    }

    private void advanceScan(Minecraft client, StructureESPModule module, long gameTime) {
        if (cursor == null) return;

        int inspected = 0;
        int horizontal = clampedRange(module.range());
        int vertical = clampedVerticalRange(module.verticalRange());
        double rangeSquared = (double) horizontal * horizontal;
        int playerY = client.player.blockPosition().getY();
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        StructureDetector.BlockAccess access = new LevelBlockAccess(client);

        while (cursor.hasNext() && inspected < MAX_CANDIDATES_PER_FRAME) {
            cursor.setPosition(mutable);
            cursor.advance();
            inspected++;

            int x = mutable.getX();
            int y = mutable.getY();
            int z = mutable.getZ();
            double dx = x + 0.5 - playerX;
            double dz = z + 0.5 - playerZ;
            if (dx * dx + dz * dz > rangeSquared || Math.abs(y - playerY) > vertical) continue;
            if (!client.level.hasChunkAt(mutable)) continue;
            if (coveredPositions.contains(BlockPos.containing(x, y, z))) continue;

            StructureDetector.StructureTarget target = StructureDetector.detectAt(access, x, y, z);
            if (target == null) continue;
            workingTargets.add(target);
            markCovered(target);
        }

        lastFrameInspected = inspected;
        if (!cursor.hasNext() && workingTargets != null) {
            cachedTargets = workingTargets == null || workingTargets.isEmpty()
                    ? List.of() : List.copyOf(workingTargets);
            workingTargets = null;
            coveredPositions = null;
            lastCompletedTick = gameTime;
        }
    }

    private void markCovered(StructureDetector.StructureTarget target) {
        for (int index = 0; index < target.length(); index++) {
            int x = target.x() + (target.alongX() ? index : 0);
            int y = target.y() + target.stepY() * index;
            int z = target.z() + (target.alongX() ? 0 : index);
            coveredPositions.add(BlockPos.containing(x, y, z));
        }
    }

    private boolean movedBeyondRefreshDistance(BlockPos current, double range, double verticalRange) {
        if (scanOrigin == null) return true;
        long dx = (long) current.getX() - scanOrigin.getX();
        long dz = (long) current.getZ() - scanOrigin.getZ();
        long horizontalThreshold = Math.max(4L, (long) Math.ceil(clampedRange(range) * 0.5));
        long dy = Math.abs((long) current.getY() - scanOrigin.getY());
        long verticalThreshold = Math.max(2L, (long) Math.ceil(clampedVerticalRange(verticalRange) * 0.5));
        return dx * dx + dz * dz >= horizontalThreshold * horizontalThreshold
                || dy >= verticalThreshold;
    }

    private static int configHash(StructureESPModule module) {
        int hash = Double.hashCode(module.range());
        hash = 31 * hash + Double.hashCode(module.verticalRange());
        return hash;
    }

    private static int clampedRange(double range) {
        return (int) Math.ceil(Math.max(1.0, Math.min(MAX_RANGE,
                Double.isFinite(range) ? range : 1.0)));
    }

    private static int clampedVerticalRange(double range) {
        return (int) Math.ceil(Math.max(1.0, Math.min(MAX_VERTICAL_RANGE,
                Double.isFinite(range) ? range : 1.0)));
    }

    public static boolean isTargetLoaded(Minecraft client, StructureDetector.StructureTarget target) {
        if (client.level == null || target == null) return false;
        BlockPos first = BlockPos.containing(target.minX(), target.minY(), target.minZ());
        BlockPos last = BlockPos.containing(target.maxX() - 1, target.maxY() - 1, target.maxZ() - 1);
        return client.level.hasChunkAt(first) && client.level.hasChunkAt(last);
    }

    public boolean isScanning() { return cursor != null && cursor.hasNext(); }
    public double scanProgress() {
        if (cursor == null) return cachedTargets.isEmpty() ? 0.0 : 1.0;
        return cursor.total() == 0 ? 1.0 : (double) cursor.processed() / cursor.total();
    }
    public int lastFrameInspected() { return lastFrameInspected; }
    public long lastFrameScanNanos() { return lastFrameScanNanos; }
    public int cachedTargetCount() { return cachedTargets.size(); }

    private static final class LevelBlockAccess implements StructureDetector.BlockAccess {
        private final Minecraft client;

        private LevelBlockAccess(Minecraft client) {
            this.client = client;
        }

        private BlockState state(int x, int y, int z) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!client.level.hasChunkAt(pos)) return null;
            return client.level.getBlockState(pos);
        }

        @Override public boolean isAir(int x, int y, int z) {
            BlockState state = state(x, y, z);
            return state != null && state.isAir();
        }

        @Override public boolean isSolid(int x, int y, int z) {
            BlockState state = state(x, y, z);
            return state != null && state.blocksMotion();
        }

        @Override public boolean isStair(int x, int y, int z) {
            BlockState state = state(x, y, z);
            return state != null && state.getBlock() instanceof StairBlock;
        }
    }
}
