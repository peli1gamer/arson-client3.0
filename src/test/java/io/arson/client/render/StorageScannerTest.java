package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageScannerTest {
    @Test
    void chunkRadiusIsBoundedBySettingViewAndBlockRange() {
        assertEquals(2, StorageScanner.effectiveChunkRadius(8, 2, 128));
        assertEquals(1, StorageScanner.effectiveChunkRadius(8, 12, 8));
        assertEquals(3, StorageScanner.effectiveChunkRadius(8, 12, 40));
        assertEquals(4, StorageScanner.effectiveChunkRadius(4, 12, 128));
    }

    @Test
    void chunkRadiusClampsInvalidAndOutOfRangeInputs() {
        assertEquals(1, StorageScanner.effectiveChunkRadius(0, 0, Double.NaN));
        assertEquals(16, StorageScanner.effectiveChunkRadius(100, 48, 256));
    }
}