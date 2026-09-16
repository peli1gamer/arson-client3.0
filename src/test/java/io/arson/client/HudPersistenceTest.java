package io.arson.client;

import io.arson.client.module.HudModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudPersistenceTest {
    @Test
    void editorPositionAndVisibilityStateRemainOnTheModuleAcrossEditorRebuilds() {
        HudModule hud = new HudModule();
        hud.setEditorPosition("watermark", 37, 49);
        hud.setElementVisible("watermark", false);
        hud.adjustElementScale("watermark", 0.25);
        assertEquals(36, hud.x());
        assertEquals(48, hud.y());
        assertFalse(hud.elementVisible("watermark"));
        assertEquals(1.25, hud.elementScale("watermark"), 0.0001);

        hud.setGridSnap(false);
        hud.setEditorPosition("watermark", 37, 49);
        assertEquals(37, hud.x());
        assertEquals(49, hud.y());
    }

    @Test
    void hudResetRestoresDefaultEditorPositionAndPresetState() {
        HudModule hud = new HudModule();
        hud.setEditorPosition("fps", 300, 300);
        hud.setElementVisible("fps", false);
        hud.resetElement("fps");
        assertEquals(6, hud.fpsX());
        assertEquals(39, hud.fpsY());

        hud.applyPreset("minimal");
        assertTrue(hud.showWatermark());
        assertFalse(hud.showCoordinates());
        assertTrue(hud.showFps());
        assertFalse(hud.showPlayerInfo());
        assertFalse(hud.showWorldInfo());
    }
}
