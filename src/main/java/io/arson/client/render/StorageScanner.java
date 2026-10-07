package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Incremental storage index for chunks the client has already loaded. */
public final class StorageScanner {
    private static final long FALLBACK_REFRESH_TICKS = 100L;

    private Object indexedLevel;
    private long indexedTick = Long.MIN_VALUE;
    private int indexedChunkX = Integer.MIN_VALUE;
    private int indexedChunkZ = Integer.MIN_VALUE;
    private int indexedRadius = Integer.MIN_VALUE;
    private long revision;
    private final Map<Long, IndexedChunk> chunks = new HashMap<>();
    private Set<String> customBlockIds = Set.of();
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();

    public long revision() { return revision; }

    public void onChunkLoaded(ClientLevel level, LevelChunk chunk) {
        switchLevel(level);
        indexChunk(level, chunk);
        cachedTargets = flattenIndex();
        revision++;
    }

    public void onChunkUnloaded(ClientLevel level, LevelChunk chunk) {
        if (indexedLevel != level) return;
        if (chunks.remove(chunkKey(chunk.getPos().x, chunk.getPos().z)) != null) revision++;
        cachedTargets = flattenIndex();
    }

    /** Invalidates one loaded chunk so the next render scan refreshes its block entities. */
    public void invalidateBlockEntity(ClientLevel level, BlockPos pos) {
        if (indexedLevel != level) return;
        chunks.remove(chunkKey(pos.getX() >> 4, pos.getZ() >> 4));
        indexedTick = Long.MIN_VALUE;
        revision++;
        cachedTargets = flattenIndex();
    }

    public List<StorageOverlay.StorageTarget> scan(net.minecraft.client.Minecraft client, double range, String customBlocks) {
        if (client.level == null || client.player == null) {
            clear();
            return List.of();
        }

        ClientLevel level = client.level;
        switchLevel(level);
        Set<String> requestedCustomBlockIds = parseCustomBlockIds(customBlocks);
        boolean customFilterChanged = !customBlockIds.equals(requestedCustomBlockIds);
        if (customFilterChanged) customBlockIds = requestedCustomBlockIds;
        double clampedRange = Math.max(1.0, Math.min(256.0, range));
        int radius = effectiveChunkRadius(client.options.renderDistance().get());
        int chunkX = client.player.blockPosition().getX() >> 4;
        int chunkZ = client.player.blockPosition().getZ() >> 4;
        long gameTime = level.getGameTime();

        boolean chunkChanged = indexedChunkX != chunkX || indexedChunkZ != chunkZ;
        boolean radiusChanged = indexedRadius != radius;
        boolean refreshDue = gameTime - indexedTick >= FALLBACK_REFRESH_TICKS || gameTime < indexedTick;
        if (chunkChanged || radiusChanged || refreshDue || customFilterChanged) {
            refreshLoadedChunks(level, radius, chunkX, chunkZ, refreshDue || customFilterChanged);
            indexedTick = gameTime;
            indexedChunkX = chunkX;
            indexedChunkZ = chunkZ;
            indexedRadius = radius;
            cachedTargets = flattenIndex();
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
            if (dx * dx + dy * dy + dz * dz <= rangeSquared) visible.add(target);
        }
        return visible.isEmpty() ? List.of() : List.copyOf(visible);
    }

    /** Uses Minecraft's client render-distance setting as the scan boundary. */
    public static int effectiveChunkRadius(int clientRenderDistance) {
        return Math.max(1, Math.min(32, clientRenderDistance));
    }

    /** Parses comma, semicolon, or whitespace-separated block registry IDs. */
    public static Set<String> parseCustomBlockIds(String input) {
        if (input == null || input.isBlank()) return Set.of();
        Set<String> ids = new HashSet<>();
        for (String token : input.split("[,;\\s]+")) {
            String id = token.trim().toLowerCase(Locale.ROOT);
            if (id.isEmpty()) continue;
            if (!id.contains(":")) id = "minecraft:" + id;
            ids.add(id);
        }
        return Set.copyOf(ids);
    }

    private void switchLevel(Object level) {
        if (indexedLevel == level) return;
        chunks.clear();
        cachedTargets = List.of();
        indexedLevel = level;
        indexedTick = Long.MIN_VALUE;
        indexedChunkX = Integer.MIN_VALUE;
        indexedChunkZ = Integer.MIN_VALUE;
        indexedRadius = Integer.MIN_VALUE;
        revision++;
    }

    private void clear() {
        if (indexedLevel != null || !chunks.isEmpty()) revision++;
        indexedLevel = null;
        indexedTick = Long.MIN_VALUE;
        indexedChunkX = Integer.MIN_VALUE;
        indexedChunkZ = Integer.MIN_VALUE;
        indexedRadius = Integer.MIN_VALUE;
        chunks.clear();
        cachedTargets = List.of();
    }

    private void refreshLoadedChunks(ClientLevel level, int chunkRadius, int centerChunkX, int centerChunkZ,
                                     boolean forceRefresh) {
        Set<Long> seen = new HashSet<>();
        for (int x = centerChunkX - chunkRadius; x <= centerChunkX + chunkRadius; x++) {
            for (int z = centerChunkZ - chunkRadius; z <= centerChunkZ + chunkRadius; z++) {
                long key = chunkKey(x, z);
                if (!level.hasChunk(x, z)) {
                    if (chunks.remove(key) != null) revision++;
                    continue;
                }
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    if (chunks.remove(key) != null) revision++;
                    continue;
                }
                seen.add(key);
                if (shouldIndexChunk(chunks.containsKey(key), forceRefresh)) indexChunk(level, chunk);
            }
        }
        int before = chunks.size();
        chunks.keySet().retainAll(seen);
        if (chunks.size() != before) revision++;
    }

    private void indexChunk(ClientLevel level, LevelChunk chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        List<StorageOverlay.StorageTarget> targets = new ArrayList<>();
        Set<BlockPos> emittedChestPositions = new HashSet<>();
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockEntity entity = entry.getValue();
            StorageType type = classify(entity, customBlockIds);
            if (type == null) continue;
            BlockPos pos = entry.getKey();
            if (entity instanceof ChestBlockEntity) {
                if (emittedChestPositions.contains(pos)) continue;
                targets.add(buildChestTarget(level, pos, emittedChestPositions));
            } else {
                targets.add(new StorageOverlay.StorageTarget(type, pos.getX(), pos.getY(), pos.getZ(), 1.0, 1.0, 1.0));
            }
        }
        List<StorageOverlay.StorageTarget> indexedTargets = List.copyOf(targets);
        long key = chunkKey(chunkX, chunkZ);
        IndexedChunk previous = chunks.put(key, new IndexedChunk(chunkX, chunkZ, indexedTargets));
        if (previous == null || !previous.targets().equals(indexedTargets)) revision++;
    }

    private List<StorageOverlay.StorageTarget> flattenIndex() {
        Map<TargetKey, StorageOverlay.StorageTarget> unique = new LinkedHashMap<>();
        for (IndexedChunk chunk : chunks.values()) {
            for (StorageOverlay.StorageTarget target : chunk.targets()) {
                TargetKey key = new TargetKey(target.type(), (int) Math.floor(target.x()),
                        (int) Math.floor(target.y()), (int) Math.floor(target.z()));
                unique.putIfAbsent(key, target);
            }
        }
        return unique.isEmpty() ? List.of() : List.copyOf(unique.values());
    }

    public static boolean shouldIndexChunk(boolean alreadyIndexed, boolean forceRefresh) {
        return forceRefresh || !alreadyIndexed;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    private static StorageOverlay.StorageTarget buildChestTarget(ClientLevel level, BlockPos pos,
                                                                  Set<BlockPos> emittedPositions) {
        BlockPos[] neighbors = {pos.east(), pos.south(), pos.west(), pos.north()};
        BlockPos partner = null;
        for (BlockPos neighbor : neighbors) {
            if (level.getBlockEntity(neighbor) instanceof ChestBlockEntity) {
                partner = neighbor;
                break;
            }
        }
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
        return new StorageOverlay.StorageTarget(StorageType.CHEST, x, pos.getY(), z, width, 0.875, depth);
    }

    private static StorageType classify(BlockEntity entity, Set<String> customBlockIds) {
        if (entity == null) return null;
        if (entity instanceof ChestBlockEntity) return StorageType.CHEST;
        if (entity instanceof ShulkerBoxBlockEntity) return StorageType.SHULKER;
        if (entity.getBlockState().is(Blocks.BARREL)) return StorageType.BARREL;
        if (entity.getBlockState().is(Blocks.ENDER_CHEST)) return StorageType.ENDER_CHEST;
        if (entity instanceof HopperBlockEntity || entity instanceof DispenserBlockEntity
                || entity instanceof DropperBlockEntity) return StorageType.OTHER;
        String blockId = BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock()).toString();
        return customBlockIds.contains(blockId) ? StorageType.OTHER : null;
    }

    private record IndexedChunk(int x, int z, List<StorageOverlay.StorageTarget> targets) {}
    private record TargetKey(StorageType type, int x, int y, int z) {}
}