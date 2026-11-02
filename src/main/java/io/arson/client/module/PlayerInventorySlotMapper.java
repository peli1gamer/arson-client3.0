package io.arson.client.module;

/** Maps Player Inventory container indices to Player Inventory menu slot ids. */
final class PlayerInventorySlotMapper {
    private PlayerInventorySlotMapper() {}

    static int menuSlotId(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot <= 8) return inventorySlot + 36;
        if (inventorySlot >= 9 && inventorySlot <= 35) return inventorySlot;
        if (inventorySlot >= 36 && inventorySlot <= 39) return 44 - inventorySlot;
        if (inventorySlot == 40) return 45;
        return -1;
    }
}
