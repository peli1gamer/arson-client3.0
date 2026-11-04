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
        EVENT_BUS.registerLambdaFactory("io.arson.client", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.registerLambdaFactory("meteordevelopment.meteorclient", (lookupInMethod, klass) ->
            (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }
}
