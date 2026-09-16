package io.arson.client.module;

import io.arson.client.settings.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String id;
    private final String name;
    private final Category category;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private boolean favorite;
    private int keyCode;

    protected Module(String id, String name, Category category) { this(id, name, category, ""); }
    protected Module(String id, String name, Category category, String description) { this(id, name, category, description, 0); }
    protected Module(String id, String name, Category category, int defaultKeyCode) { this(id, name, category, "", defaultKeyCode); }
    protected Module(String id, String name, Category category, String description, int defaultKeyCode) {
        this.id = id; this.name = name; this.category = category; this.description = description == null ? "" : description; this.keyCode = defaultKeyCode;
    }
    public final void setEnabled(boolean enabled) { if (this.enabled == enabled) return; this.enabled = enabled; if (enabled) onEnable(); else onDisable(); }
    public final void toggle() { setEnabled(!enabled); }
    public final void resetSettings() { for (Setting<?> setting : settings) setting.reset(); }
    public final void resetToDefaults() { setEnabled(false); resetSettings(); favorite = false; keyCode = 0; }
    public final void tick(Minecraft client) { if (enabled) onTick(client); }
    protected final <T extends Setting<?>> T setting(T setting) { settings.add(setting); return setting; }
    protected void onEnable() {}
    protected void onDisable() {}
    protected void onTick(Minecraft client) {}
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
    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"), WORLD("World"), MISC("Misc");
        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }
}
