package io.arson.client;

import io.arson.client.ui.ClickGuiLayoutModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClickGuiLayoutModelTest {
    @Test
    void narrowGeometryFitsSmallViewport() {
        var g = ClickGuiLayoutModel.compute(640, 400, 1.0);
        assertEquals(ClickGuiLayoutModel.Mode.NARROW, g.mode());
        assertTrue(g.panelWidth() <= 640 - g.safeMargin() * 2);
        assertTrue(g.panelHeight() <= 400 - g.safeMargin() * 2);
        assertTrue(g.search().right() <= g.panelX() + g.panelWidth());
        assertTrue(g.footer().bottom() <= g.panelY() + g.panelHeight());
        assertEquals(1, g.columns());
    }

    @Test
    void compactGeometryUsesTwoColumnsWhenSpaceAllows() {
        var g = ClickGuiLayoutModel.compute(1000, 650, 1.0);
        assertEquals(ClickGuiLayoutModel.Mode.COMPACT, g.mode());
        assertEquals(2, g.columns());
        var left = g.moduleCard(0, 12);
        var right = g.moduleCard(1, 12);
        assertTrue(left.right() <= right.x());
        assertTrue(right.right() <= g.content().right());
        assertTrue(left.bottom() <= g.content().bottom());
    }

    @Test
    void ultrawideGeometryDoesNotOvergrowOrDrift() {
        var g = ClickGuiLayoutModel.compute(3440, 1440, 1.25);
        assertEquals(ClickGuiLayoutModel.Mode.FULL, g.mode());
        assertTrue(g.panelWidth() <= 1125);
        assertTrue(g.panelX() > 0);
        assertTrue(g.panelX() + g.panelWidth() < 3440);
        assertTrue(g.rail().right() < g.content().x());
    }

    @Test
    void keyboardNavigationIsDeterministicAndWraps() {
        assertEquals(0, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.CATEGORY, 5, 1, 6, 20));
        assertEquals(5, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.CATEGORY, 0, -1, 6, 20));
        assertEquals(2, ClickGuiLayoutModel.moveModule(3, -1, 4, 2));
        assertEquals(2, ClickGuiLayoutModel.moveModule(1, 1, 4, 2));
        assertEquals(2, ClickGuiLayoutModel.moveModule(0, 1, 10, 2));
    }

    @Test
    void emptyCollectionsHaveSafeFocus() {
        assertEquals(-1, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.MODULE, 0, 1, 6, 0));
        assertNull(ClickGuiLayoutModel.safeGet(java.util.List.of("a"), 2));
    }
}
