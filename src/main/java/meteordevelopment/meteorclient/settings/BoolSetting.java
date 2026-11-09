/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BoolSetting extends Setting<Boolean> {
    private static final List<String> SUGGESTIONS = List.of("true", "false", "toggle");

    private BoolSetting(String name, String description, Boolean defaultValue, Consumer<Boolean> onChanged,
                        Consumer<Setting<Boolean>> onModuleActivated, BooleanSupplier visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    public boolean parse(String input) {
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("1")) return set(true);
        if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("0")) return set(false);
        if (input.equalsIgnoreCase("toggle")) return set(!get());
        return false;
    }

    public List<String> getSuggestions() {
        return SUGGESTIONS;
    }

    public static class Builder extends SettingBuilder<Builder, Boolean, BoolSetting> {
        public Builder() {
            super(false);
        }

        @Override
        public BoolSetting build() {
            return new BoolSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
