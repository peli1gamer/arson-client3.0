/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NbtCompound;

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
        for (SettingGroup group : groups) tag.put(group.name, group.toTag());
        return tag;
    }

    public void fromTag(NbtCompound tag) {
        for (SettingGroup group : groups) {
            if (tag.contains(group.name)) group.fromTag(tag.getCompoundOrEmpty(group.name));
        }
    }

    @Override
    public Iterator<SettingGroup> iterator() {
        return groups.iterator();
    }
}
