/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Core setting value and visibility semantics used by the Arson settings adapter.
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

    private final Consumer<T> onChanged;
    private final BooleanSupplier visible;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChanged, BooleanSupplier visible) {
        this.name = Objects.requireNonNull(name);
        this.title = toTitle(name);
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue;
        this.onChanged = onChanged;
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
}
