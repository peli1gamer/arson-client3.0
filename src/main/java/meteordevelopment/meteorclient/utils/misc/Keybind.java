/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson port of Meteor's keybind value model. Input polling is performed by the
 * client adapter so the same model can be tested without a live GLFW window.
 */
package meteordevelopment.meteorclient.utils.misc;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import org.lwjgl.glfw.GLFW;

public final class Keybind {
    private boolean key;
    private int value;
    private int modifiers;

    private Keybind(boolean key, int value, int modifiers) {
        set(key, value, modifiers);
    }

    public static Keybind none() {
        return new Keybind(true, GLFW.GLFW_KEY_UNKNOWN, 0);
    }

    public static Keybind fromKey(int key) {
        return new Keybind(true, key, 0);
    }

    public static Keybind fromKeys(int key, int modifiers) {
        return new Keybind(true, key, modifiers);
    }

    public static Keybind fromButton(int button) {
        return new Keybind(false, button, 0);
    }

    public int getValue() { return value; }
    public int getModifiers() { return modifiers; }
    public boolean isKey() { return key; }
    public boolean isSet() { return key ? value != GLFW.GLFW_KEY_UNKNOWN : value >= 0; }
    public boolean hasMods() { return key && modifiers != 0; }

    public void set(boolean key, int value, int modifiers) {
        this.key = key;
        this.value = value;
        this.modifiers = key ? modifiers : 0;
    }

    public Keybind set(Keybind other) {
        Objects.requireNonNull(other);
        set(other.key, other.value, other.modifiers);
        return this;
    }

    public void reset() {
        set(true, GLFW.GLFW_KEY_UNKNOWN, 0);
    }

    public boolean canBindTo(boolean key, int value, int modifiers) {
        if (key) {
            if (modifiers != 0 && isModifierKey(value)) return false;
            return value != GLFW.GLFW_KEY_UNKNOWN && value != GLFW.GLFW_KEY_ESCAPE;
        }
        return value != GLFW.GLFW_MOUSE_BUTTON_LEFT && value != GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    public boolean matches(boolean key, int value, int modifiers) {
        if (!isSet() || this.key != key || this.value != value) return false;
        return !hasMods() || this.modifiers == modifiers;
    }

    public boolean isPressed(long window) {
        if (!isSet()) return false;
        if (!key) return GLFW.glfwGetMouseButton(window, value) == GLFW.GLFW_PRESS;
        if (GLFW.glfwGetKey(window, value) != GLFW.GLFW_PRESS) return false;
        return modifierPressed(window, GLFW.GLFW_MOD_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)
            && modifierPressed(window, GLFW.GLFW_MOD_SUPER, GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER)
            && modifierPressed(window, GLFW.GLFW_MOD_ALT, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT)
            && modifierPressed(window, GLFW.GLFW_MOD_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean modifierPressed(long window, int mask, int left, int right) {
        if ((modifiers & mask) == 0) return true;
        return GLFW.glfwGetKey(window, left) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(window, right) == GLFW.GLFW_PRESS;
    }

    private static boolean isModifierKey(int key) {
        return key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT
            || key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL
            || key == GLFW.GLFW_KEY_LEFT_ALT || key == GLFW.GLFW_KEY_RIGHT_ALT
            || key == GLFW.GLFW_KEY_LEFT_SUPER || key == GLFW.GLFW_KEY_RIGHT_SUPER;
    }

    public String toString() {
        if (!isSet()) return "None";
        if (!key) return "Mouse " + (value + 1);
        StringBuilder label = new StringBuilder();
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) label.append("Ctrl + ");
        if ((modifiers & GLFW.GLFW_MOD_SUPER) != 0) label.append("Cmd + ");
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) label.append("Alt + ");
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) label.append("Shift + ");
        return label.append(GLFW.glfwGetKeyName(value, 0) == null ? "Key " + value : GLFW.glfwGetKeyName(value, 0)).toString();
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("isKey", key);
        tag.putInt("value", value);
        tag.putInt("modifiers", modifiers);
        return tag;
    }

    public Keybind fromTag(NbtCompound tag) {
        set(tag.getBoolean("isKey", true), tag.getInt("value", GLFW.GLFW_KEY_UNKNOWN), tag.getInt("modifiers", 0));
        return this;
    }

    @Override public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Keybind other)) return false;
        return key == other.key && value == other.value && modifiers == other.modifiers;
    }

    @Override public int hashCode() {
        return Objects.hash(key, value, modifiers);
    }
}
