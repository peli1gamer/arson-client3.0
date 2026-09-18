package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedLiveTelemetryTest {
    @Test
    void expandedModulesExposeSafeInitialState() {
        PlayerMovementInfoModule movement = new PlayerMovementInfoModule();
        WorldPositionInfoModule world = new WorldPositionInfoModule();
        RenderDisplayInfoModule display = new RenderDisplayInfoModule();

        assertEquals("player-movement-info", movement.id());
        assertEquals("world-position-info", world.id());
        assertEquals("render-display-info", display.id());
        assertTrue(movement.description().contains("velocity"));
        assertTrue(world.description().contains("world border"));
        assertTrue(display.description().contains("FOV"));
        assertEquals(0.0, movement.horizontalSpeed());
        assertFalse(movement.onGround());
        assertEquals(0, world.minBuildHeight());
        assertEquals(0.0, world.borderDiameter());
        assertEquals(0.0, display.fov());
        assertFalse(display.fullscreen());
    }

    @Test
    void managerRegistersExpandedModulesInExpectedCategories() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertEquals(Module.Category.PLAYER, manager.get("player-movement-info").category());
        assertEquals(Module.Category.WORLD, manager.get("world-position-info").category());
        assertEquals(Module.Category.RENDER, manager.get("render-display-info").category());
        assertTrue(manager.categoryCount(Module.Category.PLAYER) >= 1);
        assertTrue(manager.categoryCount(Module.Category.WORLD) >= 1);
        assertTrue(manager.categoryCount(Module.Category.RENDER) >= 1);
    }
}
