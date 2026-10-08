package io.arson.client.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingValueParserTest {
    private enum Detail { COMPACT, DETAILED }

    @Test
    void booleanParsingIsStrictAndCaseInsensitive() {
        BooleanSetting setting = new BooleanSetting("enabled", "Enabled", false);
        assertTrue(SettingValueParser.apply(setting, "TRUE"));
        assertTrue(setting.enabled());
        assertFalse(SettingValueParser.apply(setting, "yes"));
        assertTrue(setting.enabled());
        assertTrue(SettingValueParser.apply(setting, "false"));
        assertFalse(setting.enabled());
    }

    @Test
    void numericValuesUseTheSettingClampAndRejectNonFiniteInput() {
        DoubleSetting setting = new DoubleSetting("scale", "Scale", 1.0, 0.0, 10.0, 0.5);
        assertTrue(SettingValueParser.apply(setting, "20"));
        assertEquals(10.0, setting.get());
        assertFalse(SettingValueParser.apply(setting, "NaN"));
        assertEquals(10.0, setting.get());
        assertTrue(SettingValueParser.apply(setting, "2.2"));
        assertEquals(2.0, setting.get());
    }

    @Test
    void enumValuesAreMatchedWithoutCaseSensitivity() {
        EnumSetting<Detail> setting = new EnumSetting<>("detail", "Detail", Detail.COMPACT);
        assertTrue(SettingValueParser.apply(setting, "detailed"));
        assertEquals(Detail.DETAILED, setting.get());
        assertFalse(SettingValueParser.apply(setting, "unknown"));
        assertEquals(Detail.DETAILED, setting.get());
    }

    @Test
    void colorsAcceptRgbAndArgbAndRejectMalformedInput() {
        ColorSetting setting = new ColorSetting("color", "Color", 0);
        assertTrue(SettingValueParser.apply(setting, "#123456"));
        assertEquals(0xFF123456, setting.get());
        assertTrue(SettingValueParser.apply(setting, "0x80123456"));
        assertEquals(0x80123456, setting.get());
        assertFalse(SettingValueParser.apply(setting, "#xyz"));
        assertEquals(0x80123456, setting.get());
    }

    @Test
    void stringsKeepSpacesAndRejectValuesThatWouldBeTruncated() {
        StringSetting setting = new StringSetting("label", "Label", "initial", 16);
        assertTrue(SettingValueParser.apply(setting, "hello world"));
        assertEquals("hello world", setting.get());
        assertFalse(SettingValueParser.apply(setting, "this is definitely too long"));
        assertEquals("hello world", setting.get());
    }
}
