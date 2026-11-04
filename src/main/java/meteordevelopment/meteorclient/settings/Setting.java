/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson carries the core value, validation, callback and builder semantics used
 * by Meteor's typed settings. Persistence and widget adapters remain separate.
 */
package meteordevelopment.meteorclient.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class Setting<T> {
    public final String name;
    public final String title;
    public final String description;

    protected final T defaultValue;
    protected T value;

    private final Consumer<Setting<T>> onModuleActivated;
    private final Consumer<T> onChanged;
    private final BooleanSupplier visible;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChanged, BooleanSupplier visible) {
        this(name, description, defaultValue, onChanged, null, visible);
    }

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChanged,
                      Consumer<Setting<T>> onModuleActivated, BooleanSupplier visible) {
        this.name = Objects.requireNonNull(name);
        this.title = toTitle(name);
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
        this.onModuleActivated = onModuleActivated;
        this.visible = visible;
        resetImpl();
    }

    public T get() {
        return value;
    }

    public boolean set(T value) {
        if (!isValueValid(value)) return false;
        this.value = value;
        onChanged();
        return true;
    }

    protected void resetImpl() {
        value = defaultValue;
    }

    public void reset() {
        resetImpl();
        onChanged();
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean wasChanged() {
        return !Objects.equals(value, defaultValue);
    }

    public void onChanged() {
        if (onChanged != null) onChanged.accept(value);
    }

    public void onActivated() {
        if (onModuleActivated != null) onModuleActivated.accept(this);
    }

    public boolean isVisible() {
        return visible == null || visible.getAsBoolean();
    }

    protected boolean isValueValid(T value) {
        return true;
    }

    private static String toTitle(String name) {
        StringBuilder result = new StringBuilder(name.length() + 8);
        boolean capitalize = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '-' || c == '_' || c == ' ') {
                result.append(' ');
                capitalize = true;
            } else {
                result.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            }
        }
        return result.toString();
    }

    public abstract static class SettingBuilder<B, V, S> {
        protected String name = "undefined";
        protected String description = "";
        protected V defaultValue;
        protected BooleanSupplier visible;
        protected Consumer<V> onChanged;
        protected Consumer<Setting<V>> onModuleActivated;

        protected SettingBuilder(V defaultValue) {
            this.defaultValue = defaultValue;
        }

        @SuppressWarnings("unchecked")
        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B description(String description) {
            this.description = description;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B defaultValue(V defaultValue) {
            this.defaultValue = defaultValue;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B visible(BooleanSupplier visible) {
            this.visible = visible;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B onChanged(Consumer<V> onChanged) {
            this.onChanged = onChanged;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B onModuleActivated(Consumer<Setting<V>> onModuleActivated) {
            this.onModuleActivated = onModuleActivated;
            return (B) this;
        }

        public abstract S build();
    }
}
