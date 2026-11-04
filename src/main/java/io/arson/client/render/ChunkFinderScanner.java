package io.arson.client.render;

import io.arson.client.module.ChunkFinderModule;
import io.arson.client.notification.NotificationCenter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Incremental, client-thread-only chunk analysis. Never requests or force-loads chunks. */
public final class ChunkFinderScanner {
    public record ChunkResult(int chunkX, int chunkZ, int score, double centerY, String signals) {}
    private final Map<Long, ChunkResult> results = new LinkedHashMap<>();
    private final ArrayDeque<Long> pending = new ArrayDeque<>();
    private final Set<Long> queued = new HashSet<>();
    private final Set<Long> notified = new HashSet<>();
    private Object levelIdentity;
    private long lastStepTick = Long.MIN_VALUE;
    private long lastCycleTick = Long.MIN_VALUE;
    private int centerChunkX = Integer.MIN_VALUE;
    private int centerChunkZ = Integer.MIN_VALUE;
    private int lastRadius = -1;
    private int lastConfigHash;

    public List<ChunkResult> scan(Minecraft client, ChunkFinderModule module) {
        if (client.level == null || client.player == null || !module.enabled()) {
            clear();
            return List.of();
        }

        long now = client.level.getGameTime();
        int cx = client.player.blockPosition().getX() >> 4;
        int cz = client.player.blockPosition().getZ() >> 4;
        int radius = module.radiusChunks();
        int configHash = configHash(module);

        if (levelIdentity != client.level) {
            clear();
            levelIdentity = client.level;
        }
        if (cx != centerChunkX || cz != centerChunkZ || radius != lastRadius || configHash != lastConfigHash) {
            centerChunkX = cx;
            centerChunkZ = cz;
            lastRadius = radius;
            lastConfigHash = configHash;
            pending.clear();
            queued.clear();
            results.clear();
            notified.clear();
            enqueueLoadedChunks(client, cx, cz, radius);
            lastCycleTick = now;
        } else if (pending.isEmpty() && now - lastCycleTick >= 100L) {
            enqueueLoadedChunks(client, cx, cz, radius);
            lastCycleTick = now;
        }

        if (now != lastStepTick && (lastStepTick == Long.MIN_VALUE || now - lastStepTick >= module.scanIntervalTicks())) {
            lastStepTick = now;
            int budget = module.chunksPerTick();
            while (budget-- > 0 && !pending.isEmpty()) {
                long key = pending.removeFirst();
                queued.remove(key);
                int chunkX = (int) (key >> 32);
                int chunkZ = (int) key;
                if (!client.level.hasChunk(chunkX, chunkZ)) continue;
                LevelChunk chunk = client.level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;
                analyse(client, chunk, module);
            }
            if (pending.isEmpty()) lastCycleTick = now;
        }

        double maxDistance = (radius + 1.0) * 16.0;
        double maxDistanceSq = maxDistance * maxDistance;
        double px = client.player.getX(), pz = client.player.getZ();
        List<ChunkResult> visible = new ArrayList<>();
        for (ChunkResult result : results.values()) {
            double dx = (result.chunkX() * 16.0 + 8.0) - px;
            double dz = (result.chunkZ() * 16.0 + 8.0) - pz;
            if (dx * dx + dz * dz <= maxDistanceSq) visible.add(result);
        }
        visible.sort(Comparator.comparingInt(ChunkResult::score).reversed());
        return List.copyOf(visible);
    }

    public void clear() {
        results.clear();
        pending.clear();
        queued.clear();
        notified.clear();
        levelIdentity = null;
        lastStepTick = Long.MIN_VALUE;
        lastCycleTick = Long.MIN_VALUE;
        centerChunkX = centerChunkZ = Integer.MIN_VALUE;
        lastRadius = -1;
        lastConfigHash = 0;
    }

    private void enqueueLoadedChunks(Minecraft client, int centerX, int centerZ, int radius) {
        List<long[]> candidates = new ArrayList<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (!client.level.hasChunk(x, z)) continue;
                candidates.add(new long[]{x, z, (long) (x - centerX) * (x - centerX) + (long) (z - centerZ) * (z - centerZ)});
            }
        }
        candidates.sort(Comparator.comparingLong(a -> a[2]));
        for (long[] candidate : candidates) {
            long key = pack((int) candidate[0], (int) candidate[1]);
            if (queued.add(key)) pending.addLast(key);
        }
    }

    private void analyse(Minecraft client, LevelChunk chunk, ChunkFinderModule module) {
        int storage = 0, redstone = 0, obsidian = 0, processedDeepslate = 0;
        int spawners = 0, oreBlocks = 0, verticalVeins = 0, concealed = 0;
        long yTotal = 0;
        int ySignals = 0;
        int minY = client.level.getMinY();
        int maxY = client.level.getMaxY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = chunk.getPos().getMinBlockX(); x < chunk.getPos().getMinBlockX() + 16; x++) {
            for (int z = chunk.getPos().getMinBlockZ(); z < chunk.getPos().getMinBlockZ() + 16; z++) {
                int currentOreRun = 0;
                for (int y = minY; y < maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = client.level.getBlockState(pos);
                    if (state.isAir()) { currentOreRun = 0; continue; }

                    boolean ore = isOre(state);
                    if (ore) {
                        oreBlocks++;
                        currentOreRun++;
                        if (module.detectConcealedOre() && isConcealedOre(client, pos)) concealed++;
                    } else {
                        if (module.detectVerticalVeins() && currentOreRun >= 3) verticalVeins++;
                        currentOreRun = 0;
                    }

                    if (module.detectStorage() && isStorage(state)) { storage++; yTotal += y; ySignals++; }
                    if (module.detectRedstone() && isRedstone(state)) { redstone++; yTotal += y; ySignals++; }
                    if (module.detectObsidian() && (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN))) { obsidian++; yTotal += y; ySignals++; }
                    if (module.detectDeepslate() && isProcessedDeepslate(state) && y < 64) { processedDeepslate++; yTotal += y; ySignals++; }
                    if (module.detectSpawners() && state.is(Blocks.SPAWNER)) { spawners++; yTotal += y; ySignals++; }
                }
                if (module.detectVerticalVeins() && currentOreRun >= 3) verticalVeins++;
            }
        }

        int score = 0;
        List<String> reasons = new ArrayList<>();
        if (module.detectStorage() && storage >= module.minimumStorage()) {
            int points = Math.min(45, (storage - module.minimumStorage() + 1) * 6);
            score += points; reasons.add("Storage " + storage);
        }
        if (module.detectRedstone() && redstone >= 3) {
            score += Math.min(30, redstone * 2); reasons.add("Redstone " + redstone);
        }
        if (module.detectObsidian() && obsidian >= 6) {
            score += Math.min(20, obsidian / 2); reasons.add("Obsidian " + obsidian);
        }
        if (module.detectDeepslate() && processedDeepslate >= 8) {
            score += Math.min(20, processedDeepslate / 8); reasons.add("Processed deepslate " + processedDeepslate);
        }
        if (module.detectSpawners() && spawners > 0) {
            score += Math.min(30, spawners * 15); reasons.add("Spawner " + spawners);
        }
        if (module.detectVerticalVeins() && verticalVeins > 0) {
            score += Math.min(24, verticalVeins * 8); reasons.add("Vertical ore veins " + verticalVeins);
        }
        if (module.detectConcealedOre() && concealed >= 2) {
            score += Math.min(16, concealed * 2); reasons.add("Concealed ore " + concealed);
        }

        int chunkX = chunk.getPos().getMinBlockX() >> 4;
        int chunkZ = chunk.getPos().getMinBlockZ() >> 4;
        long key = pack(chunkX, chunkZ);
        if (score < module.minimumScore()) {
            results.remove(key);
            notified.remove(key);
            return;
        }

        double centerY = ySignals == 0 ? client.player.getY() : (double) yTotal / ySignals;
        String signals = String.join(" · ", reasons);
        results.put(key, new ChunkResult(chunkX, chunkZ, score, centerY, signals));
        if (module.notifyDiscoveries() && notified.add(key)) {
            NotificationCenter.push("Chunk Finder", "Suspicious chunk " + chunkX + ", " + chunkZ + " — score " + score, 4000L,
                    NotificationCenter.Priority.NORMAL);
        }
    }

    private static boolean isStorage(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)
                || state.is(Blocks.HOPPER) || state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE)
                || state.is(Blocks.SMOKER) || state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER)
                || state.is(Blocks.SHULKER_BOX);
    }

    private static boolean isRedstone(BlockState state) {
        return state.is(Blocks.PISTON) || state.is(Blocks.STICKY_PISTON) || state.is(Blocks.PISTON_HEAD)
                || state.is(Blocks.OBSERVER) || state.is(Blocks.REPEATER) || state.is(Blocks.COMPARATOR)
                || state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.HOPPER) || state.is(Blocks.DISPENSER) || state.is(Blocks.DROPPER);
    }

    private static boolean isProcessedDeepslate(BlockState state) {
        return state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.POLISHED_DEEPSLATE)
                || state.is(Blocks.DEEPSLATE_BRICKS) || state.is(Blocks.DEEPSLATE_TILES)
                || state.is(Blocks.CHISELED_DEEPSLATE);
    }

    private static boolean isOre(BlockState state) {
        return state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    private static boolean isConcealedOre(Minecraft client, BlockPos pos) {
        int solid = 0;
        for (var direction : net.minecraft.core.Direction.values()) {
            if (!client.level.getBlockState(pos.relative(direction)).isAir()) solid++;
        }
        return solid >= 5;
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    private static int configHash(ChunkFinderModule module) {
        int hash = module.radiusChunks();
        hash = 31 * hash + module.minimumScore();
        hash = 31 * hash + module.minimumStorage();
        hash = 31 * hash + Boolean.hashCode(module.detectStorage());
        hash = 31 * hash + Boolean.hashCode(module.detectRedstone());
        hash = 31 * hash + Boolean.hashCode(module.detectObsidian());
        hash = 31 * hash + Boolean.hashCode(module.detectDeepslate());
        hash = 31 * hash + Boolean.hashCode(module.detectSpawners());
        hash = 31 * hash + Boolean.hashCode(module.detectVerticalVeins());
        hash = 31 * hash + Boolean.hashCode(module.detectConcealedOre());
        return hash;
    }
}
