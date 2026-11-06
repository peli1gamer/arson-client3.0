package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockScannerTest {
    @Test
    void refreshesAtConfiguredInterval() {
        assertFalse(BlockScanner.isRefreshDue(104, 100, 8));
        assertTrue(BlockScanner.isRefreshDue(108, 100, 8));
    }

    @Test
    void refreshesImmediatelyForUninitializedOrRewoundWorldTime() {
        assertTrue(BlockScanner.isRefreshDue(0, Long.MIN_VALUE, 8));
        assertTrue(BlockScanner.isRefreshDue(10, 12, 8));
    }

    @Test
    void clampsInvalidIntervalsToOneTick() {
        assertFalse(BlockScanner.isRefreshDue(10, 10, 0));
        assertTrue(BlockScanner.isRefreshDue(11, 10, 0));
    }
}
