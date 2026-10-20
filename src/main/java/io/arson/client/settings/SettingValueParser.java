package io.arson.client.settings;

import java.util.Locale;

/** Converts user-facing command text into validated setting values. */
public final class SettingValueParser {
    private SettingValueParser() {}

    public static boolean apply(Setting<?> setting, String input) {
        if (setting == null || input == null) return false;
        if (setting instanceof BooleanSetting bool) {
            if ("true".equalsIgnoreCase(input.trim())) { bool.set(true); return true; }
            if ("false".equalsIgnoreCase(input.trim())) { bool.set(false); return true; }
            return false;
        }
        if (setting instanceof DoubleSetting number) {
            try {
                double value = Double.parseDouble(input.trim());
                if (!Double.isFinite(value)) return false;
                number.set(value);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        if (setting instanceof EnumSetting<?> select) {
            String candidate = input.trim();
            for (Enum<?> value : select.values()) {
                if (value.name().equalsIgnoreCase(candidate)) {
                    setEnum(select, value);
                    return true;
                }
            }
            return false;
        }
        if (setting instanceof ColorSetting color) {
            Integer value = parseColor(input.trim());
            if (value == null) return false;
            color.set(value);
            return true;
        }
        if (setting instanceof StringSetting string) {
            if (input.length() > string.maxLength()) return false;
            string.set(input);
            return true;
        }
        return false;
    }

    private static Integer parseColor(String input) {
        try {
            String value = input;
            if (value.startsWith("#")) value = value.substring(1);
            else if (value.regionMatches(true, 0, "0x", 0, 2)) value = value.substring(2);
            if (value.length() == 6) return (int) (0xFF000000L | Long.parseLong(value, 16));
            if (value.length() == 8) return (int) Long.parseUnsignedLong(value, 16);
            return Integer.decode(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setEnum(EnumSetting setting, Enum value) {
        setting.set(value);
    }
}
