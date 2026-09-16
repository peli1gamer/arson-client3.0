package io.arson.client.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final T defaultValue;
    private T value;
    private String description = "";
    private BooleanSupplier visibility = () -> true;
    private SettingGroup group;

    protected Setting(String id, String name, T defaultValue) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public final String id() { return id; }
    public final String name() { return name; }
    public T get() { return value; }
    public T defaultValue() { return defaultValue; }
    public void set(T value) { this.value = value; }
    public void reset() { this.value = defaultValue; }
    public String description() { return description; }
    public Setting<T> description(String value) { description = value == null ? "" : value; return this; }
    public Setting<T> visibleWhen(BooleanSupplier predicate) { visibility = Objects.requireNonNull(predicate); return this; }
    public Setting<T> dependsOn(BooleanSupplier predicate) { return visibleWhen(predicate); }

    /** Typed dependency: this setting is visible only while the dependency satisfies the predicate. */
    public Setting<T> dependsOn(Setting<?> dependency, Predicate<Object> predicate) {
        Objects.requireNonNull(dependency);
        Objects.requireNonNull(predicate);
        return visibleWhen(() -> predicate.test(dependency.get()));
    }

    /** Common dependency alias for enum/string/color/numeric settings and other value types. */
    public Setting<T> dependsOnValue(Setting<?> dependency, Object expected) {
        return dependsOn(dependency, value -> Objects.equals(value, expected));
    }

    /** Common boolean dependency alias. */
    public Setting<T> dependsOn(BooleanSetting dependency) {
        return dependsOnValue(dependency, Boolean.TRUE);
    }

    /** Inverse boolean dependency alias. */
    public Setting<T> dependsOnNot(BooleanSetting dependency) {
        return dependsOnValue(dependency, Boolean.FALSE);
    }

    public boolean visible() { return visibility.getAsBoolean() && (group == null || group.visible()); }
    public SettingGroup group() { return group; }
    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S group(SettingGroup value) {
        group = Objects.requireNonNull(value);
        value.add(this);
        return (S) this;
    }
}
