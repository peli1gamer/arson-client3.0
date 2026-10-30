package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockScannerTest {
    @Test
    void firstScanRunsWithoutWaitingForInterval() {
        assertTrue(BlockScanner.shouldRefresh(0, Long.MIN_VALUE, 8));
    }

    @Test
    void refreshesAtConfiguredInterval() {
        assertFalse(BlockScanner.shouldRefresh(107, 100, 8));
        assertTrue(BlockScanner.shouldRefresh(108, 100, 8));
    }

    @Test
    void refreshesAfterWorldTimeMovesBackwards() {
        assertTrue(BlockScanner.shouldRefresh(3, 100, 8));
    }

    @Test
    void clampsInvalidIntervalsToOneTick() {
        assertTrue(BlockScanner.shouldRefresh(101, 100, 0));
        assertFalse(BlockScanner.shouldRefresh(100, 100, -5));
    }
}
