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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import meteordevelopment.meteorclient.systems.Systems;

public class Modules extends meteordevelopment.meteorclient.systems.System<Modules> {
    protected final Map<String, Module> modules = new LinkedHashMap<>();
    private final Map<Category, List<Module>> groups = new LinkedHashMap<>();
    private final List<Module> active = new ArrayList<>();

    public Modules() {
        super("modules");
    }

    public static Modules get() {
        return Systems.get(Modules.class);
    }

    void addActive(Module module) {
        synchronized (active) {
            if (!active.contains(module)) active.add(module);
        }
    }

    void removeActive(Module module) {
        synchronized (active) {
            active.remove(module);
        }
    }

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
        synchronized (active) {
            return new ArrayList<>(active);
        }
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

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList list = new NbtList();
        for (Module module : getAll()) {
            NbtCompound moduleTag = module.toTag();
            if (moduleTag != null) list.add(moduleTag);
        }
        tag.put("modules", list);
        return tag;
    }

    @Override
    public Modules fromTag(NbtCompound tag) {
        disableAll();
        NbtList list = tag.getListOrEmpty("modules");
        for (NbtElement element : list) {
            if (!(element instanceof NbtCompound moduleTag)) continue;
            Module module = get(moduleTag.getString("name", ""));
            if (module != null) module.fromTag(moduleTag);
        }
        return this;
    }
}
