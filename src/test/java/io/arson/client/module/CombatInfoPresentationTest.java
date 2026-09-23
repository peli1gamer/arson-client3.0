package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatInfoPresentationTest {
    @Test
    void defensiveReadoutsAreEnabledAndConfigurableBySetting() {
        CombatInfoModule module = new CombatInfoModule();

        assertTrue(module.showAbsorption());
        assertTrue(module.showArmor());
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-absorption")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-armor")));

        var absorption = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-absorption")).findFirst().orElseThrow();
        var armor = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-armor")).findFirst().orElseThrow();
        absorption.set(false);
        armor.set(false);
        assertFalse(module.showAbsorption());
        assertFalse(module.showArmor());
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
