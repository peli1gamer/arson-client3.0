package io.arson.client.module;

import io.arson.client.settings.Setting;
import io.arson.client.settings.SettingGroup;
import meteordevelopment.meteorclient.MeteorClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;

/**
 * Meteor-compatible module lifecycle and setting-group foundation.
 *
 * Arson keeps its existing Mojang-mapped Minecraft runtime and public accessors;
 * the activation aliases and grouped settings provide the compatibility surface
 * needed while Meteor-derived modules are ported.
 */
public abstract class Module {
    private final String id;
    private final String name;
    private final Category category;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();
    private final List<SettingGroup> settingGroups = new ArrayList<>();
    private final SettingGroup defaultGroup = new SettingGroup("general", "General", "General module settings.");
    private boolean enabled;
    private boolean favorite;
    private int keyCode;

    /** Mirrors Meteor's option to keep a module active while the game is in menus. */
    public boolean runInMainMenu;
    /** Mirrors Meteor's module chat-feedback preference. */
    public boolean chatFeedback = true;
    /** Mirrors Meteor's keybind-release toggle option. */
    public boolean toggleOnBindRelease;

    protected Module(String id, String name, Category category) { this(id, name, category, ""); }
    protected Module(String id, String name, Category category, String description) { this(id, name, category, description, 0); }
    protected Module(String id, String name, Category category, int defaultKeyCode) { this(id, name, category, "", defaultKeyCode); }
    protected Module(String id, String name, Category category, String description, int defaultKeyCode) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description == null ? "" : description;
        this.keyCode = Math.max(0, defaultKeyCode);
        settingGroups.add(defaultGroup);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            MeteorClient.EVENT_BUS.subscribe(this);
            onActivate();
        } else {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            onDeactivate();
        }
    }

    public final void toggle() { setEnabled(!enabled); }
    public final void enable() { setEnabled(true); }
    public final void disable() { setEnabled(false); }
    public final boolean isActive() { return enabled; }

    public final void resetSettings() {
        for (Setting<?> setting : settings) setting.reset();
    }

    public final void resetToDefaults() {
        setEnabled(false);
        resetSettings();
        favorite = false;
        keyCode = 0;
        toggleOnBindRelease = false;
        chatFeedback = true;
    }

    public final void tick(MinecraftClient client) {
        if (enabled) onTick(client);
    }

    protected final <T extends Setting<?>> T setting(T setting) {
        settings.add(setting);
        defaultGroup.add(setting);
        return setting;
    }

    protected final SettingGroup createGroup(String id, String name, String description) {
        for (SettingGroup group : settingGroups) {
            if (group.id().equals(id)) throw new IllegalArgumentException("Duplicate setting group: " + id);
        }
        SettingGroup group = new SettingGroup(id, name, description);
        settingGroups.add(group);
        return group;
    }

    protected final <T extends Setting<?>> T setting(SettingGroup group, T setting) {
        if (!settingGroups.contains(group)) throw new IllegalArgumentException("Setting group does not belong to module " + id);
        settings.add(setting);
        setting.group(group);
        return setting;
    }

    /** Legacy Arson hooks remain supported by the Meteor-style lifecycle aliases. */
    protected void onEnable() {}
    protected void onDisable() {}
    protected void onTick(MinecraftClient client) {}
    public void onActivate() { onEnable(); }
    public void onDeactivate() { onDisable(); }

    public String id() { return id; }
    public String name() { return name; }
    public Category category() { return category; }
    public String description() { return description; }
    public String help() { return description.isBlank() ? name + " [" + category.displayName() + "]" : name + " [" + category.displayName() + "]: " + description; }
    public boolean enabled() { return enabled; }
    public boolean favorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public int keyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = Math.max(0, keyCode); }
    public boolean hasKeybind() { return keyCode > 0; }
    public List<Setting<?>> settings() { return Collections.unmodifiableList(settings); }
    public List<SettingGroup> settingGroups() { return Collections.unmodifiableList(settingGroups); }
    public SettingGroup defaultGroup() { return defaultGroup; }

    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"), WORLD("World"), MISC("Misc");
        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }
}
