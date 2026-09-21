package io.arson.client;

import io.arson.client.ui.ResponsiveUiLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsiveUiLayoutTest {
    @Test
    void narrowViewportFitsPanelAndKeepsInset() {
        ResponsiveUiLayout.Rect r = ResponsiveUiLayout.fitPanel(320, 240, 900, 540, 8);
        assertEquals(304, r.width());
        assertEquals(224, r.height());
        assertTrue(r.x() >= 8 && r.right() <= 312);
        assertTrue(r.y() >= 8 && r.bottom() <= 232);
    }

    @Test
    void ultrawideViewportKeepsPreferredPanelCentered() {
        ResponsiveUiLayout.Rect r = ResponsiveUiLayout.fitPanel(3440, 1440, 900, 540, 12);
        assertEquals(900, r.width());
        assertEquals(540, r.height());
        assertEquals((3440 - 900) / 2, r.x());
        assertEquals((1440 - 540) / 2, r.y());
    }

    @Test
    void viewportScaleUsesTheTighterAxis() {
        assertEquals(0.5, ResponsiveUiLayout.viewportScale(320, 240, 900, 540, 0.5, 1.0), 0.0001);
        assertEquals(1.0, ResponsiveUiLayout.viewportScale(1920, 1080, 900, 540, 0.5, 1.0), 0.0001);
    }

    @Test
    void clampAndFallbackPlacementAreDeterministic() {
        ResponsiveUiLayout.Rect a = ResponsiveUiLayout.clampRect(999, 999, 100, 40, 320, 240, 8);
        ResponsiveUiLayout.Rect b = ResponsiveUiLayout.deterministicSlot(3, 100, 40, 8, 2, 320, 240, 8);
        assertEquals(new ResponsiveUiLayout.Rect(212, 192, 100, 40), a);
        assertEquals(new ResponsiveUiLayout.Rect(116, 56, 100, 40), b);
    }
}
