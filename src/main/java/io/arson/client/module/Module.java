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
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String id, String name, Category category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }

    public final void tick(Minecraft client) {
        if (enabled) onTick(client);
    }

    protected final <T extends Setting<?>> T setting(T setting) {
        settings.add(setting);
        return setting;
    }

    protected void onEnable() {}
    protected void onDisable() {}
    protected void onTick(Minecraft client) {}

    public String id() { return id; }
    public String name() { return name; }
    public Category category() { return category; }
    public boolean enabled() { return enabled; }
    public List<Setting<?>> settings() { return Collections.unmodifiableList(settings); }

    public enum Category {
        COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"),
        PLAYER("Player"), WORLD("World"), MISC("Misc");

        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }
}
