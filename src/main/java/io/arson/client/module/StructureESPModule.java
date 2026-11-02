package io.arson.client.module;

import io.arson.client.settings.DoubleSetting;

/** Shared configuration and detector identity for Glazed-inspired structure ESP modules. */
public abstract class StructureESPModule extends VisualModule {
    public enum Detector { HOLE_TUNNEL_STAIRS, COVERED_HOLE, ONE_BY_ONE_HOLE, DRIPSTONE, BEEHIVE, CHUNK_FINDER }
    private final Detector detector;
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 24.0, 8.0, 48.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 10.0, 1.0, 40.0, 1.0));
    protected StructureESPModule(String id, String name, Detector detector) {
        super(id, name, false);
        this.detector = detector;
    }
    public final Detector detector() { return detector; }
    public final double range() { return range.get(); }
    public final int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }
}
