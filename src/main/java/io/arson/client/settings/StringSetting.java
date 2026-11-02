package io.arson.client.settings;

public final class StringSetting extends Setting<String> {
    private final int maxLength;

    public StringSetting(String id, String name, String defaultValue) {
        this(id, name, defaultValue, 128);
    }

    public StringSetting(String id, String name, String defaultValue, int maxLength) {
        super(id, name, normalizeDefault(defaultValue, maxLength));
        this.maxLength = maxLength;
    }

    private static String normalizeDefault(String value, int maxLength) {
        if (maxLength <= 0) throw new IllegalArgumentException("Invalid string setting length");
        String safe = value == null ? "" : value;
        return safe.length() > maxLength ? safe.substring(0, maxLength) : safe;
    }

    @Override public void set(String value) {
        String safe = value == null ? "" : value;
        super.set(safe.length() > maxLength ? safe.substring(0, maxLength) : safe);
    }

    public int maxLength() { return maxLength; }
}
