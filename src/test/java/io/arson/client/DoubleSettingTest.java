package io.arson.client;

import io.arson.client.settings.DoubleSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleSettingTest {
    @Test void clampsToRange() {
        DoubleSetting setting = new DoubleSetting("value", "Value", 0.5, 0.0, 1.0, 0.1);
        setting.set(-5.0);
        assertEquals(0.0, setting.get(), 1e-9);
        setting.set(5.0);
        assertEquals(1.0, setting.get(), 1e-9);
    }

    @Test void snapsToStepFromMinimum() {
        DoubleSetting setting = new DoubleSetting("value", "Value", 0.0, 0.0, 1.0, 0.1);
        setting.set(0.26);
        assertEquals(0.3, setting.get(), 1e-9);
        setting.set(0.24);
        assertEquals(0.2, setting.get(), 1e-9);
    }

    @Test void incrementAndDecrementRespectBounds() {
        DoubleSetting setting = new DoubleSetting("value", "Value", 0.9, 0.0, 1.0, 0.2);
        setting.increment();
        assertEquals(1.0, setting.get(), 1e-9);
        setting.decrement();
        assertEquals(0.8, setting.get(), 1e-9);
    }

    @Test void rejectsNonFiniteRangeDefinitions() {
        assertThrows(IllegalArgumentException.class,
                () -> new DoubleSetting("value", "Value", 0.5, Double.NaN, 1.0, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new DoubleSetting("value", "Value", 0.5, 0.0, Double.POSITIVE_INFINITY, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new DoubleSetting("value", "Value", Double.NaN, 0.0, 1.0, 0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new DoubleSetting("value", "Value", 0.5, 0.0, 1.0, Double.POSITIVE_INFINITY));
    }
}
