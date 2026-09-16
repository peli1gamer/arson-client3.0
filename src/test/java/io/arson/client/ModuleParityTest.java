package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleParityTest {
    @Test
    void moduleSurfaceHasMeteorStyleCoreCategoriesAndUsefulDiscovery() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertTrue(manager.categoryCount(Module.Category.COMBAT) > 0);
        assertTrue(manager.categoryCount(Module.Category.MOVEMENT) > 0);
        assertTrue(manager.categoryCount(Module.Category.RENDER) > 0);
        assertTrue(manager.categoryCount(Module.Category.PLAYER) > 0);
        assertTrue(manager.categoryCount(Module.Category.WORLD) > 0);
        assertTrue(manager.categoryCount(Module.Category.MISC) > 0);
        assertNotNull(manager.get("sprint"));
        assertNotNull(manager.get("hud"));
        assertNotNull(manager.get("entity-esp"));
    }

    @Test
    void moduleKeybindIsMutableAndResettableForGuiCustomization() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module sprint = manager.get("sprint");

        assertNotNull(sprint);
        assertFalse(sprint.hasKeybind());
        sprint.setKeyCode(65);
        assertTrue(sprint.hasKeybind());
        assertEquals(65, sprint.keyCode());
        sprint.resetToDefaults();
        assertEquals(0, sprint.keyCode());
        assertFalse(sprint.enabled());
    }

    @Test
    void organizedCategoriesAreStableAndPutEnabledModulesFirst() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module sprint = manager.get("sprint");
        assertNotNull(sprint);
        sprint.setEnabled(true);

        Module first = manager.organized(Module.Category.MOVEMENT).iterator().next();
        assertEquals("sprint", first.id());
    }
}
