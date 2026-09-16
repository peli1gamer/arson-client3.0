package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FeatureSurfaceContractTest {
    @Test
    void plannedFeatureFamiliesHaveConcreteModules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertNotNull(manager.get("aim-assist"));       // Combat
        assertNotNull(manager.get("entity-esp"));       // ESP
        assertNotNull(manager.get("container-esp"));    // Storage
        assertNotNull(manager.get("sprint"));            // Movement
        assertNotNull(manager.get("player-info"));       // Player
        assertNotNull(manager.get("world-info"));        // World
        assertNotNull(manager.get("client-info"));       // Utility
    }

    @Test
    void guiHudAndCombatRenderSurfacesAreRegistered() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertNotNull(manager.get("hud"));
        assertNotNull(manager.get("array-list"));
        assertNotNull(manager.get("combat-info"));
        assertNotNull(manager.get("aim-assist"));
        assertTrue(manager.categoryCount(Module.Category.RENDER) >= 6);
        assertTrue(manager.categoryCount(Module.Category.COMBAT) >= 5);
    }

    @Test
    void everyCoreCategoryContainsAUsableModule() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Set<Module.Category> categories = EnumSet.noneOf(Module.Category.class);
        for (Module module : manager.all()) categories.add(module.category());

        assertTrue(categories.contains(Module.Category.COMBAT));
        assertTrue(categories.contains(Module.Category.MOVEMENT));
        assertTrue(categories.contains(Module.Category.RENDER));
        assertTrue(categories.contains(Module.Category.PLAYER));
        assertTrue(categories.contains(Module.Category.WORLD));
        assertTrue(categories.contains(Module.Category.MISC));
    }
}
