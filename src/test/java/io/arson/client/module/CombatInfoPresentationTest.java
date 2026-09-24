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
        assertTrue(module.showTargetItem());
        assertTrue(module.showTargetDurability());
        assertTrue(module.showTargetEquipment());
        assertEquals(4, module.effectLimit());
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-absorption")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-armor")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-effects")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-target-item")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-target-durability")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("show-target-equipment")));
        assertTrue(module.settings().stream().anyMatch(setting -> setting.id().equals("absorption-color")));
        assertEquals(0xFFFFAA00, module.absorptionColor());
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
        var targetItem = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-target-item")).findFirst().orElseThrow();
        var targetDurability = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-target-durability")).findFirst().orElseThrow();
        var targetEquipment = (io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("show-target-equipment")).findFirst().orElseThrow();
        absorption.set(false);
        armor.set(false);
        effects.set(false);
        targetItem.set(false);
        targetDurability.set(false);
        targetEquipment.set(false);
        assertFalse(module.showAbsorption());
        assertFalse(module.showArmor());
        assertFalse(module.showEffects());
        assertFalse(module.showTargetItem());
        assertFalse(module.showTargetDurability());
        assertFalse(module.showTargetEquipment());
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
    void targetEquipmentRowsFormatMissingDurabilityAndSanitizeLongOffhandNames() {
        assertEquals(java.util.List.of(
                        "Target armor H 100% C — L 0% F 52%",
                        "Target offhand Totem of Undying"),
                CombatInfoModule.formatTargetEquipment(100, -1, -4, 52, "Totem of Undying"));
        assertEquals("Target offhand Empty",
                CombatInfoModule.formatTargetEquipment(-1, -1, -1, -1, " ").get(1));
        assertEquals(32, CombatInfoModule.formatTargetEquipment(0, 0, 0, 0,
                "An intentionally extremely long offhand item display name").get(1).substring("Target offhand ".length()).length());
    }

    @Test
    void healthBarSeparatesHealthAndAbsorptionWithSafeClamping() {
        var segments = CombatInfoModule.healthBarSegments(10.0f, 20.0f, 5.0f);
        assertEquals(0.5f, segments.health(), 0.0001f);
        assertEquals(0.25f, segments.absorption(), 0.0001f);

        var full = CombatInfoModule.healthBarSegments(20.0f, 20.0f, 8.0f);
        assertEquals(1.0f, full.health(), 0.0001f);
        assertEquals(0.0f, full.absorption(), 0.0001f);

        var invalid = CombatInfoModule.healthBarSegments(Float.NaN, Float.NaN, -4.0f);
        assertEquals(0.0f, invalid.health());
        assertEquals(0.0f, invalid.absorption());
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
