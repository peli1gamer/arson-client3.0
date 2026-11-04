package io.arson.client.module;

import io.arson.client.settings.Setting;
import io.arson.client.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;

/**
 * Arson adapter over Meteor's module base.
 *
 * The upstream lifecycle and Orbit subscription are inherited; Arson-specific
 * identifiers, settings, keybind storage and legacy hooks remain here.
 */
public abstract class Module extends meteordevelopment.meteorclient.systems.modules.Module {
    private final String id;
    private final Category arsonCategory;
    private final List<Setting<?>> arsonSettings = new ArrayList<>();
    private final List<SettingGroup> settingGroups = new ArrayList<>();
    private final SettingGroup defaultGroup = new SettingGroup("general", "General", "General module settings.");
    private final Keybind keybind = Keybind.none();

    protected Module(String id, String name, Category category) {
        this(id, name, category, "");
    }

    protected Module(String id, String name, Category category, String description) {
        this(id, name, category, description, 0);
    }

    protected Module(String id, String name, Category category, int defaultKeyCode) {
        this(id, name, category, "", defaultKeyCode);
    }

    protected Module(String id, String name, Category category, String description, int defaultKeyCode) {
        super(meteorCategory(category), name, description);
        this.id = id;
        this.arsonCategory = category;
        setKeyCode(defaultKeyCode);
        settingGroups.add(defaultGroup);
        settings.addGroup(defaultGroup);
    }

    public final void resetSettings() {
        settings.reset();
    }

    public final void resetToDefaults() {
        setEnabled(false);
        resetSettings();
        favorite = false;
        keybind.reset();
        toggleOnBindRelease = false;
        chatFeedback = true;
    }

    public final void tick(MinecraftClient client) {
        if (isActive()) onTick(client);
    }

    protected final <T extends Setting<?>> T setting(T setting) {
        arsonSettings.add(setting);
        defaultGroup.add(setting);
        return setting;
    }

    protected final SettingGroup createGroup(String id, String name, String description) {
        for (SettingGroup group : settingGroups) {
            if (group.id().equals(id)) throw new IllegalArgumentException("Duplicate setting group: " + id);
        }
        SettingGroup group = new SettingGroup(id, name, description);
        settingGroups.add(group);
        settings.addGroup(group);
        return group;
    }

    protected final <T extends Setting<?>> T setting(SettingGroup group, T setting) {
        if (!settingGroups.contains(group)) throw new IllegalArgumentException("Setting group does not belong to module " + id);
        arsonSettings.add(setting);
        setting.group(group);
        return setting;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onTick(MinecraftClient client) {
    }

    @Override
    public void onActivate() {
        onEnable();
    }

    @Override
    public void onDeactivate() {
        onDisable();
    }

    public String id() { return id; }
    public String name() { return name; }
    public Category category() { return arsonCategory; }
    public meteordevelopment.meteorclient.systems.modules.Category meteorCategory() { return category; }
    public String description() { return description; }
    public String help() {
        return description.isBlank()
            ? name + " [" + arsonCategory.displayName() + "]"
            : name + " [" + arsonCategory.displayName() + "]: " + description;
    }
    public boolean enabled() { return isActive(); }
    public boolean favorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public int keyCode() { return keybind.isKey() && keybind.isSet() ? keybind.getValue() : 0; }
    public void setKeyCode(int keyCode) { if (keyCode <= 0) keybind.reset(); else keybind.set(true, keyCode, 0); }
    public Keybind keybind() { return keybind; }
    public void setKeybind(Keybind value) { keybind.set(value); }
    public boolean hasKeybind() { return keybind.isSet(); }
    public List<Setting<?>> settings() { return Collections.unmodifiableList(arsonSettings); }
    public List<SettingGroup> settingGroups() { return Collections.unmodifiableList(settingGroups); }
    public SettingGroup defaultGroup() { return defaultGroup; }

    private static meteordevelopment.meteorclient.systems.modules.Category meteorCategory(Category category) {
        return switch (category) {
            case COMBAT -> Categories.Combat;
            case MOVEMENT -> Categories.Movement;
            case RENDER -> Categories.Render;
            case PLAYER -> Categories.Player;
            case WORLD -> Categories.World;
            case MISC -> Categories.Misc;
        };
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
