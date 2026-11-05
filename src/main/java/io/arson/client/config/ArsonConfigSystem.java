package io.arson.client.config;

import io.arson.client.module.ModuleManager;
import meteordevelopment.meteorclient.systems.System;
import net.minecraft.client.MinecraftClient;

import java.io.File;

/** Meteor Systems adapter for Arson's versioned JSON config and profile storage. */
public final class ArsonConfigSystem extends System<ArsonConfigSystem> {
    private final MinecraftClient client;
    private final ModuleManager modules;

    public ArsonConfigSystem(MinecraftClient client, ModuleManager modules) {
        super("arson-config");
        this.client = client;
        this.modules = modules;
    }

    @Override
    public void init() {
        ConfigManager.load(client, modules);
    }

    @Override
    public void load(File folder) {
        if (folder == null) ConfigManager.load(client, modules);
        else ConfigManager.loadFromPath(ConfigManager.resolveConfigPath(client, folder), modules);
    }

    @Override
    public void save(File folder) {
        if (folder == null) ConfigManager.save(client, modules);
        else ConfigManager.saveToPath(ConfigManager.resolveConfigPath(client, folder), modules);
    }
}
