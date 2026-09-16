package io.arson.client;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.StringSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingDependencyTest {
    private enum Mode { BASIC, ADVANCED }

    @Test void booleanDependencyTracksLiveValue() {
        BooleanSetting master = new BooleanSetting("master", "Master", false);
        BooleanSetting child = new BooleanSetting("child", "Child", true);
        child.dependsOn(master);
        assertFalse(child.visible());
        master.set(true);
        assertTrue(child.visible());
        master.set(false);
        assertFalse(child.visible());
    }

    @Test void typedValueDependencyWorksForEnumAndString() {
        EnumSetting<Mode> mode = new EnumSetting<>("mode", "Mode", Mode.BASIC);
        StringSetting detail = new StringSetting("detail", "Detail", "compact");
        detail.dependsOnValue(mode, Mode.ADVANCED);
        assertFalse(detail.visible());
        mode.set(Mode.ADVANCED);
        assertTrue(detail.visible());
        mode.set(Mode.BASIC);
        assertFalse(detail.visible());
    }

    @Test void inverseBooleanAliasComposesWithExistingVisibility() {
        BooleanSetting master = new BooleanSetting("master", "Master", true);
        BooleanSetting child = new BooleanSetting("child", "Child", true);
        child.dependsOnNot(master);
        assertFalse(child.visible());
        master.set(false);
        assertTrue(child.visible());
        child.visibleWhen(() -> false);
        assertFalse(child.visible());
    }
}
