package io.arson.client;

import io.arson.client.module.HudLayoutModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudLayoutModuleTest {
    @Test
    void topLeftDefaultsPreserveExistingHudPositions() {
        HudLayoutModule layout = new HudLayoutModule();
        assertEquals(HudLayoutModule.Anchor.TOP_LEFT, layout.anchor("watermark"));
        assertArrayEquals(new double[]{6, 6}, layout.resolve("watermark", 320, 180, 100, 20));
        assertArrayEquals(new double[]{6, 94}, layout.resolve("world-info", 320, 180, 100, 20));
    }

    @Test
    void anchorChangePreservesVisualPositionAndSurvivesViewportResize() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setAnchor("watermark", HudLayoutModule.Anchor.TOP_RIGHT);
        layout.setPositionPreservingAnchor("watermark", 210, 12, 320, 180, 100, 20);
        assertArrayEquals(new double[]{210, 12}, layout.resolve("watermark", 320, 180, 100, 20));
        double[] resized = layout.resolve("watermark", 400, 180, 100, 20);
        assertArrayEquals(new double[]{290, 12}, resized);
    }

    @Test
    void clampKeepsAnchoredElementInsideViewport() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setAnchor("fps", HudLayoutModule.Anchor.BOTTOM_RIGHT);
        layout.setOffset("fps", 500, 500);
        assertArrayEquals(new double[]{220, 160}, layout.resolve("fps", 320, 180, 100, 20));
        layout.setOffset("fps", -500, -500);
        assertArrayEquals(new double[]{0, 0}, layout.resolve("fps", 320, 180, 100, 20));
    }

    @Test
    void overlapRepairKeepsEveryHudElementInsideAndNonIntersecting() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setOffset("watermark", 0, 0);
        layout.setOffset("coordinates", 0, 0);
        layout.setOffset("fps", 0, 0);
        layout.setOffset("player-info", 0, 0);
        layout.setOffset("world-info", 0, 0);
        boolean changed = layout.repairNoOverlap(360, 240, 1.0, 100, 80, null);
        assertTrue(changed);
        assertNotEquals(0, layout.offsetY("coordinates"));
        assertNotEquals(0, layout.offsetY("fps"));
        java.util.List<io.arson.client.ui.NoOverlapLayout.Rect> rects = new java.util.ArrayList<>();
        for (String e : java.util.List.of("watermark","coordinates","fps","player-info","world-info")) {
            double[] p = layout.resolve(e, 360, 240, 95, 18);
            rects.add(new io.arson.client.ui.NoOverlapLayout.Rect((int)p[0],(int)p[1],95,18));
        }
        assertTrue(rects.stream().allMatch(r -> r.within(360,240,new io.arson.client.ui.NoOverlapLayout.Insets(6,6,6,6))));
        assertEquals(5, rects.size());
    }

    @Test
    void resetRestoresAnchorAndOffsets() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setAnchor("coordinates", HudLayoutModule.Anchor.CENTER);
        layout.setOffset("coordinates", 33, 44);
        layout.resetElement("coordinates");
        assertEquals(HudLayoutModule.Anchor.TOP_LEFT, layout.anchor("coordinates"));
        assertEquals(6, layout.offsetX("coordinates"));
        assertEquals(28, layout.offsetY("coordinates"));
    }
}
