package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Incremental storage discovery; chunk and block-entity reads stay on the client thread. */
public final class StorageScanner {
    static final int MAX_CHUNKS_PER_FRAME = 16;
    static final int MAX_BLOCK_ENTRIES_PER_FRAME = 512;
    private static final int MAX_RANGE = 256;

    private Object cachedLevel;
    private Object cachedPlayer;
    private long lastCompletedTick = Long.MIN_VALUE;
    private int cachedChunkX = Integer.MIN_VALUE;
    private int cachedChunkZ = Integer.MIN_VALUE;
    private int cachedRadius = Integer.MIN_VALUE;
    private StorageChunkCursor cursor;
    private Iterator<Map.Entry<BlockPos, BlockEntity>> currentEntries;
    private List<StorageOverlay.StorageTarget> workingTargets;
    private Set<BlockPos> emittedChestPositions;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();
    private boolean refreshRequested = true;
    private int lastFrameChunks;
    private int lastFrameEntries;
    private long lastFrameScanNanos;

    public void invalidate() {
        cachedLevel = null;
        cachedPlayer = null;
        lastCompletedTick = Long.MIN_VALUE;
        cachedChunkX = Integer.MIN_VALUE;
        cachedChunkZ = Integer.MIN_VALUE;
        cachedRadius = Integer.MIN_VALUE;
        cursor = null;
        currentEntries = null;
        workingTargets = null;
        emittedChestPositions = null;
        cachedTargets = List.of();
        refreshRequested = true;
        lastFrameChunks = 0;
        lastFrameEntries = 0;
        lastFrameScanNanos = 0L;
    }

    /** Restarts discovery after chunk load/unload while retaining the last complete snapshot. */
    public void requestRefresh() {
        // Defer refresh until the active bounded pass completes to avoid starvation during chunk streaming.
        refreshRequested = true;
    }

    public List<StorageOverlay.StorageTarget> scan(Minecraft client, double range) {
        return scan(client, range, 20);
    }

    /** Advances bounded chunk and block-entity work and returns the last complete snapshot. */
    public List<StorageOverlay.StorageTarget> scan(Minecraft client, double range, int refreshIntervalTicks) {
        if (client.level == null || client.player == null) {
            invalidate();
            return List.of();
        }

        long startedNanos = System.nanoTime();
        double clampedRange = Math.max(1.0, Math.min(MAX_RANGE, Double.isFinite(range) ? range : 1.0));
        int radius = (int) Math.ceil(clampedRange / 16.0);
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        long gameTime = client.level.getGameTime();

        boolean identityChanged = cachedLevel != client.level || cachedPlayer != client.player;
        if (identityChanged) {
            cachedTargets = List.of();
            cursor = null;
            currentEntries = null;
            workingTargets = null;
            emittedChestPositions = null;
            lastCompletedTick = Long.MIN_VALUE;
            refreshRequested = true;
        }

        boolean scanActive = isScanning();
        boolean areaChanged = !scanActive && (cachedChunkX != chunkX || cachedChunkZ != chunkZ
                || cachedRadius != radius);
        boolean refreshDue = !scanActive && (lastCompletedTick == Long.MIN_VALUE
                || gameTime < lastCompletedTick
                || gameTime - lastCompletedTick >= Math.max(1, refreshIntervalTicks));

        if (!scanActive && (cursor == null || refreshRequested || areaChanged || refreshDue)) {
            beginScan(client, chunkX, chunkZ, radius);
        }

        advanceScan(client, gameTime);
        lastFrameScanNanos = System.nanoTime() - startedNanos;
        return cachedTargets;
    }

    private void beginScan(Minecraft client, int centerChunkX, int centerChunkZ, int chunkRadius) {
        cursor = new StorageChunkCursor(centerChunkX - chunkRadius, centerChunkZ - chunkRadius,
                centerChunkX + chunkRadius, centerChunkZ + chunkRadius);
        currentEntries = null;
        workingTargets = new ArrayList<>();
        emittedChestPositions = new HashSet<>();
        cachedLevel = client.level;
        cachedPlayer = client.player;
        cachedChunkX = centerChunkX;
        cachedChunkZ = centerChunkZ;
        cachedRadius = chunkRadius;
        refreshRequested = false;
    }

    private void advanceScan(Minecraft client, long gameTime) {
        int chunks = 0;
        int entries = 0;

        while (chunks < MAX_CHUNKS_PER_FRAME && entries < MAX_BLOCK_ENTRIES_PER_FRAME) {
            if (currentEntries != null && currentEntries.hasNext()) {
                Map.Entry<BlockPos, BlockEntity> entry = currentEntries.next();
                entries++;
                addTarget(client, entry.getKey(), entry.getValue());
                continue;
            }

            currentEntries = null;
            if (!cursor.hasNext()) break;

            int chunkX = cursor.nextX();
            int chunkZ = cursor.nextZ();
            cursor.advance();
            chunks++;

            if (!client.level.hasChunk(chunkX, chunkZ)) continue;
            LevelChunk chunk = client.level.getChunkSource().getChunkNow(chunkX, chunkZ);
            if (chunk != null) currentEntries = chunk.getBlockEntities().entrySet().iterator();
        }

        lastFrameChunks = chunks;
        lastFrameEntries = entries;

        if (!cursor.hasNext() && (currentEntries == null || !currentEntries.hasNext())
                && workingTargets != null) {
            cachedTargets = workingTargets.isEmpty() ? List.of() : List.copyOf(workingTargets);
            workingTargets = null;
            emittedChestPositions = null;
            currentEntries = null;
            lastCompletedTick = gameTime;
        }
    }

    private void addTarget(Minecraft client, BlockPos pos, BlockEntity entity) {
        StorageType type = classify(entity);
        if (type == null) return;

        if (entity instanceof ChestBlockEntity) {
            if (emittedChestPositions.contains(pos)) return;
            workingTargets.add(buildChestTarget(client, pos, emittedChestPositions));
        } else {
            workingTargets.add(new StorageOverlay.StorageTarget(
                    type, pos.getX(), pos.getY(), pos.getZ(), 1.0, 1.0, 1.0));
        }
    }

    public static boolean isTargetLoaded(Minecraft client, StorageOverlay.StorageTarget target) {
        BlockPos first = BlockPos.containing(target.x(), target.y(), target.z());
        BlockPos last = BlockPos.containing(target.x() + Math.max(0.0, target.width() - 1.0),
                target.y(), target.z() + Math.max(0.0, target.depth() - 1.0));
        return client.level.hasChunkAt(first) && client.level.hasChunkAt(last);
    }

    private static StorageOverlay.StorageTarget buildChestTarget(Minecraft client, BlockPos pos,
                                                                  Set<BlockPos> emittedPositions) {
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos north = pos.north();

        BlockPos partner = null;
        if (isCompatibleChestPair(client, pos, east)) partner = east;
        else if (isCompatibleChestPair(client, pos, south)) partner = south;
        else if (isCompatibleChestPair(client, pos, west)) partner = west;
        else if (isCompatibleChestPair(client, pos, north)) partner = north;

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

    static boolean isCompatibleChestPair(Minecraft client, BlockPos firstPos, BlockPos secondPos) {
        if (!client.level.hasChunkAt(secondPos)
                || !(client.level.getBlockEntity(firstPos) instanceof ChestBlockEntity)
                || !(client.level.getBlockEntity(secondPos) instanceof ChestBlockEntity)) {
            return false;
        }
        return areCompatibleChestStates(client.level.getBlockState(firstPos),
                client.level.getBlockState(secondPos), firstPos, secondPos);
    }

    static boolean areCompatibleChestStates(BlockState first, BlockState second,
                                             BlockPos firstPos, BlockPos secondPos) {
        if (!isChestBlock(first) || !isChestBlock(second)) return false;
        Direction facing = first.getValue(ChestBlock.FACING);
        boolean sideAdjacent = secondPos.equals(firstPos.relative(facing.getClockWise()))
                || secondPos.equals(firstPos.relative(facing.getCounterClockWise()));
        return areCompatibleChestHalves(first.getValue(ChestBlock.TYPE), second.getValue(ChestBlock.TYPE),
                facing, second.getValue(ChestBlock.FACING), first.getBlock() == second.getBlock(), sideAdjacent);
    }

    static boolean areCompatibleChestHalves(ChestType firstType, ChestType secondType,
                                             Direction firstFacing, Direction secondFacing,
                                             boolean sameBlock, boolean sideAdjacent) {
        return sameBlock && sideAdjacent && firstFacing == secondFacing
                && firstType != ChestType.SINGLE && secondType != ChestType.SINGLE
                && firstType != secondType;
    }

    private static boolean isChestBlock(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST);
    }

    private static StorageType classify(BlockEntity entity) {
        if (entity == null) return null;
        if (entity instanceof ChestBlockEntity) return StorageType.CHEST;
        if (entity instanceof ShulkerBoxBlockEntity) return StorageType.SHULKER;
        if (entity.getBlockState().is(Blocks.BARREL)) return StorageType.BARREL;
        if (entity.getBlockState().is(Blocks.ENDER_CHEST)) return StorageType.ENDER_CHEST;
        if (entity instanceof HopperBlockEntity
                || entity instanceof DispenserBlockEntity
                || entity instanceof DropperBlockEntity) return StorageType.OTHER;
        return null;
    }

    public boolean isScanning() {
        return cursor != null && (cursor.hasNext() || currentEntries != null && currentEntries.hasNext());
    }
    public double scanProgress() {
        if (cursor == null) return cachedTargets.isEmpty() ? 0.0 : 1.0;
        return cursor.total() == 0 ? 1.0 : (double) cursor.processed() / cursor.total();
    }
    public int lastFrameChunks() { return lastFrameChunks; }
    public int lastFrameEntries() { return lastFrameEntries; }
    public long lastFrameScanNanos() { return lastFrameScanNanos; }
    public int cachedTargetCount() { return cachedTargets.size(); }
}