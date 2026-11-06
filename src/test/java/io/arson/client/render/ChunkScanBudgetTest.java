package io.arson.client.render;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkScanBudgetTest {
    @Test
    void budgetStopsAdditionalChunksAfterDeadline() {
        AtomicLong clock = new AtomicLong(100);
        ChunkScanBudget budget = new ChunkScanBudget(clock::get);
        budget.reset(10);

        assertTrue(budget.canProcess(0));
        clock.addAndGet(9);
        assertTrue(budget.canProcess(1));
        clock.incrementAndGet();
        assertFalse(budget.canProcess(1));
        assertEquals(10, budget.usedNanos());
    }

    @Test
    void atLeastOneChunkCanRunEvenWhenBudgetIsZero() {
        AtomicLong clock = new AtomicLong();
        ChunkScanBudget budget = new ChunkScanBudget(clock::get);
        budget.reset(0);

        assertTrue(budget.canProcess(0));
        assertFalse(budget.canProcess(1));
    }

    @Test
    void unlimitedBudgetDoesNotStopQueuedWork() {
        AtomicLong clock = new AtomicLong();
        ChunkScanBudget budget = new ChunkScanBudget(clock::get);
        budget.reset(Long.MAX_VALUE);
        clock.set(Long.MAX_VALUE / 2);

        assertTrue(budget.canProcess(1));
    }
}
