/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }
}
