package io.arson.client.settings;

/** Bounded, step-aware integer setting with Meteor-style typed access. */
public final class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;
    private final int step;

    public IntSetting(String id, String name, int defaultValue, int min, int max, int step) {
        super(id, name, defaultValue);
        if (min > max || step <= 0) throw new IllegalArgumentException("Invalid integer setting range");
        this.min = min;
        this.max = max;
        this.step = step;
        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("Default integer is outside the allowed range");
        }
        set(defaultValue);
    }

    @Override
    public boolean set(Integer value) {
        if (value == null) return false;
        long bounded = Math.max((long) min, Math.min((long) max, value.longValue()));
        long snapped = min + Math.round((bounded - min) / (double) step) * (long) step;
        return super.set((int) Math.max(min, Math.min((long) max, snapped)));
    }

    public int min() { return min; }
    public int max() { return max; }
    public int step() { return step; }
    public double normalized() {
        return max == min ? 0.0 : (get() - (double) min) / ((double) max - min);
    }
    public void increment() { set(get() + step); }
    public void decrement() { set(get() - step); }
}
