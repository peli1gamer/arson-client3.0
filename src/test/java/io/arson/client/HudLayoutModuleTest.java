package io.arson.client;

import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HudModule;
import io.arson.client.ui.NoOverlapLayout;
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
    void overlapRepairChangesLegacyCollisionState() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setOffset("watermark", 0, 0);
        layout.setOffset("coordinates", 0, 0);
        layout.setOffset("fps", 0, 0);
        layout.setOffset("player-info", 0, 0);
        layout.setOffset("world-info", 0, 0);
        assertTrue(layout.repairNoOverlap(360, 240, 1.0, 100, 80, null));
        assertFalse(layout.repairNoOverlap(360, 240, 1.0, 100, 80, null));
    }

    @Test
    void hiddenElementsDoNotReserveSpaceDuringOverlapRepair() {
        HudLayoutModule layout = new HudLayoutModule();
        HudModule hud = new HudModule();
        for (String element : new String[]{"watermark", "coordinates", "fps", "player-info", "world-info"}) {
            layout.setOffset(element, 0, 0);
        }
        hud.setElementVisible("coordinates", false);

        layout.repairNoOverlap(640, 360, 1.0, 100, 80, null, hud);

        assertEquals(0.0, layout.offsetX("coordinates"));
        assertEquals(0.0, layout.offsetY("coordinates"));
    }

    @Test
    void overlapRepairUsesMeasuredContentBounds() {
        HudLayoutModule layout = new HudLayoutModule();
        HudModule hud = new HudModule();
        for (String element : new String[]{"coordinates", "fps", "world-info"}) hud.setElementVisible(element, false);
        layout.setOffset("watermark", 6, 6);
        layout.setOffset("player-info", 6, 6);
        layout.updateElementSize("watermark", 120, 20);
        layout.updateElementSize("player-info", 120, 110);

        layout.repairNoOverlap(360, 240, 1.0, 100, 80, null, hud);

        double[] watermark = layout.resolve("watermark", 360, 240, 120, 20);
        double[] playerInfo = layout.resolve("player-info", 360, 240, 120, 110);
        NoOverlapLayout.Rect first = new NoOverlapLayout.Rect((int) watermark[0], (int) watermark[1], 120, 20);
        NoOverlapLayout.Rect second = new NoOverlapLayout.Rect((int) playerInfo[0], (int) playerInfo[1], 120, 110);
        assertFalse(first.intersects(second));
    }

    @Test
    void editorCanReadMeasuredBoundsWithFallbackBeforeFirstRender() {
        HudLayoutModule layout = new HudLayoutModule();

        assertEquals(new NoOverlapLayout.Size(190, 35), layout.measuredSize("watermark", 190, 35));
        layout.updateElementSize("player-info", 140, 96);
        assertEquals(new NoOverlapLayout.Size(140, 96), layout.measuredSize("player-info", 190, 35));
    }

    @Test
    void measuredBoundsSignatureChangesWhenRenderedSizeChanges() {
        HudLayoutModule layout = new HudLayoutModule();
        int before = layout.boundsSignature();

        assertTrue(layout.updateElementSize("player-info", 140, 96));
        assertNotEquals(before, layout.boundsSignature());
        assertFalse(layout.updateElementSize("player-info", 140, 96));
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
