package io.arson.client.settings;

/** Packed ARGB color setting. The value is clamped to an unsigned 32-bit range. */
public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String id, String name, int defaultValue) {
        super(id, name, defaultValue);
    }

    @Override
    public void set(Integer value) {
        super.set(value == null ? 0 : value);
    }
}
