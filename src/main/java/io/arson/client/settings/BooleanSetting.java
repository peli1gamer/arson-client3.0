package io.arson.client.settings;

public final class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String id, String name, boolean defaultValue) {
        super(id, name, defaultValue);
    }

    public boolean enabled() {
        return get();
    }
}
