/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class IntSetting extends Setting<Integer> {
    public final int min;
    public final int max;
    public final int sliderMin;
    public final int sliderMax;
    public final boolean noSlider;

    private IntSetting(String name, String description, int defaultValue, Consumer<Integer> onChanged,
                       Consumer<Setting<Integer>> onModuleActivated, BooleanSupplier visible,
                       int min, int max, int sliderMin, int sliderMax, boolean noSlider) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.min = min;
        this.max = max;
        this.sliderMin = sliderMin;
        this.sliderMax = sliderMax;
        this.noSlider = noSlider;
    }

    public boolean parse(String input) {
        try {
            return set(Integer.parseInt(input.trim()));
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    protected boolean isValueValid(Integer value) {
        return value != null && value >= min && value <= max;
    }

    public static class Builder extends SettingBuilder<Builder, Integer, IntSetting> {
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;
        private int sliderMin = 0;
        private int sliderMax = 10;
        private boolean noSlider;

        public Builder() {
            super(0);
        }

        public Builder min(int value) { min = value; return this; }
        public Builder max(int value) { max = value; return this; }
        public Builder range(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            return this;
        }
        public Builder sliderMin(int value) { sliderMin = value; return this; }
        public Builder sliderMax(int value) { sliderMax = value; return this; }
        public Builder sliderRange(int min, int max) {
            sliderMin = min;
            sliderMax = max;
            return this;
        }
        public Builder noSlider() { noSlider = true; return this; }

        @Override
        public IntSetting build() {
            if (defaultValue < min || defaultValue > max) {
                throw new IllegalArgumentException("Default integer is outside the allowed range");
            }
            return new IntSetting(name, description, defaultValue, onChanged, onModuleActivated, visible,
                min, max, Math.max(sliderMin, min), Math.min(sliderMax, max), noSlider);
        }
    }
}
