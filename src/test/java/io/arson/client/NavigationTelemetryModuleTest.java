package io.arson.client;

import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NavigationTelemetryModuleTest {
    @Test
    void navigationAndServerTelemetryModulesAreRegistered() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        var detection = manager.get("player-detection");
        var chunk = manager.get("chunk-position");
        var server = manager.get("server-info");

        assertAll(
            () -> assertNotNull(detection),
            () -> assertEquals("Render", detection.category().displayName()),
            () -> assertTrue(detection.description().contains("nearby players")),
            () -> assertTrue(detection.settings().stream().anyMatch(s -> s.id().equals("range"))),
            () -> assertNotNull(chunk),
            () -> assertEquals("World", chunk.category().displayName()),
            () -> assertTrue(chunk.description().contains("chunk")),
            () -> assertNotNull(server),
            () -> assertEquals("Misc", server.category().displayName()),
            () -> assertTrue(server.description().contains("server"))
        );
    }

    @Test
    void chunkFormattingStartsFromStableDefaults() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        var chunk = (io.arson.client.module.ChunkPositionModule) manager.get("chunk-position");

        assertEquals(0, chunk.chunkX());
        assertEquals(0, chunk.chunkZ());
        assertEquals(0, chunk.regionX());
        assertEquals(0, chunk.regionZ());
        assertEquals("Chunk 0 0 • Region 0 0", chunk.formatted());
    }

    @Test
    void serverFormattingStartsInSingleplayerState() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        var server = (io.arson.client.module.ServerInfoModule) manager.get("server-info");

        assertFalse(server.multiplayer());
        assertFalse(server.lan());
        assertEquals("Singleplayer", server.serverName());
        assertEquals("Singleplayer", server.formatted());
    }
}
