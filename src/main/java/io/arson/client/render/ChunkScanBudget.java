package io.arson.client.render;

import java.util.Objects;
import java.util.function.LongSupplier;

/** Per-tick time limit for incremental, optional chunk analysis. */
final class ChunkScanBudget {
    private final LongSupplier clock;
    private long startedAt;
    private long limitNanos = Long.MAX_VALUE;

    ChunkScanBudget(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    void reset(long limitNanos) {
        this.startedAt = clock.getAsLong();
        this.limitNanos = Math.max(0L, limitNanos);
    }

    /** Always permits one chunk so a tiny budget cannot permanently starve the queue. */
    boolean canProcess(int processed) {
        if (processed == 0 || limitNanos == Long.MAX_VALUE) return true;
        return elapsedNanos() < limitNanos;
    }

    long usedNanos() {
        return elapsedNanos();
    }

    private long elapsedNanos() {
        return Math.max(0L, clock.getAsLong() - startedAt);
    }
}
