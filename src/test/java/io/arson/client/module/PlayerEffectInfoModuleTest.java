package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerEffectInfoModuleTest {
    @Test
    void formatsEmptyEffectsWithoutStaleSummary() {
        assertEquals("Effects 0", PlayerEffectInfoModule.formatSummary(0, 0, 0, "", 0));
    }

    @Test
    void formatsEffectCountsAndFirstRemainingDuration() {
        assertEquals("Effects 3  2 beneficial  1 harmful  Speed 12s",
                PlayerEffectInfoModule.formatSummary(3, 2, 1, "Speed", 12));
    }

    @Test
    void clampsInvalidTelemetryValues() {
        assertEquals("Effects 1  0 beneficial  0 harmful  Effect 0s",
                PlayerEffectInfoModule.formatSummary(1, -2, -1, " ", -5));
    }
}
