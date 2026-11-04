package io.arson.client.settings;

public final class StringSetting extends Setting<String> {
    private final int maxLength;
    public StringSetting(String id, String name, String defaultValue) { this(id, name, defaultValue, 128); }
    public StringSetting(String id, String name, String defaultValue, int maxLength) {
        super(id, name, defaultValue == null ? "" : defaultValue);
        if (maxLength <= 0) throw new IllegalArgumentException("Invalid string setting length");
        this.maxLength = maxLength;
    }
    @Override public boolean set(String value) { String safe = value == null ? "" : value; return super.set(safe.length() > maxLength ? safe.substring(0, maxLength) : safe); }
    public int maxLength() { return maxLength; }
}
