package io.arson.client.render;

import io.arson.client.module.StructureESPModule;

/** Immutable detector result, independent of the rendering backend. */
public record StructureTarget(StructureESPModule.Detector detector,
                              double minX, double minY, double minZ,
                              double maxX, double maxY, double maxZ) {
}
