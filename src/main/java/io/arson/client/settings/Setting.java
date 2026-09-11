package io.arson.client.settings;

public abstract class Setting<T> {
    private final String id;
    private final String name;
    private T value;

    protected Setting(String id, String name, T defaultValue) {
        this.id = id;
        this.name = name;
        this.value = defaultValue;
    }

    public String id() { return id; }
    public String name() { return name; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }
}
