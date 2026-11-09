package io.arson.client.module;

import io.arson.client.accessor.HandledScreenAccessor;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

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
    @Override protected void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (!(client.currentScreen instanceof InventoryScreen screen)) {
            if (autoOpenInventory.enabled() && !client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) && hasTotem(client)) client.setScreen(new InventoryScreen(client.player));
            return;
        }
        Slot hovered = null;
        if (screen instanceof HandledScreenAccessor accessor) hovered = accessor.arson$getHoveredSlot();
        if (!requireHover.enabled()) {
            if (hovered == null || !hovered.hasStack() || !hovered.getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                hovered = null;
                for (Slot slot : screen.getScreenHandler().slots) {
                    if (slot.getIndex() >= 0 && slot.getIndex() <= 35
                            && slot.hasStack() && slot.getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                        hovered = slot;
                        break;
                    }
                }
            }
        }
        if (hovered == null || !hovered.hasStack()) return;
        if (onlyPlayerInventory.enabled() && hovered.getIndex() > 35) return;
        if (!hovered.getStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        int hotbarIndex = hotbarSlot.get().intValue() - 1;
        if (autoSwitch.enabled()) client.player.getInventory().setSelectedSlot(hotbarIndex);
        int syncId = screen.getScreenHandler().syncId;
        int source = hovered.getIndex();
        if (replaceOffhand.enabled() && !client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) { client.interactionManager.clickSlot(syncId, source, 40, SlotActionType.SWAP, client.player); return; }
        if (hotbarTotem.enabled() && !client.player.getInventory().getStack(hotbarIndex).isOf(Items.TOTEM_OF_UNDYING)) client.interactionManager.clickSlot(syncId, source, hotbarIndex, SlotActionType.SWAP, client.player);
    }
    private boolean hasTotem(MinecraftClient client) { for (int i = 0; i < 36; i++) if (client.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return true; return false; }
}
