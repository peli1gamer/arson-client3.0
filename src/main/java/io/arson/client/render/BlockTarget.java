package io.arson.client.render;

import com.arson.client.render.RenderStyle;

/** Cached block target handed from block discovery to the world renderer. */
public record BlockTarget(double minX, double minY, double minZ,
                          double maxX, double maxY, double maxZ,
                          RenderStyle style) {
}
