package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageScannerTest {
    @Test
    void clientRenderDistanceDefinesTheLoadedChunkSearchBoundary() {
        assertEquals(2, StorageScanner.effectiveChunkRadius(2));
        assertEquals(12, StorageScanner.effectiveChunkRadius(12));
        assertEquals(32, StorageScanner.effectiveChunkRadius(48));
        assertEquals(1, StorageScanner.effectiveChunkRadius(0));
    }
}
