package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerEquipmentModuleTest {
    @Test
    void exposesEmptySafeArmorSlotState() {
        PlayerEquipmentModule module = new PlayerEquipmentModule();

        assertEquals(0, module.equippedArmor());
        assertEquals(-1, module.helmetDurabilityPercent());
        assertEquals(-1, module.chestplateDurabilityPercent());
        assertEquals(-1, module.leggingsDurabilityPercent());
        assertEquals(-1, module.bootsDurabilityPercent());
        assertEquals("Armor H — C — L — F —", module.formattedArmorSlots());
    }

    @Test
    void formatsArmorSlotsIndividuallyAndClampsInvalidValues() {
        assertEquals("Armor H 100% C 52% L — F —",
                PlayerEquipmentModule.formatArmorSlots(100, 52, -1, -1));
        assertEquals("Armor H 100% C — L 75% F 100%",
                PlayerEquipmentModule.formatArmorSlots(140, -4, 75, 100));
    }
}
