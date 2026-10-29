package io.arson.client;

import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleCatalogueTest {
    @Test
    void hudLayoutIsAConcreteRenderModuleWithPersistentSettings() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        var module = manager.get("hud-layout");
        assertNotNull(module);
        assertEquals("Render", module.category().displayName());
        assertTrue(module.description().contains("anchors"));
        assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("watermark-anchor")));
        assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("clamp")));
    }

    @Test
    void worldEffectsExposePersistentMovementVisualControls() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        var module = manager.get("world-effects");
        assertNotNull(module);
        assertEquals("Render", module.category().displayName());
        assertTrue(module.description().contains("no packets"));
        assertAll(
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("landing-ripple"))),
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("movement-trail"))),
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("effect-color"))),
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("ripple-radius"))),
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("lifetime"))),
            () -> assertTrue(module.settings().stream().anyMatch(s -> s.id().equals("trail-interval")))
        );
    }


    @Test
    void customSkyExposesShaderControls() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        var module = manager.get("custom-sky");
        assertNotNull(module);
        assertEquals("Render", module.category().displayName());
        assertTrue(module.description().contains("shader tint"));
        assertAll(
            () -> assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("custom-sky"))),
            () -> assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("sky-tint")))
        );
    }

    @Test
    void coreFeatureFamiliesRemainDiscoverableAsConcreteModules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertAll(
            () -> assertNotNull(manager.get("combat-status")),
            () -> assertNotNull(manager.get("player-status")),
            () -> assertNotNull(manager.get("movement-status")),
            () -> assertNotNull(manager.get("render-profile")),
            () -> assertNotNull(manager.get("world-status")),
            () -> assertNotNull(manager.get("storage-status")),
            () -> assertNotNull(manager.get("client-info"))
        );
    }
}
