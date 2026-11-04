/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson carries the core value, validation, callback and builder semantics used
 * by Meteor's typed settings. Persistence and widget adapters remain separate.
 */
package meteordevelopment.meteorclient.settings;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
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

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        Object current = value;
        if (current instanceof Boolean bool) tag.putBoolean("value", bool);
        else if (current instanceof Integer integer) tag.putInt("value", integer);
        else if (current instanceof Long number) tag.putLong("value", number);
        else if (current instanceof Float number) tag.putFloat("value", number);
        else if (current instanceof Double number) tag.putDouble("value", number);
        else if (current instanceof String string) tag.putString("value", string);
        else if (current instanceof Enum<?> enumeration) tag.putString("value", enumeration.name());
        return tag;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Setting<T> fromTag(NbtCompound tag) {
        if (!tag.contains("value")) return this;
        Object fallback = defaultValue;
        if (fallback instanceof Boolean bool) set((T) Boolean.valueOf(tag.getBoolean("value", bool)));
        else if (fallback instanceof Integer number) set((T) Integer.valueOf(tag.getInt("value", number)));
        else if (fallback instanceof Long number) set((T) Long.valueOf(tag.getLong("value", number)));
        else if (fallback instanceof Float number) set((T) Float.valueOf(tag.getFloat("value", number)));
        else if (fallback instanceof Double number) set((T) Double.valueOf(tag.getDouble("value", number)));
        else if (fallback instanceof String string) set((T) tag.getString("value", string));
        else if (fallback instanceof Enum enumeration) {
            try { set((T) Enum.valueOf(enumeration.getDeclaringClass(), tag.getString("value", enumeration.name()))); }
            catch (IllegalArgumentException ignored) { }
        }
        return this;
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
