package io.arson.client.module;

import io.arson.client.accessor.HandledScreenAccessor;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

/** Equips a hovered totem from the player's inventory into the offhand. */
public final class HoverTotemModule extends Module {
    private final BooleanSetting hotbarTotem = setting(new BooleanSetting("hotbar-totem", "Hotbar Totem", true));
    private final DoubleSetting hotbarSlot = setting(new DoubleSetting("hotbar-slot", "Hotbar Slot", 1.0, 1.0, 9.0, 1.0));
    private final BooleanSetting autoSwitch = setting(new BooleanSetting("auto-switch", "Auto Switch", false));
    private final BooleanSetting autoOpenInventory = setting(new BooleanSetting("auto-open-inventory", "Auto Open Inventory", false));
    private final BooleanSetting replaceOffhand = setting(new BooleanSetting("replace-offhand", "Replace Offhand", true));
    private final BooleanSetting requireHover = setting(new BooleanSetting("require-hover", "Require Hover", true));
    private final BooleanSetting onlyPlayerInventory = setting(new BooleanSetting("player-inventory-only", "Player Inventory Only", true));
    public HoverTotemModule() { super("hover-totem", "Hover Totem", Category.COMBAT); }
    @Override protected void onTick(Minecraft client) {
        if (client.player == null || client.gameMode == null) return;
        if (!(client.screen instanceof InventoryScreen screen)) {
            if (autoOpenInventory.enabled() && !client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) && hasTotem(client)) client.setScreen(new InventoryScreen(client.player));
            return;
        }
        Slot hovered = null;
        if (screen instanceof HandledScreenAccessor accessor) hovered = accessor.arson$getHoveredSlot();

        int sourceInventorySlot = -1;
        if (requireHover.enabled()) {
            if (hovered == null || !hovered.hasItem()
                    || !hovered.getItem().is(Items.TOTEM_OF_UNDYING)) return;
            sourceInventorySlot = hovered.getContainerSlot();
            if (onlyPlayerInventory.enabled() && (sourceInventorySlot < 0 || sourceInventorySlot > 35)) return;
        } else if (hovered != null && hovered.hasItem()
                && hovered.getItem().is(Items.TOTEM_OF_UNDYING)
                && (!onlyPlayerInventory.enabled()
                || (hovered.getContainerSlot() >= 0 && hovered.getContainerSlot() <= 35))) {
            sourceInventorySlot = hovered.getContainerSlot();
        } else {
            sourceInventorySlot = findTotemInventorySlot(client);
        }

        int source = PlayerInventorySlotMapper.menuSlotId(sourceInventorySlot);
        if (source < 0) return;

        int hotbarIndex = hotbarSlot.get().intValue() - 1;
        int syncId = screen.getMenu().containerId;
        if (replaceOffhand.enabled() && !client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            client.gameMode.handleInventoryMouseClick(syncId, source, 40, ClickType.SWAP, client.player);
            return;
        }
        if (hotbarTotem.enabled() && !client.player.getInventory().getItem(hotbarIndex).is(Items.TOTEM_OF_UNDYING)) {
            client.gameMode.handleInventoryMouseClick(syncId, source, hotbarIndex, ClickType.SWAP, client.player);
            if (autoSwitch.enabled()) client.player.getInventory().setSelectedSlot(hotbarIndex);
        }
    }
    private int findTotemInventorySlot(Minecraft client) {
        for (int i = 0; i < 36; i++) {
            if (client.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }

    private boolean hasTotem(Minecraft client) { return findTotemInventorySlot(client) >= 0; }
}
