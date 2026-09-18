package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedParityTrancheTest {
    @Test
    void liveModulesExposeSafeDefaultsAndDescriptions() {
        PlayerPoseInfoModule pose = new PlayerPoseInfoModule();
        WorldLightInfoModule light = new WorldLightInfoModule();
        RenderTargetInfoModule target = new RenderTargetInfoModule();
        ClickGuiPreferencesModule gui = new ClickGuiPreferencesModule();

        assertEquals("standing", pose.pose());
        assertFalse(pose.swimming());
        assertEquals(0, light.blockLight());
        assertEquals(0, light.skyLight());
        assertEquals("none", target.targetType());
        assertEquals("No target", target.targetDetails());
        assertEquals(ClickGuiPreferencesModule.Theme.MIDNIGHT, gui.theme());
        assertEquals(ClickGuiPreferencesModule.SortMode.FAVORITES_FIRST, gui.sortMode());
        assertTrue(pose.description().contains("yaw"));
        assertTrue(light.description().contains("light"));
        assertTrue(target.description().contains("crosshair"));
        assertTrue(gui.description().contains("Persists"));
    }

    @Test
    void managerRegistersNewParityModules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertEquals(Module.Category.PLAYER, manager.get("player-pose-info").category());
        assertEquals(Module.Category.WORLD, manager.get("world-light-info").category());
        assertEquals(Module.Category.RENDER, manager.get("render-target-info").category());
        assertEquals(Module.Category.MISC, manager.get("clickgui-preferences").category());
        assertTrue(manager.search("light").stream().anyMatch(m -> m.id().equals("world-light-info")));
        assertTrue(manager.search("TARGET").stream().anyMatch(m -> m.id().equals("render-target-info")));
    }

    @Test
    void clickGuiPreferencesCycleAndFiltersArePersistentSettings() {
        ClickGuiPreferencesModule gui = new ClickGuiPreferencesModule();
        gui.cycleTheme();
        gui.cycleSortMode();
        gui.setFavoritesOnly(true);
        gui.setEnabledOnly(true);

        assertEquals(ClickGuiPreferencesModule.Theme.GRAPHITE, gui.theme());
        assertEquals(ClickGuiPreferencesModule.SortMode.ENABLED_FIRST, gui.sortMode());
        assertTrue(gui.favoritesOnly());
        assertTrue(gui.enabledOnly());

        gui.resetSettings();
        assertEquals(ClickGuiPreferencesModule.Theme.MIDNIGHT, gui.theme());
        assertEquals(ClickGuiPreferencesModule.SortMode.FAVORITES_FIRST, gui.sortMode());
        assertFalse(gui.favoritesOnly());
        assertFalse(gui.enabledOnly());
    }
}
