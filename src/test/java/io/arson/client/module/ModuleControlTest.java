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
    
    @Test
    void enableToggleOnlyChangesKnownModules() {
        ModuleManager manager = modules();
        Module module = manager.get("hud");

        assertFalse(ModuleControl.toggleEnabled(manager, "missing-module"));
        assertTrue(ModuleControl.toggleEnabled(manager, "hud"));
        assertTrue(module.enabled());
        assertTrue(ModuleControl.toggleEnabled(manager, "hud"));
        assertFalse(module.enabled());
    }

    @Test
    void snapshotExposesTypedStateAndRepresentsClearedKeybind() {
        ModuleManager manager = modules();
        Module module = manager.get("hud");

        var initial = ModuleControl.snapshot(manager, "hud").orElseThrow();
        assertEquals("hud", initial.id());
        assertFalse(initial.enabled());
        assertFalse(initial.favorite());
        assertEquals(0, initial.keyCode());
        assertFalse(initial.hasKeybind());
        assertEquals(module.settings().size(), initial.settingCount());
        assertEquals(java.util.Optional.empty(), ModuleControl.snapshot(manager, "unknown"));
        
        assertTrue(ModuleControl.setKeyCode(manager, "hud", 75));
        var bound = ModuleControl.snapshot(manager, "hud").orElseThrow();
        assertTrue(bound.hasKeybind());
        assertEquals(75, bound.keyCode());
        assertTrue(bound.formatted().contains("keybind=75"));
    }

    @Test
    void statusReportsCurrentStateWithoutMutatingIt() {
        ModuleManager manager = modules();
        Module module = manager.get("hud");

        assertEquals(java.util.Optional.of("hud enabled=false favorite=false keybind=none settings=" + module.settings().size()),
                ModuleControl.status(manager, "hud"));
        assertEquals(java.util.Optional.empty(), ModuleControl.status(manager, "missing-module"));
        assertTrue(ModuleControl.setKeyCode(manager, "hud", 70));
        assertTrue(ModuleControl.setFavorite(manager, "hud", true));
        assertEquals(java.util.Optional.of("hud enabled=false favorite=true keybind=70 settings=" + module.settings().size()),
                ModuleControl.status(manager, "hud"));
    }
}
