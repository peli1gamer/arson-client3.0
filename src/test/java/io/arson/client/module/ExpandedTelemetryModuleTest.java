package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedTelemetryModuleTest {
    @Test
    void newLiveTelemetryModulesExposeStableMetadataAndSafeInitialState() {
        PlayerEquipmentModule equipment = new PlayerEquipmentModule();
        CameraInfoModule camera = new CameraInfoModule();
        WorldDetailsModule world = new WorldDetailsModule();
        assertEquals("player-equipment", equipment.id());
        assertEquals("camera-info", camera.id());
        assertEquals("world-details", world.id());
        assertTrue(equipment.description().contains("armor"));
        assertTrue(camera.description().contains("camera"));
        assertTrue(world.description().contains("sea level"));
        assertEquals(0, equipment.equippedArmor());
        assertEquals("Empty", equipment.mainHand());
        assertEquals("FIRST_PERSON", camera.perspective());
        assertEquals(0, world.seaLevel());
        assertEquals("unknown", world.levelName());
    }

    @Test
    void managerRegistersExpandedTelemetry() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertNotNull(manager.get("player-equipment"));
        assertNotNull(manager.get("camera-info"));
        assertNotNull(manager.get("world-details"));
        assertEquals(Module.Category.PLAYER, manager.get("player-equipment").category());
        assertEquals(Module.Category.RENDER, manager.get("camera-info").category());
        assertEquals(Module.Category.WORLD, manager.get("world-details").category());
    }
}
