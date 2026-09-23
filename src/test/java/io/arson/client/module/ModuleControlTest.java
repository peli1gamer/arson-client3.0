package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleControlTest {
    private ModuleManager modules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        return manager;
    }

    @Test
    void favoriteOperationsSetAndToggleOnlyKnownModules() {
        ModuleManager manager = modules();
        Module module = manager.get("hud");

        assertFalse(ModuleControl.toggleFavorite(manager, "missing-module"));
        assertTrue(ModuleControl.setFavorite(manager, "hud", true));
        assertTrue(module.favorite());
        assertTrue(ModuleControl.toggleFavorite(manager, "hud"));
        assertFalse(module.favorite());
        assertTrue(ModuleControl.toggleFavorite(manager, " hud "));
        assertTrue(module.favorite());
    }

    @Test
    void keybindOperationValidatesAndSupportsClearing() {
        ModuleManager manager = modules();
        Module module = manager.get("hud");

        assertFalse(ModuleControl.setKeyCode(manager, "missing-module", 65));
        assertFalse(ModuleControl.setKeyCode(manager, "hud", -1));
        assertTrue(ModuleControl.setKeyCode(manager, "hud", 65));
        assertEquals(65, module.keyCode());
        assertTrue(ModuleControl.setKeyCode(manager, "hud", 0));
        assertFalse(module.hasKeybind());
    }
}
