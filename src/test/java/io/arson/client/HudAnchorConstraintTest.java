package io.arson.client;

import io.arson.client.ui.HudAnchorConstraint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HudAnchorConstraintTest {
    @Test void centerAnchorResolvesAndRoundTripsOffset() {
        double[] position = HudAnchorConstraint.resolve(HudAnchorConstraint.Anchor.CENTER, 800, 600, 200, 40, 12, -8);
        assertArrayEquals(new double[]{312, 272}, position);
        assertArrayEquals(new double[]{12, -8}, HudAnchorConstraint.offsetFor(HudAnchorConstraint.Anchor.CENTER, 800, 600, 200, 40, position[0], position[1]));
    }

    @Test void bottomRightAnchorAccountsForBoxSize() {
        assertArrayEquals(new double[]{590, 540}, HudAnchorConstraint.resolve(HudAnchorConstraint.Anchor.BOTTOM_RIGHT, 800, 600, 200, 60, -10, 0));
    }
}
