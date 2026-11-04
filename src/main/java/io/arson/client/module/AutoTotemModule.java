package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

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
    protected void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null || client.currentScreen != null) return;
        if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        boolean emergency = client.player.getHealth() <= healthThreshold.get();
        if (!always.enabled() && !emergency) return;

        int source = findTotem(client);
        if (source < 0) return;

        // Inventory click slot 40 is the player's offhand slot in the player container.
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, source, 40, SlotActionType.SWAP, client.player);
    }

    private int findTotem(MinecraftClient client) {
        if (includeHotbar.enabled()) {
            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
            }
        }

        for (int i = 9; i < 36; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
