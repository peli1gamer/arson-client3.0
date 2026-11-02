package io.arson.client.module;

import net.minecraft.client.Minecraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleManagerFailureIsolationTest {
    @Test
    void failingModuleDoesNotPreventLaterModulesFromTicking() {
        ModuleManager manager = new ModuleManager();
        int[] healthyTicks = {0};

        Module broken = new Module("broken", "Broken", Module.Category.MISC) {
            @Override protected void onTick(Minecraft client) {
                throw new IllegalStateException("intentional test failure");
            }
        };
        Module healthy = new Module("healthy", "Healthy", Module.Category.MISC) {
            @Override protected void onTick(Minecraft client) {
                healthyTicks[0]++;
            }
        };
        manager.register(broken);
        manager.register(healthy);
        broken.setEnabled(true);
        healthy.setEnabled(true);

        manager.tick(null);

        assertEquals(1, healthyTicks[0]);
    }

    @Test
    void moduleRegistrationDuringTickDoesNotMutateTheActiveIteration() {
        ModuleManager manager = new ModuleManager();
        int[] lateTicks = {0};
        Module late = new Module("late", "Late", Module.Category.MISC) {
            @Override protected void onTick(Minecraft client) { lateTicks[0]++; }
        };
        Module registering = new Module("registering", "Registering", Module.Category.MISC) {
            private boolean registered;
            @Override protected void onTick(Minecraft client) {
                if (!registered) {
                    registered = true;
                    late.setEnabled(true);
                    manager.register(late);
                }
            }
        };
        registering.setEnabled(true);
        manager.register(registering);

        manager.tick(null);
        assertEquals(0, lateTicks[0]);

        manager.tick(null);
        assertEquals(1, lateTicks[0]);
    }
}
