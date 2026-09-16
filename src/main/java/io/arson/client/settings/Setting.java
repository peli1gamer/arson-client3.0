package io.arson.client.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final T defaultValue;
    private T value;
    private String description = "";
    private BooleanSupplier visibility = () -> true;

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
    public boolean visible() { return visibility.getAsBoolean(); }
}
