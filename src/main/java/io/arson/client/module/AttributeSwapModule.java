package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

/** Unified combat weapon selector. It scans the hotbar and temporarily selects the configured weapon. */
public final class AttributeSwapModule extends Module {
    private final BooleanSetting mace = setting(new BooleanSetting("mace", "Mace", true));
    private final BooleanSetting maceBreach = setting(new BooleanSetting("mace-breach", "Breach", true));
    private final BooleanSetting maceWindBurst = setting(new BooleanSetting("mace-wind-burst", "Wind Burst", false));
    private final BooleanSetting maceDensity = setting(new BooleanSetting("mace-density", "Density", false));
    private final DoubleSetting maceFallHeight = setting(new DoubleSetting("mace-fall-height", "Mace Fall Height", 3.0, 0.0, 20.0, 0.5));
    private final BooleanSetting axe = setting(new BooleanSetting("axe", "Axe", false));
    private final BooleanSetting sword = setting(new BooleanSetting("sword", "Sword", true));
    private final BooleanSetting spear = setting(new BooleanSetting("spear", "Spear", false));
    private final BooleanSetting returnToPrevious = setting(new BooleanSetting("return-to-previous", "Return To Previous", true));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Target Range", 5.0, 1.0, 16.0, 0.5));
    private final DoubleSetting maceSlot = setting(new DoubleSetting("mace-slot", "Mace Slot", 0.0, 0.0, 9.0, 1.0));
    private final DoubleSetting axeSlot = setting(new DoubleSetting("axe-slot", "Axe Slot", 0.0, 0.0, 9.0, 1.0));
    private final DoubleSetting swordSlot = setting(new DoubleSetting("sword-slot", "Sword Slot", 0.0, 0.0, 9.0, 1.0));
    private final DoubleSetting spearSlot = setting(new DoubleSetting("spear-slot", "Spear Slot", 0.0, 0.0, 9.0, 1.0));
    private int previousSlot = -1;
    private String activeSwap = "";
    public AttributeSwapModule() { super("attribute-swap", "Attribute Swap", Category.COMBAT); }
    @Override protected void onDisable() { restorePrevious(MinecraftClient.getInstance()); }
    @Override protected void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null) return;
        LivingEntity target = findTarget(client);
        boolean fallingMace = mace.enabled() && target != null && !client.player.isOnGround() && client.player.getVelocity().y < 0.0 && client.player.fallDistance >= maceFallHeight.get() && findMaceSlot(client.player) >= 0;
        if (fallingMace) { switchTo(client, "mace", maceSlot, true); return; }
        if (client.options.attackKey.isPressed() && target != null) {
            if (sword.enabled() && findWeaponSlot(client.player, "sword", swordSlot) >= 0) { switchTo(client, "sword", swordSlot, false); return; }
            if (axe.enabled() && findWeaponSlot(client.player, "axe", axeSlot) >= 0) { switchTo(client, "axe", axeSlot, false); return; }
            if (spear.enabled() && findWeaponSlot(client.player, "spear", spearSlot) >= 0) { switchTo(client, "spear", spearSlot, false); return; }
        }
        if (!activeSwap.isEmpty() && (client.player.isOnGround() || !client.options.attackKey.isPressed())) restorePrevious(client);
    }
    private LivingEntity findTarget(MinecraftClient client) {
        double maxSq = range.get() * range.get();
        FriendsModule friends = clientModules(client, "friends", FriendsModule.class);
        return client.world.getEntitiesByClass(LivingEntity.class, client.player.getBoundingBox().expand(range.get()), entity -> entity != client.player && entity.isAlive() && !entity.isRemoved() && client.player.squaredDistanceTo(entity) <= maxSq && !(entity instanceof PlayerEntity player && friends != null && friends.isFriend(player)) && allowedEntity(entity)).stream().min(java.util.Comparator.comparingDouble(client.player::squaredDistanceTo)).orElse(null);
    }
    private boolean allowedEntity(LivingEntity entity) { return entity instanceof PlayerEntity || entity instanceof HostileEntity || entity instanceof AnimalEntity; }
    private void switchTo(MinecraftClient client, String reason, DoubleSetting explicitSlot, boolean maceReason) {
        int slot = maceReason ? findMaceSlot(client.player) : findWeaponSlot(client.player, reason, explicitSlot);
        if (slot < 0) return;
        if (client.player.getInventory().getSelectedSlot() == slot) { if (activeSwap.isEmpty()) activeSwap = reason; return; }
        if (activeSwap.isEmpty()) previousSlot = client.player.getInventory().getSelectedSlot();
        activeSwap = reason; client.player.getInventory().setSelectedSlot(slot);
    }
    private void restorePrevious(MinecraftClient client) {
        if (!returnToPrevious.enabled() || previousSlot < 0 || client.player == null) { previousSlot = -1; activeSwap = ""; return; }
        client.player.getInventory().setSelectedSlot(previousSlot); previousSlot = -1; activeSwap = "";
    }
    private int findMaceSlot(PlayerEntity player) {
        int explicit = configuredSlot(player, maceSlot, item -> item.isOf(Items.MACE));
        if (explicit >= 0) return explicit;
        for (int i = 0; i < 9; i++) { ItemStack stack = player.getInventory().getStack(i); if (stack.isOf(Items.MACE) && matchesMaceFilters(stack)) return i; }
        return -1;
    }
    private int findWeaponSlot(PlayerEntity player, String type, DoubleSetting setting) {
        int explicit = configuredSlot(player, setting, stack -> isWeapon(stack, type));
        if (explicit >= 0) return explicit;
        for (int i = 0; i < 9; i++) if (isWeapon(player.getInventory().getStack(i), type)) return i;
        return -1;
    }
    private int configuredSlot(PlayerEntity player, DoubleSetting setting, java.util.function.Predicate<ItemStack> predicate) {
        int configured = (int) Math.round(setting.get()) - 1;
        if (configured < 0 || configured > 8) return -1;
        return predicate.test(player.getInventory().getStack(configured)) ? configured : -1;
    }
    private boolean isWeapon(ItemStack stack, String type) {
        Item item = stack.getItem();
        return switch (type) {
            case "sword" -> item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.COPPER_SWORD || item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD;
            case "axe" -> item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.COPPER_AXE || item == Items.IRON_AXE || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE;
            case "spear" -> item == Items.WOODEN_SPEAR || item == Items.STONE_SPEAR || item == Items.COPPER_SPEAR || item == Items.IRON_SPEAR || item == Items.GOLDEN_SPEAR || item == Items.DIAMOND_SPEAR || item == Items.NETHERITE_SPEAR;
            default -> false;
        };
    }
    private boolean matchesMaceFilters(ItemStack stack) {
        boolean any = maceBreach.enabled() || maceWindBurst.enabled() || maceDensity.enabled();
        if (!any) return true;
        var enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) return false;
        Set<RegistryEntry<Enchantment>> holders = enchantments.getEnchantments();
        for (RegistryEntry<Enchantment> holder : holders) {
            String path = holder.getKey().map(key -> key.getValue().getPath()).orElse("");
            if (maceBreach.enabled() && path.equals("breach")) return true;
            if (maceWindBurst.enabled() && path.equals("wind_burst")) return true;
            if (maceDensity.enabled() && path.equals("density")) return true;
        }
        return false;
    }
    @SuppressWarnings("unchecked") private static <T extends Module> T clientModules(MinecraftClient client, String id, Class<T> type) {
        if (io.arson.client.ArsonClient.getInstance() == null) return null;
        Module module = io.arson.client.ArsonClient.getInstance().modules().get(id);
        return type.isInstance(module) ? (T) module : null;
    }
}
