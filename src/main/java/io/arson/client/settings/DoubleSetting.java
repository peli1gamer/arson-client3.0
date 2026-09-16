package io.arson.client.settings;

public final class DoubleSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public DoubleSetting(String id, String name, double defaultValue, double min, double max, double step) {
        super(id, name, defaultValue);
        if (min > max || step <= 0) throw new IllegalArgumentException("Invalid numeric setting range");
        this.min = min;
        this.max = max;
        this.step = step;
        set(defaultValue);
    }

    @Override public void set(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) return;
        double clamped = Math.max(min, Math.min(max, value));
        double snapped = min + Math.round((clamped - min) / step) * step;
        super.set(Math.max(min, Math.min(max, snapped)));
    }
    public double min() { return min; }
    public double max() { return max; }
    public double step() { return step; }
    public void increment() { set(get() + step); }
    public void decrement() { set(get() - step); }
    public double normalized() { return max == min ? 0.0 : (get() - min) / (max - min); }
}
