package io.arson.client;

import io.arson.client.ui.NoOverlapLayout;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NoOverlapLayoutTest {
    @Test
    void stacksCollidingLegacyPositionsDeterministically() {
        Map<String,NoOverlapLayout.Rect> preferred=Map.of(
                "a",new NoOverlapLayout.Rect(10,10,80,20),
                "b",new NoOverlapLayout.Rect(10,10,80,20),
                "c",new NoOverlapLayout.Rect(10,10,80,20));
        Map<String,NoOverlapLayout.Size> sizes=Map.of(
                "a",new NoOverlapLayout.Size(80,20),
                "b",new NoOverlapLayout.Size(80,20),
                "c",new NoOverlapLayout.Size(80,20));
        var solved=NoOverlapLayout.solve(320,180,new NoOverlapLayout.Insets(6,6,6,6),4,preferred,sizes,List.of("a","b","c"));
        var rects=solved.values().stream().map(NoOverlapLayout.Placement::rect).toList();
        assertTrue(NoOverlapLayout.pairwiseNonIntersecting(rects));
        assertTrue(rects.stream().allMatch(r->r.within(320,180,new NoOverlapLayout.Insets(6,6,6,6))));
        assertEquals(new NoOverlapLayout.Rect(10,10,80,20),solved.get("a").rect());
        assertTrue(solved.get("b").moved());
        assertTrue(solved.get("c").moved());
    }

    @Test
    void tinyViewportCompactsOversizedRectanglesIntoSafeArea() {
        Map<String,NoOverlapLayout.Rect> preferred=Map.of(
                "first",new NoOverlapLayout.Rect(-100,-100,500,300),
                "second",new NoOverlapLayout.Rect(-100,-100,500,300));
        Map<String,NoOverlapLayout.Size> sizes=Map.of(
                "first",new NoOverlapLayout.Size(500,300),
                "second",new NoOverlapLayout.Size(500,300));
        var solved=NoOverlapLayout.solve(120,90,new NoOverlapLayout.Insets(4,4,4,4),2,preferred,sizes,List.of("first","second"));
        var rects=solved.values().stream().map(NoOverlapLayout.Placement::rect).toList();
        assertTrue(rects.get(0).within(120,90,new NoOverlapLayout.Insets(4,4,4,4)));
        assertTrue(solved.get("first").compacted());
        assertTrue(NoOverlapLayout.pairwiseNonIntersecting(rects));
    }


    @Test
    void tooltipFlowsAroundReservedControls() {
        Map<String,NoOverlapLayout.Rect> preferred=Map.of(
                "panel",new NoOverlapLayout.Rect(40,30,240,140),
                "control",new NoOverlapLayout.Rect(80,70,120,22),
                "tooltip",new NoOverlapLayout.Rect(82,72,160,44));
        Map<String,NoOverlapLayout.Size> sizes=Map.of(
                "panel",new NoOverlapLayout.Size(240,140),
                "control",new NoOverlapLayout.Size(120,22),
                "tooltip",new NoOverlapLayout.Size(160,44));
        var solved=NoOverlapLayout.solve(320,200,new NoOverlapLayout.Insets(6,6,6,6),4,preferred,sizes,List.of("panel","control","tooltip"));
        assertTrue(solved.get("tooltip").rect().within(320,200,new NoOverlapLayout.Insets(6,6,6,6)));
        assertFalse(solved.get("tooltip").rect().intersects(solved.get("control").rect()));
    }

    @Test
    void ultrawideKeepsPlacementsInsideSafeArea() {
        Map<String,NoOverlapLayout.Rect> preferred=Map.of(
                "left",new NoOverlapLayout.Rect(4,4,200,40),
                "right",new NoOverlapLayout.Rect(3300,4,200,40),
                "tooltip",new NoOverlapLayout.Rect(3290,4,180,60));
        Map<String,NoOverlapLayout.Size> sizes=Map.of(
                "left",new NoOverlapLayout.Size(200,40),
                "right",new NoOverlapLayout.Size(200,40),
                "tooltip",new NoOverlapLayout.Size(180,60));
        var solved=NoOverlapLayout.solve(3440,1440,new NoOverlapLayout.Insets(12,12,12,12),6,preferred,sizes,List.of("left","right","tooltip"));
        var rects=solved.values().stream().map(NoOverlapLayout.Placement::rect).toList();
        assertTrue(rects.stream().allMatch(r->r.within(3440,1440,new NoOverlapLayout.Insets(12,12,12,12))));
        assertTrue(NoOverlapLayout.pairwiseNonIntersecting(rects));
    }
}
