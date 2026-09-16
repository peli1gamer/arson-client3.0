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
