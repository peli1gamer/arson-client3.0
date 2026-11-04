/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson-specific integration retains the upstream module lifecycle while leaving
 * Arson's existing settings, persistence, and keybind adapters in its subclass.
 */
package meteordevelopment.meteorclient.systems.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected final MinecraftClient mc;

    public final Category category;
    public final String name;
    public final String title;
    public final String description;

    private boolean active;

    public boolean serialize = true;
    public boolean runInMainMenu;
    public boolean autoSubscribe = true;
    public boolean chatFeedback = true;
    public boolean toggleOnBindRelease;
    public boolean favorite;

    protected Module(Category category, String name, String description) {
        this.mc = MinecraftClient.getInstance();
        this.category = category;
        this.name = name;
        this.title = toTitle(name);
        this.description = description == null ? "" : description;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public final void toggle() {
        if (!active) {
            active = true;
            settings.onActivated();
            if (autoSubscribe) MeteorClient.EVENT_BUS.subscribe(this);
            onActivate();
        } else {
            if (autoSubscribe) MeteorClient.EVENT_BUS.unsubscribe(this);
            onDeactivate();
            active = false;
        }
    }

    public final void setEnabled(boolean enabled) {
        if (active != enabled) toggle();
    }

    public final void enable() {
        if (!isActive()) toggle();
    }

    public final void disable() {
        if (isActive()) toggle();
    }

    public final boolean isActive() {
        return active;
    }

    private static String toTitle(String name) {
        if (name == null || name.isBlank()) return "";
        StringBuilder result = new StringBuilder(name.length() + 8);
        boolean capitalize = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '-' || c == '_' || c == ' ') {
                result.append(' ');
                capitalize = true;
            } else {
                result.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            }
        }
        return result.toString();
    }
}
