package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatInfoPresentationTest {
    @Test
    void defensiveReadoutsAreEnabledAndConfigurableBySetting() {
        CombatInfoModule module = new CombatInfoModule();

        assertTrue(module.showAbsorption());
        assertTrue(module.showArmor());
        assertTrue(module.showEffects());
        assertEquals(4, module.effectLimit());
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-absorption")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-armor")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-effects")));
        var limit = (io.arson.client.settings.DoubleSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("effect-limit")).findFirst().orElseThrow();
        limit.set(2.0);
        assertEquals(2, module.effectLimit());

        var absorption = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-absorption")).findFirst().orElseThrow();
        var armor = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-armor")).findFirst().orElseThrow();
        var effects = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-effects")).findFirst().orElseThrow();
        absorption.set(false);
        armor.set(false);
        effects.set(false);
        assertFalse(module.showAbsorption());
        assertFalse(module.showArmor());
        assertFalse(module.showEffects());
    }

    @Test
    void effectRowsAreSortedLimitedAndShowRemainingCount() {
        var effects = java.util.List.of(
                new CombatInfoModule.EffectSnapshot("Speed", 0, 400),
                new CombatInfoModule.EffectSnapshot("Strength", 1, 1300),
                new CombatInfoModule.EffectSnapshot("Jump Boost", 0, -1));

        assertEquals(java.util.List.of("Jump Boost 1 ∞", "Speed 1 0:20", "+1 more effects"),
                CombatInfoModule.formatEffects(effects, 2));
        assertTrue(CombatInfoModule.formatEffects(effects, 0).isEmpty());
        assertEquals("Strength 2 1:05", CombatInfoModule.formatEffect("Strength", 1, 1300));
        assertEquals("Unknown 1 ∞", CombatInfoModule.formatEffect(" ", -1, -1));
    }

    @Test
    void defensiveReadoutsUseStableClampedFormatting() {
        assertEquals("Absorption 2.5", CombatInfoModule.formatAbsorption(2.46f));
        assertEquals("Absorption 0.0", CombatInfoModule.formatAbsorption(-3.0f));
        assertEquals("Absorption 0.0", CombatInfoModule.formatAbsorption(Float.NaN));
        assertEquals("Armor 20", CombatInfoModule.formatArmor(20));
        assertEquals("Armor 0", CombatInfoModule.formatArmor(-1));
    }
}
