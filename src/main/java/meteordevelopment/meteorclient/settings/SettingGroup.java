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

    public SettingGroup(String name, boolean sectionExpanded) {
        this.name = Objects.requireNonNull(name);
        this.sectionExpanded = sectionExpanded;
    }

    public SettingGroup(String name) {
        this(name, true);
    }

    public <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : this) {
            if (setting.name.equals(name)) return setting;
        }
        return null;
    }

    public boolean wasChanged() {
        for (Setting<?> setting : settings) if (setting.wasChanged()) return true;
        return false;
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", name);
        tag.putBoolean("sectionExpanded", sectionExpanded);
        NbtList list = new NbtList();
        for (Setting<?> setting : settings) {
            if (setting.wasChanged()) list.add(setting.toTag());
        }
        if (!list.isEmpty()) tag.put("settings", list);
        return tag;
    }

    public SettingGroup fromTag(NbtCompound tag) {
        sectionExpanded = tag.getBoolean("sectionExpanded", false);
        NbtList list = tag.getListOrEmpty("settings");
        for (NbtElement element : list) {
            if (!(element instanceof NbtCompound settingTag)) continue;
            Setting<?> setting = get(settingTag.getString("name", ""));
            if (setting != null) setting.fromTag(settingTag);
        }
        return this;
    }

    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }
}
