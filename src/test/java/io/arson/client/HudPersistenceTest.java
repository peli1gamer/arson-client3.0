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
    void rowFormattingAndAlignmentArePersistentSettings() {
        HudModule hud = new HudModule();
        assertEquals(HudModule.RowFormat.STACKED, hud.rowFormat());
        var format = hud.settings().stream().filter(s -> s.id().equals("row-format")).findFirst().orElseThrow();
        assertEquals(HudModule.RowFormat.STACKED, format.get());
        hud.cycleAlignment("player-info");
        assertEquals("center", hud.elementAlignment("player-info"));
        hud.cycleAlignment("player-info");
        assertEquals("right", hud.elementAlignment("player-info"));
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

    class HudWidgetPositionTest {
@Test
    void widgetPositionsAreReadableAndRespectSnapAndNonNegativeBounds() {
        HudModule hud = new HudModule();
        hud.setEditorPosition("player-info", 31, 43);
        assertEquals(32, hud.elementX("player-info"));
        assertEquals(44, hud.elementY("player-info"));

        hud.setGridSnap(false);
        hud.setEditorPosition("world-info", -5, 17);
        assertEquals(0, hud.elementX("world-info"));
        assertEquals(17, hud.elementY("world-info"));
        assertThrows(IllegalArgumentException.class, () -> hud.elementX("unknown"));
    }
}
