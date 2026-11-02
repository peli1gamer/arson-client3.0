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
}
