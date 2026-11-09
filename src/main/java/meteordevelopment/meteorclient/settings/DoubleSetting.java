/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class DoubleSetting extends Setting<Double> {
    public final double min;
    public final double max;
    public final double sliderMin;
    public final double sliderMax;
    public final boolean onSliderRelease;
    public final int decimalPlaces;
    public final boolean noSlider;

    private DoubleSetting(String name, String description, double defaultValue, Consumer<Double> onChanged,
                          Consumer<Setting<Double>> onModuleActivated, BooleanSupplier visible,
                          double min, double max, double sliderMin, double sliderMax,
                          boolean onSliderRelease, int decimalPlaces, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.onSliderRelease = onSliderRelease;
        this.decimalPlaces = decimalPlaces;
        this.noSlider = noSlider;
    }

    public boolean parse(String input) {
        try {
            return set(Double.parseDouble(input.trim()));
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    protected boolean isValueValid(Double value) {
        return value != null && Double.isFinite(value) && value >= min && value <= max;
    }

    public static class Builder extends SettingBuilder<Builder, Double, DoubleSetting> {
        public double min = Double.NEGATIVE_INFINITY;
        public double max = Double.POSITIVE_INFINITY;
        public double sliderMin = 0;
        public double sliderMax = 10;
        public boolean onSliderRelease;
        public int decimalPlaces = 3;
        public boolean noSlider;

        public Builder() {
            super(0D);
        }

        public Builder min(double value) { min = value; return this; }
        public Builder max(double value) { max = value; return this; }
        public Builder range(double min, double max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }
        public Builder sliderMin(double value) { sliderMin = value; return this; }
        public Builder sliderMax(double value) { sliderMax = value; return this; }
        public Builder sliderRange(double min, double max) {
            sliderMin = min;
            sliderMax = max;
            return this;
        }
        public Builder onSliderRelease() { onSliderRelease = true; return this; }
        public Builder decimalPlaces(int value) { decimalPlaces = value; return this; }
        public Builder noSlider() { noSlider = true; return this; }

        @Override
        public DoubleSetting build() {
            if (min > max) throw new IllegalArgumentException("Minimum exceeds maximum");
            if (defaultValue < min || defaultValue > max) throw new IllegalArgumentException("Default value is outside the allowed range");
            return new DoubleSetting(name, description, defaultValue, onChanged, onModuleActivated, visible,
                min, max, Math.max(sliderMin, min), Math.min(sliderMax, max),
                onSliderRelease, decimalPlaces, noSlider);
        }
    }
}
