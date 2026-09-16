package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderInfoModuleTest {
    @Test
    void exposesRenderTelemetryMetadataAndSafeInitialState() {
        RenderInfoModule module = new RenderInfoModule();
        assertEquals("render-info", module.id());
        assertEquals(Module.Category.RENDER, module.category());
        assertTrue(module.description().contains("viewport"));
        assertEquals(0, module.width());
        assertEquals(0, module.height());
        assertEquals(0, module.guiScale());
        assertFalse(module.fullscreen());
        assertTrue(module.formatted().contains("Render 0x0"));
    }
}
