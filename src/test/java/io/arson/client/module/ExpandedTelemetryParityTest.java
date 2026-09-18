package io.arson.client.module;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpandedTelemetryParityTest {
    @Test
    void newTelemetryModulesExposeStableInitialState() {
        PlayerAirInfoModule air = new PlayerAirInfoModule();
        WorldWeatherInfoModule weather = new WorldWeatherInfoModule();
        RenderFrameInfoModule frame = new RenderFrameInfoModule();

        assertEquals("player-air-info", air.id());
        assertEquals("world-weather-info", weather.id());
        assertEquals("render-frame-info", frame.id());
        assertEquals(Module.Category.PLAYER, air.category());
        assertEquals(Module.Category.WORLD, weather.category());
        assertEquals(Module.Category.RENDER, frame.category());
        assertEquals(0, air.air());
        assertEquals(0, air.maxAir());
        assertFalse(air.swimming());
        assertFalse(weather.raining());
        assertFalse(weather.thundering());
        assertEquals(0, frame.fps());
        assertEquals(0L, frame.frameTimeNanos());
    }

    @Test
    void managerRegistersExpandedTelemetryByCategory() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertNotNull(manager.get("player-air-info"));
        assertNotNull(manager.get("world-weather-info"));
        assertNotNull(manager.get("render-frame-info"));
        assertTrue(manager.categoryCount(Module.Category.PLAYER) >= 1);
        assertTrue(manager.categoryCount(Module.Category.WORLD) >= 1);
        assertTrue(manager.categoryCount(Module.Category.RENDER) >= 1);
    }

    @Test
    void clickGuiPreferencesExposePersistentDefaults() {
        ClickGuiPreferencesModule prefs = new ClickGuiPreferencesModule();
        assertEquals("clickgui-preferences", prefs.id());
        assertEquals(ClickGuiPreferencesModule.Theme.MIDNIGHT, prefs.theme());
        assertFalse(prefs.alphabetical());
        assertFalse(prefs.favoritesOnly());
        assertFalse(prefs.enabledOnly());
        assertEquals(1.0, prefs.panelScale());
        prefs.cycleTheme();
        prefs.setAlphabetical(true);
        prefs.setFavoritesOnly(true);
        prefs.setEnabledOnly(true);
        assertEquals(ClickGuiPreferencesModule.Theme.GRAPHITE, prefs.theme());
        assertTrue(prefs.alphabetical());
        assertTrue(prefs.favoritesOnly());
        assertTrue(prefs.enabledOnly());
    }

    @Test
    void managerRegistersClickGuiPreferencesInMisc() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module module = manager.get("clickgui-preferences");
        assertNotNull(module);
        assertEquals(Module.Category.MISC, module.category());
    }

    @Test
    void formattedTelemetryIsHumanReadable() {
        assertTrue(new PlayerAirInfoModule().formatted().startsWith("Air "));
        assertTrue(new WorldWeatherInfoModule().formatted().startsWith("Weather "));
        assertTrue(new RenderFrameInfoModule().formatted().startsWith("Frame "));
    }
}
