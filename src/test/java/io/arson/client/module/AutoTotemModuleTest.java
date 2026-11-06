package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoTotemModuleTest {
    @Test
    void mapsHotbarInventoryIndicesToPlayerMenuSlots() {
        for (int inventoryIndex = 0; inventoryIndex < 9; inventoryIndex++) {
            assertEquals(inventoryIndex + 36, AutoTotemModule.menuSlotForInventoryIndex(inventoryIndex));
        }
    }

    @Test
    void leavesMainInventoryIndicesUnchanged() {
        for (int inventoryIndex = 9; inventoryIndex < 36; inventoryIndex++) {
            assertEquals(inventoryIndex, AutoTotemModule.menuSlotForInventoryIndex(inventoryIndex));
        }
    }

    @Test
    void rejectsIndicesOutsidePlayerInventory() {
        assertEquals(-1, AutoTotemModule.menuSlotForInventoryIndex(-1));
        assertEquals(-1, AutoTotemModule.menuSlotForInventoryIndex(36));
    }
}
