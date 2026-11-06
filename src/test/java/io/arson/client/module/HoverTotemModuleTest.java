package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoverTotemModuleTest {
    @Test
    void recognizesOnlyPlayerInventoryAndHotbarMenuSlots() {
        assertFalse(HoverTotemModule.isPlayerInventoryMenuSlot(8));
        assertTrue(HoverTotemModule.isPlayerInventoryMenuSlot(9));
        assertTrue(HoverTotemModule.isPlayerInventoryMenuSlot(35));
        assertTrue(HoverTotemModule.isPlayerInventoryMenuSlot(36));
        assertTrue(HoverTotemModule.isPlayerInventoryMenuSlot(44));
        assertFalse(HoverTotemModule.isPlayerInventoryMenuSlot(45));
    }
}
