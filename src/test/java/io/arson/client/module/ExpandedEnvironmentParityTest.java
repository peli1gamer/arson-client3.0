package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpandedEnvironmentParityTest {
    @Test
    void newEnvironmentModulesExposeSafeDefaults() {
        WorldBorderInfoModule border = new WorldBorderInfoModule();
        WorldSpawnInfoModule spawn = new WorldSpawnInfoModule();
        RenderResolutionInfoModule resolution = new RenderResolutionInfoModule();

        assertEquals(0.0, border.diameter());
        assertEquals(0.0, border.distanceToEdge());
        assertEquals(0, spawn.spawnX());
        assertEquals(0.0, spawn.distance());
        assertEquals(0, resolution.framebufferWidth());
        assertEquals(0, resolution.guiHeight());
        assertEquals(0.0, resolution.aspectRatio());
        assertTrue(border.description().contains("world-border"));
        assertTrue(spawn.description().contains("shared-spawn"));
        assertTrue(resolution.description().contains("framebuffer"));
    }

    @Test
    void managerRegistersEnvironmentAndRenderModules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertEquals(Module.Category.WORLD, manager.get("world-border-info").category());
        assertEquals(Module.Category.WORLD, manager.get("world-spawn-info").category());
        assertEquals(Module.Category.RENDER, manager.get("render-resolution-info").category());
        assertTrue(manager.search("spawn").stream().anyMatch(m -> m.id().equals("world-spawn-info")));
        assertTrue(manager.search("aspect").stream().anyMatch(m -> m.id().equals("render-resolution-info")));
    }
}
