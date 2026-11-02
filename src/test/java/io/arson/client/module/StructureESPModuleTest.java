package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureESPModuleTest {
    @Test
    void registersTunnelAndStaircaseControlsWithIndependentStyles() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module module = manager.get("structure-esp");

        assertNotNull(module);
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("tunnels")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("staircases")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("tunnel-color")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("staircase-color")));
    }
}
