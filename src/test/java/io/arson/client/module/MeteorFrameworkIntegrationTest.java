package io.arson.client.module;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import io.arson.client.settings.BooleanSetting;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import org.lwjgl.glfw.GLFW;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.orbit.EventHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeteorFrameworkIntegrationTest {
    private enum Mode { FIRST, SECOND }

    private static final class Pulse { }

    private static final class EventModule extends Module {
        private final AtomicInteger events = new AtomicInteger();
        private final BooleanSetting flag = setting(new BooleanSetting("flag", "Flag", true));

        private EventModule() {
            this("event-test");
        }

        private EventModule(String id) {
            super(id, "Event Test", Category.MISC);
        }

        @EventHandler
        private void onPulse(Pulse event) {
            events.incrementAndGet();
        }
    }

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

        IntSetting count = new IntSetting.Builder()
            .name("count")
            .defaultValue(5)
            .range(0, 10)
            .build();
        assertTrue(count.set(8));
        assertFalse(count.set(11));
        assertEquals(8, count.get());

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
    void moduleActivationSubscribesAndDeactivationUnsubscribesOrbitHandlers() {
        MeteorClient.initialize(null);
        EventModule module = new EventModule();
        module.enable();

        MeteorClient.EVENT_BUS.post(new Pulse());
        assertEquals(1, module.events.get());

        module.disable();
        MeteorClient.EVENT_BUS.post(new Pulse());
        assertEquals(1, module.events.get());
    }

    @Test
    void moduleAndSettingsRoundTripThroughMeteorNbtFormat() {
        EventModule source = new EventModule();
        source.flag.set(false);
        source.favorite = true;
        source.keybind.set(Keybind.fromKeys(GLFW.GLFW_KEY_R, GLFW.GLFW_MOD_CONTROL));
        source.enable();

        var tag = source.toTag();
        EventModule restored = new EventModule("event-test-restored");
        restored.fromTag(tag);

        assertFalse(restored.flag.get());
        assertTrue(restored.favorite);
        assertEquals(source.keybind, restored.keybind);
        assertTrue(restored.isActive());
    }

    @Test
    void moduleActivationMaintainsMeteorActiveRegistry() {
        MeteorClient.initialize(null);
        ModuleManager manager = Systems.add(new ModuleManager());
        EventModule module = new EventModule();
        manager.register(module);

        module.enable();
        assertTrue(manager.getActive().contains(module));

        module.disable();
        assertFalse(manager.getActive().contains(module));
    }

    @Test
    void moduleManagerRegistersThroughMeteorSystemsLifecycle() {
        ModuleManager manager = Systems.add(new ModuleManager());

        assertSame(manager, Systems.get(ModuleManager.class));
        assertNotNull(manager.get("aim-assist"));
        assertTrue(manager.getCount() > 0);
    }
}
