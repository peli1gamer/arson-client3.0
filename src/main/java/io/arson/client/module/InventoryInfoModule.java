package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Reports local inventory occupancy and selected-slot state for HUD integrations. */
public final class InventoryInfoModule extends Module {
    private int occupied;
    private int capacity;
    private int selectedSlot;
    private String selectedItem = "Empty";

    public InventoryInfoModule() {
        super("inventory-info", "Inventory Info", Category.PLAYER,
                "Tracks local inventory occupancy, selected slot, and held item for HUD presentation.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null) {
            occupied = 0;
            capacity = 0;
            selectedSlot = 0;
            selectedItem = "Empty";
            return;
        }
        var inventory = client.player.getInventory();
        capacity = inventory.getContainerSize();
        occupied = 0;
        for (int i = 0; i < capacity; i++) {
            if (!inventory.getItem(i).isEmpty()) occupied++;
        }
        selectedSlot = inventory.getSelectedSlot() + 1;
        var stack = client.player.getMainHandItem();
        selectedItem = stack.isEmpty() ? "Empty" : stack.getHoverName().getString();
    }

    public int occupied() { return occupied; }
    public int capacity() { return capacity; }
    public int selectedSlot() { return selectedSlot; }
    public String selectedItem() { return selectedItem; }
    public String formatted() { return "Inventory " + occupied + "/" + capacity; }
}
