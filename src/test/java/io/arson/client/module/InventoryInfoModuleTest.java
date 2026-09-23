package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryInfoModuleTest {
    @Test
    void moduleHasStableDiscoveryMetadata() {
        InventoryInfoModule module = new InventoryInfoModule();
        assertEquals("inventory-info", module.id());
        assertEquals("Inventory Info", module.name());
        assertEquals(Module.Category.PLAYER, module.category());
        assertEquals("Inventory Info [Player]: Tracks local inventory occupancy, selected slot, and held item for HUD presentation.", module.help());
    }

    @Test
    void formattedValueUsesOccupancyAndCapacity() {
        InventoryInfoModule module = new InventoryInfoModule();
        assertEquals("Inventory 0/0", module.formatted());
    }

    @Test
    void detailedFormatterReportsFreeSpaceAndSelectedItemSafely() {
        assertEquals("Inventory 12/36  Free 24  Slot 3: Diamond Pickaxe",
                InventoryInfoModule.formatDetails(12, 36, 3, " Diamond Pickaxe "));
        assertEquals("Inventory 0/0  Free 0  Slot —: Empty",
                InventoryInfoModule.formatDetails(-5, -4, 0, null));
        assertEquals("Inventory 4/4  Free 0  Slot 1: Empty",
                InventoryInfoModule.formatDetails(9, 4, 1, " "));
    }
}
