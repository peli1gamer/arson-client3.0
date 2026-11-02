package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockESPEnvironmentCategoryTest {
    @Test
    void exposesOptionalDripstoneAndBeehiveCategoriesWithIndependentColors() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module module = manager.get("block-esp");

        assertNotNull(module);
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("dripstone")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("dripstone-color")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("beehives")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("beehive-color")));
    }
}
