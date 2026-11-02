package io.arson.client.settings;

public final class DoubleSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public DoubleSetting(String id, String name, double defaultValue, double min, double max, double step) {
        super(id, name, normalizeDefault(defaultValue, min, max, step));
        this.min = min;
        this.max = max;
        this.step = step;
    }

    private static double normalizeDefault(double value, double min, double max, double step) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || !Double.isFinite(step)
                || !Double.isFinite(value) || min > max || step <= 0.0
                || !Double.isFinite(max - min)) {
            throw new IllegalArgumentException("Invalid numeric setting range or default");
        }
        double clamped = Math.max(min, Math.min(max, value));
        double snapped = min + Math.round((clamped - min) / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }

    @Override public void set(Double value) {
        if (value == null || !Double.isFinite(value)) return;
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
