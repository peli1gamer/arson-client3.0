package io.arson.client;

import io.arson.client.module.HudModule;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryUiTrancheTest {
    @Test
    void newLiveTelemetryModulesAreRegisteredAndDiscoverable() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertNotNull(manager.get("player-interaction-info"));
        assertNotNull(manager.get("world-entity-count-info"));
        assertNotNull(manager.get("render-gui-info"));
        assertTrue(manager.search("interaction").stream().anyMatch(m -> m.id().equals("player-interaction-info")));
        assertTrue(manager.search("entity").stream().anyMatch(m -> m.id().equals("world-entity-count-info")));
        assertTrue(manager.search("gui scale").stream().anyMatch(m -> m.id().equals("render-gui-info")));
    }

    @Test
    void telemetryDescriptionsRemainConcreteAndLocal() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertTrue(manager.get("player-interaction-info").description().contains("crosshair"));
        assertTrue(manager.get("world-entity-count-info").description().contains("local world"));
        assertTrue(manager.get("render-gui-info").description().contains("GUI scale"));
    }

    @Test
    void hudRowFormatsRemainPersistentAndApiFacing() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        HudModule hud = (HudModule) manager.get("hud");
        assertEquals(HudModule.RowFormat.STACKED, hud.rowFormat());
        var setting = hud.settings().stream().filter(s -> s.id().equals("row-format")).findFirst().orElseThrow();
        ((io.arson.client.settings.EnumSetting<HudModule.RowFormat>) setting).set(HudModule.RowFormat.DENSE);
        assertEquals(HudModule.RowFormat.DENSE, hud.rowFormat());
        setting.reset();
        assertEquals(HudModule.RowFormat.STACKED, hud.rowFormat());
    }

    @Test
    void categoryResetRestoresModuleStateWithoutChangingCatalogue() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module sprint = manager.get("sprint");
        assertNotNull(sprint);
        int before = manager.categoryCount(Module.Category.MOVEMENT);
        sprint.setEnabled(true);
        sprint.setFavorite(true);
        sprint.setKeyCode(65);
        sprint.resetToDefaults();
        assertFalse(sprint.enabled());
        assertFalse(sprint.favorite());
        assertEquals(0, sprint.keyCode());
        assertEquals(before, manager.categoryCount(Module.Category.MOVEMENT));
    }
}
