package io.arson.client.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Arson metadata and grouping adapter over Meteor's setting value model. */
public abstract class Setting<T> extends meteordevelopment.meteorclient.settings.Setting<T> {
    private final String id;
    private String arsonDescription = "";
    private BooleanSupplier visibility = () -> true;
    private SettingGroup group;

    protected Setting(String id, String name, T defaultValue) {
        super(name, "", defaultValue, null, null);
        this.id = Objects.requireNonNull(id);
    }

    public final String id() { return id; }
    public final String name() { return name; }
    public T defaultValue() { return getDefaultValue(); }

    @Override
    public boolean set(T value) {
        return super.set(value);
    }

    public String description() { return arsonDescription; }

    public Setting<T> description(String value) {
        arsonDescription = value == null ? "" : value;
        return this;
    }

    public Setting<T> visibleWhen(BooleanSupplier predicate) {
        Objects.requireNonNull(predicate);
        BooleanSupplier previous = visibility;
        visibility = () -> previous.getAsBoolean() && predicate.getAsBoolean();
        return this;
    }

    public Setting<T> dependsOn(BooleanSupplier predicate) {
        return visibleWhen(predicate);
    }

    public Setting<T> dependsOn(Setting<?> dependency, Predicate<Object> predicate) {
        Objects.requireNonNull(dependency);
        Objects.requireNonNull(predicate);
        return visibleWhen(() -> predicate.test(dependency.get()));
    }

    public Setting<T> dependsOnValue(Setting<?> dependency, Object expected) {
        return dependsOn(dependency, value -> Objects.equals(value, expected));
    }

    public Setting<T> dependsOn(BooleanSetting dependency) {
        return dependsOnValue(dependency, Boolean.TRUE);
    }

    public Setting<T> dependsOnNot(BooleanSetting dependency) {
        return dependsOnValue(dependency, Boolean.FALSE);
    }

    public boolean visible() {
        return visibility.getAsBoolean() && (group == null || group.visible());
    }

    public SettingGroup group() { return group; }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S group(SettingGroup value) {
        group = Objects.requireNonNull(value);
        value.add(this);
        return (S) this;
    }
}
