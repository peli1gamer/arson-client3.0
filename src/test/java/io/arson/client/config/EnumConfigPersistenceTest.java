package io.arson.client.config;

import io.arson.client.module.ModuleManager;
import io.arson.client.settings.EnumSetting;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class EnumConfigPersistenceTest {
    @Test
    void enumSettingRoundTripsThroughConfig() throws Exception {
        ModuleManager first = new ModuleManager();
        first.registerDefaults();
        @SuppressWarnings("unchecked") EnumSetting<ModuleManager.RenderMode> setting = (EnumSetting<ModuleManager.RenderMode>) first.get("render-profile").settings().get(0);
        setting.set(ModuleManager.RenderMode.HIGH_CONTRAST);
        Path file = Files.createTempFile("arson-enum", ".json");
        assertTrue(ConfigManager.saveToPath(file, first));

        ModuleManager second = new ModuleManager();
        second.registerDefaults();
        @SuppressWarnings("unchecked") EnumSetting<ModuleManager.RenderMode> loaded = (EnumSetting<ModuleManager.RenderMode>) second.get("render-profile").settings().get(0);
        assertEquals(ModuleManager.RenderMode.STANDARD, loaded.get());
        assertTrue(ConfigManager.loadFromPath(file, second));
        assertEquals(ModuleManager.RenderMode.HIGH_CONTRAST, loaded.get());
        Files.deleteIfExists(file);
    }
}
