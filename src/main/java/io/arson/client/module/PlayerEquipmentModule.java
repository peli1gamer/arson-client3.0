package io.arson.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Tracks the local player's equipped armor and offhand state for HUD/addon presentation. */
public final class PlayerEquipmentModule extends Module {
    private int equippedArmor;
    private int armorDurabilityPercent;
    private String mainHand = "Empty";
    private String offHand = "Empty";

    public PlayerEquipmentModule() {
        super("player-equipment", "Player Equipment", Category.PLAYER,
                "Tracks live armor occupancy, armor durability, main-hand item, and off-hand item for HUD presentation.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) {
            equippedArmor = 0;
            armorDurabilityPercent = 0;
            mainHand = offHand = "Empty";
            return;
        }
        int equipped = 0, remaining = 0, maximum = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            equipped++;
            if (stack.isDamageableItem()) {
                remaining += stack.getMaxDamage() - stack.getDamageValue();
                maximum += stack.getMaxDamage();
            }
        }
        equippedArmor = equipped;
        armorDurabilityPercent = maximum == 0 ? 0 : Math.round(remaining * 100.0f / maximum);
        ItemStack hand = client.player.getMainHandItem();
        ItemStack off = client.player.getOffhandItem();
        mainHand = hand.isEmpty() ? "Empty" : hand.getHoverName().getString();
        offHand = off.isEmpty() ? "Empty" : off.getHoverName().getString();
    }

    public int equippedArmor() { return equippedArmor; }
    public int armorDurabilityPercent() { return armorDurabilityPercent; }
    public String mainHand() { return mainHand; }
    public String offHand() { return offHand; }
    public String formatted() { return "Gear " + equippedArmor + "/4  Armor " + armorDurabilityPercent + "%  Main " + mainHand + "  Off " + offHand; }
}
