package io.arson.client;

import io.arson.client.ui.ResponsiveNavigation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponsiveNavigationTest {
    @Test
    void selectionWrapsDeterministically() {
        assertEquals(0, ResponsiveNavigation.moveIndex(-1, 1, 5));
        assertEquals(4, ResponsiveNavigation.moveIndex(0, -1, 5));
        assertEquals(1, ResponsiveNavigation.moveIndex(4, 2, 5));
        assertEquals(-1, ResponsiveNavigation.moveIndex(0, 1, 0));
    }

    @Test
    void categoryNavigationWrapsAtBothEnds() {
        assertEquals(5, ResponsiveNavigation.moveCategory(0, -1, 6));
        assertEquals(0, ResponsiveNavigation.moveCategory(5, 1, 6));
    }
}
