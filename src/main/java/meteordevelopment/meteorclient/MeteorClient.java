/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson integration bootstrap for Meteor's Orbit event bus.
 */
package meteordevelopment.meteorclient;

import io.arson.client.module.Module;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.minecraft.client.MinecraftClient;

import java.lang.invoke.MethodHandles;
import java.io.File;

public final class MeteorClient {
    public static final String MOD_ID = "arson";
    public static final File FOLDER = new File("config/arson-v3");
    public static final IEventBus EVENT_BUS = new EventBus();
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
