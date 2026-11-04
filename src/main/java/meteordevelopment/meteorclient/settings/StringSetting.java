/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StringSetting extends Setting<String> {
    public final String placeholder;
    private final int maxLength;
    private final Predicate<Character> filter;

    private StringSetting(String name, String description, String defaultValue, Consumer<String> onChanged,
                          Consumer<Setting<String>> onModuleActivated, BooleanSupplier visible,
                          String placeholder, int maxLength, Predicate<Character> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.filter = filter;
    }

    @Override
    protected boolean isValueValid(String value) {
        if (value == null || value.length() > maxLength) return false;
        if (filter == null) return true;
        for (int i = 0; i < value.length(); i++) if (!filter.test(value.charAt(i))) return false;
        return true;
    }

    public static class Builder extends SettingBuilder<Builder, String, StringSetting> {
        private String placeholder = "";
        private int maxLength = 256;
        private Predicate<Character> filter;

        public Builder() {
            super("");
        }

        public Builder placeholder(String value) { placeholder = value == null ? "" : value; return this; }
        public Builder maxLength(int value) {
            if (value < 1) throw new IllegalArgumentException("Maximum length must be positive");
            maxLength = value;
            return this;
        }
        public Builder filter(Predicate<Character> value) { filter = value; return this; }

        @Override
        public StringSetting build() {
            if (defaultValue == null || defaultValue.length() > maxLength) {
                throw new IllegalArgumentException("Default string exceeds maximum length");
            }
            return new StringSetting(name, description, defaultValue, onChanged, onModuleActivated,
                visible, placeholder, maxLength, filter);
        }
    }
}
