package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
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
        register(new SprintModule());
        register(new PerformanceModule());
        register(new ContainerESPModule());
        register(new EntityESPModule());
    }

    public Module get(String id) { return modules.get(id); }
    public Collection<Module> all() { return new ArrayList<>(modules.values()); }

    public void tick(Minecraft client) {
        for (Module module : modules.values()) module.tick(client);
    }

    private static final class ClientInfoModule extends Module {
        private ClientInfoModule() {
            super("client-info", "Client Info", Category.MISC);
            setting(new BooleanSetting("show-version", "Show Version", true));
        }
    }

    private static final class SprintModule extends Module {
        private final BooleanSetting forwardOnly = setting(
                new BooleanSetting("forward-only", "Forward Only", true));

        private SprintModule() {
            super("sprint", "Sprint", Category.MOVEMENT);
        }

        @Override
        protected void onTick(Minecraft client) {
            if (client.player == null) return;
            if (forwardOnly.enabled() && !client.options.keyUp.isDown()) return;
            client.player.setSprinting(true);
        }
    }

    private static final class PerformanceModule extends Module {
        private final DoubleSetting tickBudget = setting(
                new DoubleSetting("tick-budget", "Logic Budget", 1.0, 0.1, 5.0, 0.1));

        private PerformanceModule() {
            super("performance", "Performance", Category.MISC);
        }

        public double tickBudget() { return tickBudget.get(); }
    }
}
