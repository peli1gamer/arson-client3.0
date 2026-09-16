package io.arson.client;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.SettingGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingGroupTest {
    @Test void groupRegistersSettingsAndControlsVisibility() {
        BooleanSetting enabled = new BooleanSetting("enabled", "Enabled", true);
        SettingGroup group = new SettingGroup("display", "Display", "Display options");
        enabled.group(group);
        assertSame(group, enabled.group());
        assertEquals(1, group.settings().size());
        assertTrue(enabled.visible());
        group.visibleWhen(() -> false);
        assertFalse(enabled.visible());
    }

    @Test void existingConditionalVisibilityStillComposesWithGroups() {
        BooleanSetting enabled = new BooleanSetting("enabled", "Enabled", true);
        SettingGroup group = new SettingGroup("display", "Display", "Display options");
        enabled.group(group).visibleWhen(() -> enabled.enabled());
        assertTrue(enabled.visible());
        enabled.set(false);
        assertFalse(enabled.visible());
        enabled.set(true);
        group.visibleWhen(() -> false);
        assertFalse(enabled.visible());
    }
}
