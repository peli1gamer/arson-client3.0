package io.arson.client.config;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.IntSetting;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPersistenceTest {
    private static final class IntegerTestModule extends Module {
        final IntSetting count = setting(new IntSetting("count", "Count", 4, 0, 20, 2));
        IntegerTestModule() { super("integer-test", "Integer Test", Category.MISC); }
    }

    private static final class NativeSettingsTestModule extends Module {
        final meteordevelopment.meteorclient.settings.BoolSetting nativeToggle;
        NativeSettingsTestModule() {
            super("native-settings-test", "Native Settings Test", Category.MISC);
            nativeToggle = defaultGroup().add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                    .name("native-toggle").defaultValue(false).build());
        }
    }

    @Test
    void profileNameSanitizationIsDeterministicAndPathSafe() {
        assertEquals("Combat_01test", ConfigManager.sanitizeProfileName("Combat_01/test"));
        assertEquals("", ConfigManager.sanitizeProfileName("../../"));
        assertTrue(ConfigManager.sanitizeProfileName("abcdefghijklmnopqrstuvwxyz0123456789-long").length() <= 32);
    }

    @Test
    void savedModuleStateLoadsAndCanBePersistedAsLiveConfig() throws Exception {
        Path dir = Files.createTempDirectory("arson-config-test");
        Path profile = dir.resolve("profile.json");
        Path live = dir.resolve("live.json");

        ModuleManager source = new ModuleManager();
        source.registerDefaults();
        IntegerTestModule sourceInteger = new IntegerTestModule();
        source.register(sourceInteger);
        sourceInteger.count.set(14);
        Module sprint = source.get("sprint");
        assertNotNull(sprint);
        sprint.setEnabled(true);
        sprint.setKeyCode(65);
        sprint.toggleOnBindRelease = true;
        sprint.chatFeedback = false;
        ConfigManager.saveToPath(profile, source);

        ModuleManager loaded = new ModuleManager();
        loaded.registerDefaults();
        IntegerTestModule loadedInteger = new IntegerTestModule();
        loaded.register(loadedInteger);
        assertFalse(loaded.get("sprint").enabled());
        assertTrue(ConfigManager.loadFromPath(profile, loaded));
        assertTrue(loaded.get("sprint").enabled());
        assertEquals(65, loaded.get("sprint").keyCode());
        assertTrue(loaded.get("sprint").toggleOnBindRelease);
        assertFalse(loaded.get("sprint").chatFeedback);
        assertEquals(14, loadedInteger.count.get());

        ConfigManager.saveToPath(live, loaded);
        ModuleManager restarted = new ModuleManager();
        restarted.registerDefaults();
        IntegerTestModule restartedInteger = new IntegerTestModule();
        restarted.register(restartedInteger);
        assertTrue(ConfigManager.loadFromPath(live, restarted));
        assertTrue(restarted.get("sprint").enabled());
        assertEquals(65, restarted.get("sprint").keyCode());
        assertTrue(restarted.get("sprint").toggleOnBindRelease);
        assertFalse(restarted.get("sprint").chatFeedback);
        assertEquals(14, restartedInteger.count.get());
    }

    @Test
    void legacyHudLayoutWithoutSettingsObjectMigratesSafely() throws Exception {
        Path profile = Files.createTempFile("arson-legacy-hud", ".json");
        Files.writeString(profile, """
                {
                  "version": 3,
                  "hudLayout": {
                    "watermark": { "x": 12.5, "y": 22.5, "anchor": "TOP_LEFT" }
                  },
                  "modules": {
                    "hud-layout": {}
                  }
                }
                """);
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();

        assertTrue(ConfigManager.loadFromPath(profile, manager));
    }

    @Test
    void malformedLaterModuleDoesNotPartiallyApplyEarlierModuleState() throws Exception {
        Path profile = Files.createTempFile("arson-malformed", ".json");
        Files.writeString(profile, """
                {
                  "version": 4,
                  "modules": {
                    "sprint": { "enabled": false },
                    "aim-assist": { "keybind": { "isKey": true, "value": "not-a-key-code", "modifiers": 0 } }
                  }
                }
                """);
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module sprint = manager.get("sprint");
        sprint.setEnabled(true);

        assertFalse(ConfigManager.loadFromPath(profile, manager));
        assertTrue(sprint.enabled());
    }

    @Test
    void invalidProfileLoadLeavesExistingStateUntouched() throws Exception {
        Path profile = Files.createTempFile("arson-invalid", ".json");
        Files.writeString(profile, "not json");
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        manager.get("sprint").setEnabled(true);
        assertFalse(ConfigManager.loadFromPath(profile, manager));
        assertTrue(manager.get("sprint").enabled());
    }
}
