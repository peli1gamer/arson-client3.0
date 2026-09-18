package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiveTelemetryModulesTest {
    @Test
    void experienceTelemetryHasRealDefaultsAndFormatting() {
        PlayerExperienceInfoModule module = new PlayerExperienceInfoModule();
        assertEquals("player-experience-info", module.id());
        assertEquals(Module.Category.PLAYER, module.category());
        assertEquals(0, module.level());
        assertEquals(0, module.totalExperience());
        assertEquals(0.0f, module.progress());
        assertTrue(module.formatted().contains("XP Level 0"));
        assertTrue(module.formatted().contains("Total 0"));
    }

    @Test
    void worldLightTelemetryHasRealDefaultsAndFormatting() {
        WorldLightInfoModule module = new WorldLightInfoModule();
        assertEquals("world-light-info", module.id());
        assertEquals(Module.Category.WORLD, module.category());
        assertEquals(0, module.blockLight());
        assertEquals(0, module.skyLight());
        assertTrue(module.formatted().contains("Light Block 0"));
        assertTrue(module.formatted().contains("Sky 0"));
    }

    @Test
    void newModulesAreRegisteredAndCategoryControlsPersistThroughApi() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertNotNull(manager.get("player-experience-info"));
        assertNotNull(manager.get("world-light-info"));
        assertTrue(manager.categoryCount(Module.Category.PLAYER) >= 1);
        assertTrue(manager.categoryCount(Module.Category.WORLD) >= 1);
    }
}
