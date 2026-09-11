package com.arson.client.render;

/** Shared immutable RGB color used by client-side render modules. */
public record RenderColor(int red, int green, int blue, int alpha) {
    public RenderColor(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public RenderColor {
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
        alpha = clamp(alpha);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public int argb() {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16)
                | ((green & 0xFF) << 8) | (blue & 0xFF);
    }
}
