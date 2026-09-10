package io.arson.client.module;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void register(Module module) {
        if (modules.putIfAbsent(module.id(), module) != null) {
            throw new IllegalArgumentException("Duplicate module: " + module.id());
        }
    }

    public void registerDefaults() {
        register(new ClientInfoModule());
    }

    public Module get(String id) {
        return modules.get(id);
    }

    public Collection<Module> all() {
        return new ArrayList<>(modules.values());
    }

    public void tick(Minecraft client) {
        for (Module module : modules.values()) {
            module.tick(client);
        }
    }

    private static final class ClientInfoModule extends Module {
        private ClientInfoModule() {
            super("client-info", "Client Info", Category.MISC);
        }
    }
}
