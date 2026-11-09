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
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;

public abstract class Module implements ISerializable<Module> {
    protected final MinecraftClient mc;

    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final Settings settings = new Settings();

    private boolean active;

    public boolean serialize = true;
    public boolean runInMainMenu;
    public boolean autoSubscribe = true;
    public boolean chatFeedback = true;
    public boolean toggleOnBindRelease;
    public boolean favorite;
    public final Keybind keybind = Keybind.none();

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
            Modules manager = Modules.get();
            active = true;
            if (manager != null) manager.addActive(this);

            try {
                settings.onActivated();
                if (autoSubscribe) MeteorClient.EVENT_BUS.subscribe(this);
                onActivate();
            } catch (RuntimeException | Error exception) {
                if (autoSubscribe) MeteorClient.EVENT_BUS.unsubscribe(this);
                active = false;
                if (manager != null) manager.removeActive(this);
                throw exception;
            }
        } else {
            Modules manager = Modules.get();
            try {
                if (autoSubscribe) MeteorClient.EVENT_BUS.unsubscribe(this);
                onDeactivate();
            } finally {
                active = false;
                if (manager != null) manager.removeActive(this);
            }
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

    @Override
    public NbtCompound toTag() {
        if (!serialize) return null;
        NbtCompound tag = new NbtCompound();
        tag.putString("name", name);
        tag.put("keybind", keybind.toTag());
        tag.putBoolean("toggleOnKeyRelease", toggleOnBindRelease);
        tag.putBoolean("chatFeedback", chatFeedback);
        tag.putBoolean("favorite", favorite);
        tag.put("settings", settings.toTag());
        tag.putBoolean("active", active);
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        keybind.fromTag(tag.getCompoundOrEmpty("keybind"));
        toggleOnBindRelease = tag.getBoolean("toggleOnKeyRelease", false);
        chatFeedback = tag.getBoolean("chatFeedback", true);
        favorite = tag.getBoolean("favorite", false);
        settings.fromTag(tag.getCompoundOrEmpty("settings"));
        setEnabled(tag.getBoolean("active", false));
        return this;
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
