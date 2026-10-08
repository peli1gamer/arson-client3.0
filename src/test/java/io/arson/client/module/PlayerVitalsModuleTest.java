package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerVitalsModuleTest {
    @Test
    void exposesLiveTelemetrySurfaceAndSafeInitialState() {
        PlayerVitalsModule module = new PlayerVitalsModule();
        assertEquals("player-vitals", module.id());
        assertEquals(Module.Category.PLAYER, module.category());
        assertTrue(module.description().contains("health"));
        assertEquals(0, module.level());
        assertEquals(0, module.totalExperience());
        assertEquals(0.0f, module.absorption());
        assertEquals(0.0f, module.exhaustion());
        assertEquals("Saturation 0.0  Exhaustion 0.0", module.formattedFoodState());
        assertEquals("Absorption 0.0  Armor 0", module.formattedDefense());
        assertEquals("Air 0/0", module.formattedAir());
        assertTrue(module.formattedExperience().contains("Level 0"));
        assertFalse(module.sprinting());
        assertFalse(module.sneaking());
    }
}
