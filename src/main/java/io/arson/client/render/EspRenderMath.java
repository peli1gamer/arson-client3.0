package io.arson.client.render;

import com.arson.client.render.RenderStyle;

/** Shared, finite-safe presentation math for world-space ESP effects. */
public final class EspRenderMath {
    private EspRenderMath() {
    }

    /**
     * Keeps full opacity until fadeStart (a fraction of range), then fades linearly to zero
     * at the configured range. Invalid inputs produce a safe, invisible result.
     */
    public static float distanceFade(double distance, double range, double fadeStart) {
        if (!Double.isFinite(distance) || !Double.isFinite(range) || range <= 0.0) return 0.0f;
        double start = Double.isFinite(fadeStart) ? Math.max(0.0, Math.min(0.99, fadeStart)) : 0.45;
        double normalized = Math.max(0.0, distance / range);
        if (normalized <= start) return 1.0f;
        if (normalized >= 1.0) return 0.0f;
        return (float) Math.max(0.0, Math.min(1.0, (1.0 - normalized) / (1.0 - start)));
    }

    public static RenderStyle fadeStyle(RenderStyle style, float fade) {
        if (style == null) return null;
        float amount = Float.isFinite(fade) ? Math.max(0.0f, Math.min(1.0f, fade)) : 0.0f;
        return new RenderStyle(style.color(), style.fill(), style.outline(),
                style.fillAlpha() * amount, style.outlineAlpha() * amount, style.lineWidth());
    }

    public static int fadeAlpha(int argb, float fade) {
        float amount = Float.isFinite(fade) ? Math.max(0.0f, Math.min(1.0f, fade)) : 0.0f;
        int alpha = Math.round(((argb >>> 24) & 0xFF) * amount);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }
}