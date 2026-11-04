/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson integration bootstrap for Meteor's Orbit event bus.
 */
package meteordevelopment.meteorclient;

import io.arson.client.module.Module;
import meteordevelopment.orbit.EventBus;
import net.minecraft.client.MinecraftClient;

import java.lang.invoke.MethodHandles;

public final class MeteorClient {
    public static final EventBus EVENT_BUS = new EventBus();
    public static MinecraftClient mc;

    private MeteorClient() {
    }

    public static void initialize(MinecraftClient client) {
        mc = client;
        try {
            EVENT_BUS.registerLookup("io.arson.client.module",
                MethodHandles.privateLookupIn(Module.class, MethodHandles.lookup()));
            EVENT_BUS.registerLookup("meteordevelopment.meteorclient", MethodHandles.lookup());
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to initialize Meteor Orbit event dispatch", exception);
        }
    }
}
