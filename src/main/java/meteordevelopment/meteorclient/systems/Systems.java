/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson initially registers its module system here; additional Meteor systems
 * can be migrated into the same lifecycle incrementally.
 */
package meteordevelopment.meteorclient.systems;

import meteordevelopment.meteorclient.MeteorClient;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Systems {
    private static final Map<Class<?>, System<?>> systems = new LinkedHashMap<>();
    private static final List<Runnable> preLoadTasks = new ArrayList<>();

    private Systems() {
    }

    public static void addPreLoadTask(Runnable task) {
        preLoadTasks.add(task);
    }

    public static <T extends System<?>> T add(T system) {
        systems.put(system.getClass(), system);
        MeteorClient.EVENT_BUS.subscribe(system);
        system.init();
        return system;
    }

    public static void save(File folder) {
        for (System<?> system : systems.values()) system.save(folder);
    }

    public static void save() {
        save(null);
    }

    public static void load(File folder) {
        for (Runnable task : preLoadTasks) task.run();
        for (System<?> system : systems.values()) system.load(folder);
    }

    public static void load() {
        load(null);
    }

    public static <T extends System<?>> T get(Class<T> type) {
        System<?> system = systems.get(type);
        return system == null ? null : type.cast(system);
    }

    public static List<System<?>> all() {
        return List.copyOf(systems.values());
    }
}
