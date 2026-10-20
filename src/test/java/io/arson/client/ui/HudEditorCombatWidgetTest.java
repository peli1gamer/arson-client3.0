package io.arson.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudEditorCombatWidgetTest {
    @Test
    void keyboardNudgeDistanceRespectsGridAndCoarseStep() {
        assertEquals(1.0, HudEditorScreen.keyboardNudgeDistance(false, 16.0, false));
        assertEquals(16.0, HudEditorScreen.keyboardNudgeDistance(true, 16.0, false));
        assertEquals(160.0, HudEditorScreen.keyboardNudgeDistance(true, 16.0, true));
        assertEquals(10.0, HudEditorScreen.keyboardNudgeDistance(false, 16.0, true));
        assertEquals(1.0, HudEditorScreen.keyboardNudgeDistance(true, Double.NaN, false));
        assertEquals(1.0, HudEditorScreen.keyboardNudgeDistance(true, 0.0, false));
    }

    @Test
    void combatInfoParticipatesInCrossElementClipboardCompatibility() {
        assertTrue(HudEditorScreen.HudClipboardCompatible.isCompatible("combat-info", "player-info"));
        assertTrue(HudEditorScreen.HudClipboardCompatible.isCompatible("watermark", "combat-info"));
        assertFalse(HudEditorScreen.HudClipboardCompatible.isCompatible("array-list", "combat-info"));
        assertFalse(HudEditorScreen.HudClipboardCompatible.isCompatible("combat-info", "array-list"));
    }
}
