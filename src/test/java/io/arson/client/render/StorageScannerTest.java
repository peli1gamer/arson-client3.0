package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageScannerTest {
    @Test
    void refreshesAtConfiguredStorageInterval() {
        assertFalse(StorageScanner.isRefreshDue(104, 100, 5));
        assertTrue(StorageScanner.isRefreshDue(105, 100, 5));
    }

    @Test
    void refreshesImmediatelyWhenUninitializedOrWorldTimeRewinds() {
        assertTrue(StorageScanner.isRefreshDue(0, Long.MIN_VALUE, 5));
        assertTrue(StorageScanner.isRefreshDue(10, 12, 5));
    }

    @Test
    void clampsZeroOrNegativeIntervalsToOneTick() {
        assertFalse(StorageScanner.isRefreshDue(10, 10, 0));
        assertTrue(StorageScanner.isRefreshDue(11, 10, 0));
        assertTrue(StorageScanner.isRefreshDue(11, 10, -4));
    }
}