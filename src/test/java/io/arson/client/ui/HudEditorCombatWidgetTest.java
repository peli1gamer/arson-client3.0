package io.arson.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudEditorCombatWidgetTest {
    @Test
    void combatInfoParticipatesInCrossElementClipboardCompatibility() {
        assertTrue(HudEditorScreen.HudClipboardCompatible.isCompatible("combat-info", "player-info"));
        assertTrue(HudEditorScreen.HudClipboardCompatible.isCompatible("watermark", "combat-info"));
        assertFalse(HudEditorScreen.HudClipboardCompatible.isCompatible("array-list", "combat-info"));
        assertFalse(HudEditorScreen.HudClipboardCompatible.isCompatible("combat-info", "array-list"));
    }
}
