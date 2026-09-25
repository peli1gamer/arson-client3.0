package io.arson.client.config;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigPersistenceTest {
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
    void combatAbsorptionBarColorSurvivesConfigReload() throws Exception {
        Path config = Files.createTempFile("arson-combat-style", ".json");
        ModuleManager source = new ModuleManager();
        source.registerDefaults();
        var sourceCombat = source.get("combat-info");
        var sourceColor = (io.arson.client.settings.ColorSetting) sourceCombat.settings().stream()
                .filter(setting -> setting.id().equals("absorption-color")).findFirst().orElseThrow();
        sourceColor.set(0xFFCCAA22);
        var sourceEquipmentSetting = (io.arson.client.settings.BooleanSetting) sourceCombat.settings().stream()
                .filter(setting -> setting.id().equals("show-target-equipment")).findFirst().orElseThrow();
        sourceEquipmentSetting.set(false);
        ConfigManager.saveToPath(config, source);

        ModuleManager loaded = new ModuleManager();
        loaded.registerDefaults();
        assertTrue(ConfigManager.loadFromPath(config, loaded));
        var loadedColor = (io.arson.client.settings.ColorSetting) loaded.get("combat-info").settings().stream()
                .filter(setting -> setting.id().equals("absorption-color")).findFirst().orElseThrow();
        assertEquals(0xFFCCAA22, loadedColor.get());
        var loadedEquipmentSetting = (io.arson.client.settings.BooleanSetting) loaded.get("combat-info").settings().stream()
                .filter(setting -> setting.id().equals("show-target-equipment")).findFirst().orElseThrow();
        assertFalse(loadedEquipmentSetting.enabled());
    }

    @Test
    void profileCopyAndRenamePreserveFilesAndNeverOverwrite() throws Exception {
        Path dir = Files.createTempDirectory("arson-profile-ops");
        Path source = dir.resolve("source.json");
        Path copy = dir.resolve("copy.json");
        Path occupied = dir.resolve("occupied.json");
        Path renamed = dir.resolve("renamed.json");
        Files.writeString(source, "{\"profile\":\"source\"}");
        Files.writeString(occupied, "keep-existing");

        assertTrue(ConfigManager.copyProfileFile(source, copy));
        assertEquals(Files.readString(source), Files.readString(copy));
        assertFalse(ConfigManager.copyProfileFile(source, occupied));
        assertEquals("keep-existing", Files.readString(occupied));
        assertTrue(Files.exists(source));

        assertTrue(ConfigManager.moveProfileFile(source, renamed));
        assertFalse(Files.exists(source));
        assertEquals("{\"profile\":\"source\"}", Files.readString(renamed));
        assertFalse(ConfigManager.moveProfileFile(renamed, occupied));
        assertEquals("keep-existing", Files.readString(occupied));
        assertTrue(Files.exists(renamed));
        assertFalse(ConfigManager.moveProfileFile(renamed, renamed));
    }

    @Test
    void profileFileOperationsRejectMissingSources() throws Exception {
        Path dir = Files.createTempDirectory("arson-profile-missing");
        Path missing = dir.resolve("missing.json");
        assertFalse(ConfigManager.copyProfileFile(missing, dir.resolve("copy.json")));
        assertFalse(ConfigManager.moveProfileFile(missing, dir.resolve("renamed.json")));
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
