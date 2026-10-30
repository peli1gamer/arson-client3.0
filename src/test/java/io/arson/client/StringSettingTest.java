package io.arson.client;

import io.arson.client.settings.StringSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringSettingTest {
    @Test
    void truncatesDefaultValueToConfiguredLimit() {
        StringSetting setting = new StringSetting("label", "Label", "abcdef", 4);
        assertEquals("abcd", setting.get());
    }

    @Test
    void resetKeepsConfiguredLengthLimit() {
        StringSetting setting = new StringSetting("label", "Label", "abcdef", 4);
        setting.set("xy");
        setting.reset();
        assertEquals("abcd", setting.get());
    }

    @Test
    void truncatesValuesAssignedAfterConstruction() {
        StringSetting setting = new StringSetting("label", "Label", "ok", 3);
        setting.set("longer");
        assertEquals("lon", setting.get());
    }
}
