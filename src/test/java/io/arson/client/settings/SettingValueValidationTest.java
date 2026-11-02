package io.arson.client.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingValueValidationTest {
    @Test
    void stringDefaultAndResetRespectMaximumLength() {
        StringSetting setting = new StringSetting("label", "Label", "abcdefgh", 4);
        assertEquals("abcd", setting.get());
        assertEquals("abcd", setting.defaultValue());

        setting.set("longer-than-limit");
        setting.reset();
        assertEquals("abcd", setting.get());
    }

    @Test
    void stringSettingRejectsNonPositiveMaximumLength() {
        assertThrows(IllegalArgumentException.class,
                () -> new StringSetting("label", "Label", "value", 0));
    }
}
