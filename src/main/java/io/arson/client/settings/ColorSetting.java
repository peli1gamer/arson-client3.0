package io.arson.client.settings;

/** Packed ARGB color setting. */
public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String id, String name, int defaultValue) {
        super(id, name, defaultValue);
    }

    @Override
    public boolean set(Integer value) {
        return super.set(value == null ? 0 : value);
    }
}
