package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/** Automatically keeps a totem in the offhand when configured conditions are met. */
public final class AutoTotemModule extends Module {
    private final DoubleSetting healthThreshold = setting(new DoubleSetting("health-threshold", "Health Threshold", 8.0, 1.0, 20.0, 0.5));
    private final BooleanSetting always = setting(new BooleanSetting("always", "Always", true));
    private final BooleanSetting includeHotbar = setting(new BooleanSetting("include-hotbar", "Include Hotbar", true));
    private final BooleanSetting preserveHeld = setting(new BooleanSetting("preserve-held", "Preserve Held Item", true));

    public AutoTotemModule() {
        super("auto-totem", "Auto Totem", Category.COMBAT);
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null || client.gameMode == null || client.screen != null) return;
        if (client.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        boolean emergency = client.player.getHealth() <= healthThreshold.get();
        if (!always.enabled() && !emergency) return;

        int inventoryIndex = findTotem(client);
        int sourceMenuSlot = menuSlotForInventoryIndex(inventoryIndex);
        if (sourceMenuSlot < 0) return;

        // SWAP's slot argument is a menu slot, not a raw PlayerInventory index.
        client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, sourceMenuSlot, 40, ClickType.SWAP, client.player);
    }

    private int findTotem(Minecraft client) {
        if (includeHotbar.enabled()) {
            int selectedSlot = client.player.getInventory().getSelectedSlot();
            for (int i = 0; i < 9; i++) {
                if (preserveHeld.enabled() && i == selectedSlot) continue;
                if (client.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return i;
            }
        }

        for (int i = 9; i < 36; i++) {
            if (client.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }

    static int menuSlotForInventoryIndex(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= 36) return -1;
        return inventoryIndex < 9 ? inventoryIndex + 36 : inventoryIndex;
    }
}
