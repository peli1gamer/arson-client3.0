package io.arson.client.module;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerDetectionModuleTest {
    @Test
    void hudRowsIncludeCountAndRespectDisplayLimit() {
        var players = List.of(
            new PlayerDetectionModule.PlayerSnapshot("Alex", 3.25, 18.5f),
            new PlayerDetectionModule.PlayerSnapshot("Sam", 7.0, 20.0f)
        );
        assertEquals(
            List.of("Nearby Players 2", "  Alex  3.3m  18.5 HP"),
            PlayerDetectionModule.formatSnapshots(players, 1)
        );
    }

    @Test
    void emptyOrMissingSnapshotsHaveAUsefulHudState() {
        assertEquals(
            List.of("Nearby Players 0", "  None"),
            PlayerDetectionModule.formatSnapshots(List.of(), 4)
        );
        assertEquals(
            List.of("Nearby Players 0", "  None"),
            PlayerDetectionModule.formatSnapshots(null, 4)
        );
    }

    @Test
    void displayLimitNeverRemovesTheCountOrAllRows() {
        var player = new PlayerDetectionModule.PlayerSnapshot("Alex", 1.0, 20.0f);
        assertEquals(
            List.of("Nearby Players 1", "  Alex  1.0m  20.0 HP"),
            PlayerDetectionModule.formatSnapshots(List.of(player), 0)
        );
    }
}
