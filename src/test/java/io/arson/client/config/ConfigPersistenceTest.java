package io.arson.client.config;

import io.arson.client.module.ModuleManager;
import io.arson.client.module.Module;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPersistenceTest {
    @Test
    void profileNameSanitizationIsDeterministicAndPathSafe() {
        assertEquals("Combat_01-test", ConfigManager.sanitizeProfileName("Combat_01/test"));
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
        Module sprint = source.get("sprint");
        assertNotNull(sprint);
        sprint.setEnabled(true);
        sprint.setKeyCode(65);
        ConfigManager.saveToPath(profile, source);

        ModuleManager loaded = new ModuleManager();
        loaded.registerDefaults();
        assertFalse(loaded.get("sprint").enabled());
        assertTrue(ConfigManager.loadFromPath(profile, loaded));
        assertTrue(loaded.get("sprint").enabled());
        assertEquals(65, loaded.get("sprint").keyCode());

        ConfigManager.saveToPath(live, loaded);
        ModuleManager restarted = new ModuleManager();
        restarted.registerDefaults();
        assertTrue(ConfigManager.loadFromPath(live, restarted));
        assertTrue(restarted.get("sprint").enabled());
        assertEquals(65, restarted.get("sprint").keyCode());
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
