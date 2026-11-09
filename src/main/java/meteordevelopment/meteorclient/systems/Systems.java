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
import java.util.Objects;

public final class Systems {
    private static final Map<Class<?>, System<?>> systems = new LinkedHashMap<>();
    private static final List<Runnable> preLoadTasks = new ArrayList<>();

    private Systems() {
    }

    public static synchronized void addPreLoadTask(Runnable task) {
        preLoadTasks.add(Objects.requireNonNull(task, "task"));
    }

    public static synchronized <T extends System<?>> T add(T system) {
        Objects.requireNonNull(system, "system");
        Class<?> type = system.getClass();
        if (systems.containsKey(type)) {
            throw new IllegalArgumentException("System already registered: " + type.getName());
        }

        // Publish before init so systems can resolve each other during bootstrap.
        systems.put(type, system);
        boolean subscribed = false;
        try {
            MeteorClient.EVENT_BUS.subscribe(system);
            subscribed = true;
            system.init();
            return system;
        } catch (RuntimeException | Error exception) {
            systems.remove(type, system);
            if (subscribed) {
                try {
                    MeteorClient.EVENT_BUS.unsubscribe(system);
                } catch (RuntimeException | Error cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw exception;
        }
    }

    public static synchronized void save(File folder) {
        for (System<?> system : List.copyOf(systems.values())) system.save(folder);
    }

    public static void save() {
        save(null);
    }

    public static synchronized void load(File folder) {
        for (Runnable task : List.copyOf(preLoadTasks)) task.run();
        for (System<?> system : List.copyOf(systems.values())) system.load(folder);
    }

    public static void load() {
        load(null);
    }

    public static synchronized <T extends System<?>> T get(Class<T> type) {
        System<?> exact = systems.get(type);
        if (exact != null) return type.cast(exact);
        for (System<?> system : systems.values()) {
            if (type.isInstance(system)) return type.cast(system);
        }
        return null;
    }

    public static synchronized List<System<?>> all() {
        return List.copyOf(systems.values());
    }
}
