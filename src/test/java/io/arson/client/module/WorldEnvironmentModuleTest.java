package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldEnvironmentModuleTest {
    @Test
    void exposesPersistentWorldTelemetryTogglesAndSafeInitialState() {
        WorldEnvironmentModule module = new WorldEnvironmentModule();
        assertEquals("world-environment", module.id());
        assertEquals(Module.Category.WORLD, module.category());
        assertTrue(module.showWeather());
        assertTrue(module.showDifficulty());
        assertTrue(module.showChunk());
        assertTrue(module.showLight());
        assertEquals("unknown", module.dimension());
        assertEquals("unknown", module.biome());
        assertEquals("unknown", module.weather());
        assertEquals("unknown", module.difficulty());
        assertEquals(0, module.blockLight());
        assertEquals(4, module.settings().stream().filter(s -> s instanceof BooleanSetting).count());
    }
}
