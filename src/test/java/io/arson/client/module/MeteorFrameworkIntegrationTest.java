package io.arson.client.module;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.systems.Systems;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeteorFrameworkIntegrationTest {
    private enum Mode { FIRST, SECOND }

    @Test
    void typedMeteorSettingBuildersValidateValues() {
        BoolSetting enabled = new BoolSetting.Builder()
            .name("enabled")
            .description("Test toggle")
            .defaultValue(true)
            .build();
        assertTrue(enabled.get());

        DoubleSetting amount = new DoubleSetting.Builder()
            .name("amount")
            .defaultValue(0.5)
            .range(0.0, 1.0)
            .build();
        assertTrue(amount.set(0.75));
        assertFalse(amount.set(2.0));
        assertEquals(0.75, amount.get());

        EnumSetting<Mode> mode = new EnumSetting.Builder<Mode>()
            .name("mode")
            .defaultValue(Mode.FIRST)
            .build();
        assertTrue(mode.parse("SECOND"));
        assertEquals(Mode.SECOND, mode.get());
    }

    @Test
    void arsonModulesUseMeteorModuleSettingsAndCategoryRegistry() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        Module aimAssist = manager.get("aim-assist");
        assertNotNull(aimAssist);
        assertInstanceOf(meteordevelopment.meteorclient.systems.modules.Module.class, aimAssist);
        assertSame(aimAssist.defaultGroup(), aimAssist.settings.getGroup("General"));
        assertNotNull(aimAssist.meteorCategory());
    }

    @Test
    void moduleManagerRegistersThroughMeteorSystemsLifecycle() {
        ModuleManager manager = Systems.add(new ModuleManager());

        assertSame(manager, Systems.get(ModuleManager.class));
        assertNotNull(manager.get("aim-assist"));
        assertTrue(manager.getCount() > 0);
    }
}
