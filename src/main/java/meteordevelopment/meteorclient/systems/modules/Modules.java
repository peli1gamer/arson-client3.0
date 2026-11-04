/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Core registry adapted for Arson's module identifiers and retained module API.
 */
package meteordevelopment.meteorclient.systems.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Modules {
    protected final Map<String, Module> modules = new LinkedHashMap<>();
    private final Map<Category, List<Module>> groups = new LinkedHashMap<>();
    private final List<Module> active = new ArrayList<>();

    protected String key(Module module) {
        return module.name.toLowerCase(java.util.Locale.ROOT);
    }

    public void add(Module module) {
        String key = key(module);
        if (modules.putIfAbsent(key, module) != null) {
            throw new IllegalArgumentException("Duplicate module: " + key);
        }
        groups.computeIfAbsent(module.category, category -> new ArrayList<>()).add(module);
    }

    public void register(Module module) {
        add(module);
    }

    public Module get(String name) {
        if (name == null) return null;
        Module module = modules.get(name);
        if (module != null) return module;
        for (Module candidate : modules.values()) {
            if (candidate.name.equalsIgnoreCase(name)) return candidate;
        }
        return null;
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules.values()) {
            if (type.isInstance(module)) return type.cast(module);
        }
        return null;
    }

    public Collection<Module> getAll() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> getGroup(Category category) {
        return new ArrayList<>(groups.getOrDefault(category, List.of()));
    }

    public List<Module> getActive() {
        active.clear();
        for (Module module : modules.values()) {
            if (module.isActive()) active.add(module);
        }
        return new ArrayList<>(active);
    }

    public int getCount() {
        return modules.size();
    }

    public void sortModules() {
        for (List<Module> group : groups.values()) {
            group.sort(Comparator.comparing(module -> module.title, String.CASE_INSENSITIVE_ORDER));
        }
    }

    public void disableAll() {
        for (Module module : getAll()) module.disable();
    }
}
