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
        assertFalse(g.search().intersects(g.toolbar()));
        assertFalse(g.toolbar().intersects(g.content()));
        assertFalse(g.rail().intersects(g.content()));
        assertFalse(g.footer().intersects(g.content()));
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
        assertTrue(left.bottom() <= g.moduleList().bottom());
        assertFalse(left.intersects(g.detail()));
        assertFalse(right.intersects(g.detail()));
    }

    @Test
    void ultrawideGeometryDoesNotOvergrowOrDrift() {
        var g = ClickGuiLayoutModel.compute(3440, 1440, 1.25);
        assertEquals(ClickGuiLayoutModel.Mode.FULL, g.mode());
        assertTrue(g.panelWidth() <= 1125);
        assertTrue(g.panelX() > 0);
        assertTrue(g.panelX() + g.panelWidth() < 3440);
        assertFalse(g.search().intersects(g.toolbar()));
        assertFalse(g.toolbar().intersects(g.content()));
        assertFalse(g.rail().intersects(g.content()));
        assertFalse(g.footer().intersects(g.content()));
        assertTrue(g.rail().right() < g.content().x());
    }

    @Test
    void keyboardNavigationIsDeterministicAndWraps() {
        assertEquals(0, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.CATEGORY, 5, 1, 6, 20));
        assertEquals(5, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.CATEGORY, 0, -1, 6, 20));
        assertEquals(2, ClickGuiLayoutModel.moveModule(3, -1, 4, 2));
        assertEquals(2, ClickGuiLayoutModel.moveModule(1, 1, 4, 2));
        assertEquals(1, ClickGuiLayoutModel.moveModule(0, 1, 10, 2));
    }

    @Test
    void finalResponsiveContractKeepsSearchInsidePanel() {
        for (int[] size : new int[][]{{360,240},{640,400},{900,600},{3440,1440}}) {
            var g=ClickGuiLayoutModel.compute(size[0],size[1],1.25);
            assertTrue(g.search().x() >= g.panelX());
            assertTrue(g.search().right() <= g.panelX()+g.panelWidth());
            assertTrue(g.footer().bottom() <= g.panelY()+g.panelHeight());
        }
    }

    @Test
    void tinyNarrowToolbarHasEnoughRoomForSixControls() {
        var g = ClickGuiLayoutModel.compute(360, 240, 1.25);
        int gap = 2;
        int fitted = Math.max(15, (g.toolbar().width() - gap * 5) / 6);
        int total = fitted * 6 + gap * 5;
        assertTrue(total <= g.toolbar().width());
    }

    @Test
    void categoryRowsCannotRequireTallerThanTheirAllocatedSlot() {
        var g = ClickGuiLayoutModel.compute(360, 240, 1.25);
        assertTrue(g.categoryRowHeight() >= 18);
        assertTrue(Math.min(24, g.categoryRowHeight()) <= g.categoryRowHeight());
    }

    @Test
    void responsiveModuleGridStaysInsideItsAreaWithoutOverlaps() {
        for (int[] size : new int[][]{{100,64},{320,120},{480,220},{960,300}}) {
            var area = new ClickGuiLayoutModel.Rect(20, 30, size[0], size[1]);
            var cards = ClickGuiLayoutModel.gridCards(area, 30, 3, 48, 8);
            assertFalse(cards.isEmpty());
            assertTrue(cards.size() <= 30);
            assertTrue(ClickGuiLayoutModel.pairwiseNonIntersecting(cards));
            for (var card : cards) {
                assertTrue(card.x() >= area.x());
                assertTrue(card.y() >= area.y());
                assertTrue(card.right() <= area.right());
                assertTrue(card.bottom() <= area.bottom());
            }
        }
    }

    @Test
    void moduleCardsReserveRoomForSecondRowKeybindControls() {
        for (int[] size : new int[][]{{360, 240}, {640, 400}, {960, 640}, {1600, 900}}) {
            var geometry = ClickGuiLayoutModel.compute(size[0], size[1], 1.0);
            int requestedHeight = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 52 : 60;
            var cards = ClickGuiLayoutModel.gridCards(geometry.moduleList(), 8, geometry.columns(),
                    requestedHeight, geometry.cardGap());
            assertFalse(cards.isEmpty());
            for (var card : cards) {
                assertTrue(card.height() >= 50);
                assertTrue(card.right() <= geometry.moduleList().right());
                assertTrue(card.bottom() <= geometry.moduleList().bottom());
            }
        }
    }

    @Test
    void moduleCardDetailsAndToggleTargetsStaySeparatedInNarrowAndWideCards() {
        for (int width : new int[]{70, 100, 180}) {
            var card = new ClickGuiLayoutModel.Rect(12, 24, width, 40);
            var actions = ClickGuiLayoutModel.moduleCardActions(card, 38, 5);
            assertTrue(actions.details().width() > 0);
            assertFalse(actions.details().intersects(actions.toggle()));
            assertTrue(actions.details().x() >= card.x());
            assertTrue(actions.details().right() <= actions.toggle().x());
            assertEquals(card.right(), actions.toggle().right());
            assertEquals(card.y(), actions.toggle().y());
            assertEquals(card.height(), actions.toggle().height());
        }
        var empty = ClickGuiLayoutModel.moduleCardActions(null, 38, 5);
        assertEquals(0, empty.details().width());
        assertEquals(0, empty.toggle().width());
    }

    @Test
    void moduleGridHandlesEmptyAndDegenerateAreas() {
        assertTrue(ClickGuiLayoutModel.gridCards(new ClickGuiLayoutModel.Rect(0, 0, 10, 10), 0, 2, 8, 2).isEmpty());
        assertTrue(ClickGuiLayoutModel.gridCards(new ClickGuiLayoutModel.Rect(0, 0, 0, 10), 4, 2, 8, 2).isEmpty());
    }

    @Test
    void emptyCollectionsHaveSafeFocus() {
        assertEquals(-1, ClickGuiLayoutModel.moveFocus(ClickGuiLayoutModel.Focus.MODULE, 0, 1, 6, 0));
        assertNull(ClickGuiLayoutModel.safeGet(java.util.List.of("a"), 2));
    }
}
