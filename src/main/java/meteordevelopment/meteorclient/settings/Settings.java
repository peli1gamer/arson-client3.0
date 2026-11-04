/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class Settings implements Iterable<SettingGroup> {
    private SettingGroup defaultGroup;
    public final List<SettingGroup> groups = new ArrayList<>(1);

    public void onActivated() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) setting.onActivated();
        }
    }

    public Setting<?> get(String name) {
        for (SettingGroup group : this) {
            for (Setting<?> setting : group) {
                if (name.equalsIgnoreCase(setting.name)) return setting;
            }
        }
        return null;
    }

    public void reset() {
        for (SettingGroup group : groups) {
            for (Setting<?> setting : group) setting.reset();
        }
    }

    public SettingGroup getGroup(String name) {
        for (SettingGroup group : this) {
            if (group.name.equals(name)) return group;
        }
        return null;
    }

    public SettingGroup getDefaultGroup() {
        if (defaultGroup == null) defaultGroup = createGroup("General");
        return defaultGroup;
    }

    public SettingGroup createGroup(String name, boolean expanded) {
        SettingGroup group = new SettingGroup(name, expanded);
        addGroup(group);
        return group;
    }

    public SettingGroup createGroup(String name) {
        return createGroup(name, true);
    }

    public void addGroup(SettingGroup group) {
        if (!groups.contains(group)) groups.add(group);
    }

    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList list = new NbtList();
        for (SettingGroup group : groups) {
            if (group.wasChanged()) list.add(group.toTag());
        }
        if (!list.isEmpty()) tag.put("groups", list);
        return tag;
    }

    public Settings fromTag(NbtCompound tag) {
        reset();
        NbtList list = tag.getListOrEmpty("groups");
        for (NbtElement element : list) {
            if (!(element instanceof NbtCompound groupTag)) continue;
            SettingGroup group = getGroup(groupTag.getString("name", ""));
            if (group != null) group.fromTag(groupTag);
        }
        return this;
    }

    @Override
    public Iterator<SettingGroup> iterator() {
        return groups.iterator();
    }
}
