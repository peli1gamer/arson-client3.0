/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EnumSetting<T extends Enum<T>> extends Setting<T> {
    private final T[] values;
    private final List<String> suggestions;

    private EnumSetting(String name, String description, T defaultValue, Consumer<T> onChanged,
                        Consumer<Setting<T>> onModuleActivated, BooleanSupplier visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        values = defaultValue.getDeclaringClass().getEnumConstants();
        suggestions = new ArrayList<>(values.length);
        for (T value : values) suggestions.add(value.toString());
    }

    public boolean parse(String input) {
        for (T possibleValue : values) {
            if (input.equalsIgnoreCase(possibleValue.toString())) return set(possibleValue);
        }
        return false;
    }

    public List<String> getSuggestions() {
        return List.copyOf(suggestions);
    }

    public static class Builder<T extends Enum<T>> extends SettingBuilder<Builder<T>, T, EnumSetting<T>> {
        public Builder() {
            super(null);
        }

        @Override
        public EnumSetting<T> build() {
            if (defaultValue == null) throw new IllegalStateException("Enum default value is required");
            return new EnumSetting<>(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
