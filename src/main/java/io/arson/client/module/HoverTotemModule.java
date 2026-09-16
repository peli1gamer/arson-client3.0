package io.arson.client.module;

import io.arson.client.mixin.HandledScreenAccessor;
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

    public HoverTotemModule() {
        super("hover-totem", "Hover Totem", Category.COMBAT);
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null || client.gameMode == null) return;

        if (!(client.screen instanceof InventoryScreen screen)) {
            if (autoOpenInventory.enabled()
                && !client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                && hasTotem(client)) {
                client.setScreen(new InventoryScreen(client.player));
            }
            return;
        }

        Slot hovered = null;
        if (screen instanceof HandledScreenAccessor accessor) hovered = accessor.arson$getHoveredSlot();
        if (hovered == null || !hovered.hasItem()) return;
        if (onlyPlayerInventory.enabled() && hovered.getContainerSlot() > 35) return;
        if (!hovered.getItem().is(Items.TOTEM_OF_UNDYING)) return;

        if (autoSwitch.enabled()) {
            client.player.getInventory().setSelectedSlot((int) hotbarSlot.get() - 1);
        }

        int syncId = screen.getMenu().containerId;
        int source = hovered.getContainerSlot();

        if (replaceOffhand.enabled() && !client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            client.gameMode.handleInventoryMouseClick(syncId, source, 40, ClickType.SWAP, client.player);
            return;
        }

        if (hotbarTotem.enabled()) {
            int hotbarIndex = (int) hotbarSlot.get() - 1;
            if (!client.player.getInventory().getItem(hotbarIndex).is(Items.TOTEM_OF_UNDYING)) {
                client.gameMode.handleInventoryMouseClick(syncId, source, hotbarIndex, ClickType.SWAP, client.player);
            }
        }
    }

    private boolean hasTotem(Minecraft client) {
        for (int i = 0; i < 36; i++) {
            if (client.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return true;
        }
        return false;
    }
}
