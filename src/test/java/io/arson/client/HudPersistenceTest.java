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
    void perElementBackgroundWorksWithoutGlobalBackground() {
        HudModule hud = new HudModule();
        var local = (io.arson.client.settings.BooleanSetting) hud.settings().stream()
                .filter(setting -> setting.id().equals("fps-background")).findFirst().orElseThrow();
        var global = (io.arson.client.settings.BooleanSetting) hud.settings().stream()
                .filter(setting -> setting.id().equals("background")).findFirst().orElseThrow();

        assertFalse(hud.elementBackgroundVisible("fps"));
        local.set(true);
        assertTrue(hud.elementBackgroundVisible("fps"));
        local.set(false);
        global.set(true);
        assertTrue(hud.elementBackgroundVisible("fps"));
    }

    @Test
    void globalBackgroundColorIsUsedWhenGlobalBackgroundIsEnabled() {
        HudModule hud = new HudModule();
        var globalBackground = (io.arson.client.settings.BooleanSetting) hud.settings().stream()
                .filter(setting -> setting.id().equals("background")).findFirst().orElseThrow();
        var globalColor = (io.arson.client.settings.ColorSetting) hud.settings().stream()
                .filter(setting -> setting.id().equals("background-color")).findFirst().orElseThrow();
        var elementColor = (io.arson.client.settings.ColorSetting) hud.settings().stream()
                .filter(setting -> setting.id().equals("element-background-color")).findFirst().orElseThrow();

        globalColor.set(0xFF112233);
        elementColor.set(0xFF445566);
        assertEquals(0xFF445566, hud.elementBackgroundColor());
        globalBackground.set(true);
        assertEquals(0xFF112233, hud.elementBackgroundColor());
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
