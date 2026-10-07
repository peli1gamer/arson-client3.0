package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageScannerTest {
    @Test
    void unchangedLoadedChunksAreNotReindexedUntilFallbackRefresh() {
        assertEquals(true, StorageScanner.shouldIndexChunk(false, false));
        assertEquals(false, StorageScanner.shouldIndexChunk(true, false));
        assertEquals(true, StorageScanner.shouldIndexChunk(true, true));
    }

    @Test
    void parsesCustomBlockIdsWithDefaultMinecraftNamespaceAndNormalization() {
        assertEquals(java.util.Set.of("minecraft:chest", "mod:crate"),
                StorageScanner.parseCustomBlockIds(" Chest, mod:crate; chest "));
        assertEquals(java.util.Set.of(), StorageScanner.parseCustomBlockIds("  "));
    }

    @Test
    void clientRenderDistanceDefinesTheLoadedChunkSearchBoundary() {
        assertEquals(2, StorageScanner.effectiveChunkRadius(2));
        assertEquals(12, StorageScanner.effectiveChunkRadius(12));
        assertEquals(32, StorageScanner.effectiveChunkRadius(48));
        assertEquals(1, StorageScanner.effectiveChunkRadius(0));
    }
}
