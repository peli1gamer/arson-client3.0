package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;

/** Player-state HUD configuration. Information only; no gameplay automation. */
public final class PlayerInfoModule extends Module {
    private final BooleanSetting health = setting(new BooleanSetting("health", "Health", true));
    private final BooleanSetting hunger = setting(new BooleanSetting("hunger", "Hunger", true));
    private final BooleanSetting armor = setting(new BooleanSetting("armor", "Armor", true));
    private final BooleanSetting heldItem = setting(new BooleanSetting("held-item", "Held Item", true));
    private final BooleanSetting inventory = setting(new BooleanSetting("inventory", "Inventory", true));

    public PlayerInfoModule() {
        super("player-info", "Player Info", Category.PLAYER, "Displays local health, hunger, armor, held item, and inventory occupancy in the HUD.");
    }

    public boolean showHealth() { return health.enabled(); }
    public boolean showHunger() { return hunger.enabled(); }
    public boolean showArmor() { return armor.enabled(); }
    public boolean showHeldItem() { return heldItem.enabled(); }
    public boolean showInventory() { return inventory.enabled(); }
}
