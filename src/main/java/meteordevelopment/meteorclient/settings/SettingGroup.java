/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class SettingGroup implements Iterable<Setting<?>> {
    public final String name;
    public boolean sectionExpanded;
    protected final List<Setting<?>> settings = new ArrayList<>(1);
    private final boolean defaultSectionExpanded;

    public SettingGroup(String name, boolean sectionExpanded) {
        this.name = Objects.requireNonNull(name);
        this.sectionExpanded = sectionExpanded;
        this.defaultSectionExpanded = sectionExpanded;
    }

    public SettingGroup(String name) { this(name, true); }

    /** Stable identity used by persistence adapters; defaults to the group name. */
    public String getId() { return name; }

    public <T extends Setting<?>> T add(T setting) {
        Objects.requireNonNull(setting, "setting");
        for (Setting<?> existing : settings) {
            if (existing == setting) return setting;
            if (existing.getId().equals(setting.getId())) {
                throw new IllegalArgumentException("Duplicate setting ID in group " + getId() + ": " + setting.getId());
            }
        }
        settings.add(setting);
        return setting;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : this) {
            if (setting.name.equals(name) || setting.getId().equals(name)) return setting;
        }
        return null;
    }

    public boolean wasChanged() {
        if (sectionExpanded != defaultSectionExpanded) return true;
        for (Setting<?> setting : settings) if (setting.wasChanged()) return true;
        return false;
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", name);
        tag.putString("id", getId());
        tag.putBoolean("sectionExpanded", sectionExpanded);
        NbtList list = new NbtList();
        for (Setting<?> setting : settings) {
            if (setting.wasChanged()) list.add(setting.toTag());
        }
        if (!list.isEmpty()) tag.put("settings", list);
        return tag;
    }

    public SettingGroup fromTag(NbtCompound tag) {
        sectionExpanded = tag.getBoolean("sectionExpanded", defaultSectionExpanded);
        NbtList list = tag.getListOrEmpty("settings");
        for (NbtElement element : list) {
            if (!(element instanceof NbtCompound settingTag)) continue;
            String id = settingTag.getString("id", "");
            String settingName = settingTag.getString("name", "");
            Setting<?> setting = get(id.isEmpty() ? settingName : id);
            if (setting == null) setting = get(settingName);
            if (setting != null) setting.fromTag(settingTag);
        }
        return this;
    }

    @Override public Iterator<Setting<?>> iterator() { return settings.iterator(); }
}
