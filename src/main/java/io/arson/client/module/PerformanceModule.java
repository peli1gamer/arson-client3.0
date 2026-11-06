package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import java.util.Locale;

/**
 * Controls optional Chunk Finder analysis scheduling and exposes its live work
 * metrics to the HUD. It does not alter rendering, input, or network behavior.
 */
public final class PerformanceModule extends Module {
    private final DoubleSetting tickBudget = setting(new DoubleSetting(
            "tick-budget", "Chunk Scan Budget (ms)", 1.0, 0.1, 5.0, 0.1));
    private final BooleanSetting showStats = setting(new BooleanSetting(
            "show-stats", "Show Stats", true));

    public PerformanceModule() {
        super("performance", "Performance", Category.MISC,
                "Budgets optional Chunk Finder analysis and reports live scan timing and queue depth.");
        tickBudget.description("Maximum time Chunk Finder may spend analysing chunks in one game tick. "
                + "At least one queued chunk is processed to prevent starvation.");
        showStats.description("Show the latest Chunk Finder scan time, processed chunk count, and pending queue "
                + "in the World Info HUD when that HUD element is enabled.");
    }

    public double tickBudget() {
        return tickBudget.get();
    }

    public long scanBudgetNanos() {
        return Math.max(0L, Math.round(tickBudget() * 1_000_000.0));
    }

    public boolean showStats() {
        return showStats.enabled();
    }

    public String formattedStats(long scanNanos, int processed, int pending) {
        double millis = Math.max(0L, scanNanos) / 1_000_000.0;
        return String.format(Locale.ROOT, "Chunk scan %.2f ms · %d processed · %d queued",
                millis, Math.max(0, processed), Math.max(0, pending));
    }
}
