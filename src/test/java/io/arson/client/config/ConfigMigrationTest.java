package io.arson.client.config;

import com.google.gson.JsonObject;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.ModuleManager;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ConfigMigrationTest {
    @Test
    void legacyHudLayoutObjectMigratesIntoAnchorSettings() throws Exception {
        ModuleManager modules = new ModuleManager();
        modules.registerDefaults();

        JsonObject root = new JsonObject();
        JsonObject legacy = new JsonObject();
        JsonObject watermark = new JsonObject();
        watermark.addProperty("x", 42);
        watermark.addProperty("y", 18);
        watermark.addProperty("anchor", "TOP_RIGHT");
        legacy.add("watermark", watermark);
        root.add("hudLayout", legacy);

        var path = Files.createTempFile("arson-v3-legacy-", ".json");
        try {
            Files.writeString(path, root.toString());
            assertTrue(ConfigManager.loadFromPath(path, modules));
            var layout = (HudLayoutModule) modules.get("hud-layout");
            assertEquals(HudLayoutModule.Anchor.TOP_RIGHT, layout.anchor("watermark"));
            assertEquals(42.0, layout.offsetX("watermark"));
            assertEquals(18.0, layout.offsetY("watermark"));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void malformedExistingHudSettingsDoNotAbortLegacyMigration() throws Exception {
        ModuleManager modules = new ModuleManager();
        modules.registerDefaults();

        JsonObject root = new JsonObject();
        JsonObject moduleRoot = new JsonObject();
        JsonObject layoutData = new JsonObject();
        layoutData.addProperty("settings", "not-an-object");
        moduleRoot.add("hud-layout", layoutData);
        root.add("modules", moduleRoot);

        JsonObject legacy = new JsonObject();
        JsonObject watermark = new JsonObject();
        watermark.addProperty("x", 42);
        watermark.addProperty("y", 18);
        watermark.addProperty("anchor", "TOP_RIGHT");
        legacy.add("watermark", watermark);
        root.add("hudLayout", legacy);

        var path = Files.createTempFile("arson-v3-malformed-layout-", ".json");
        try {
            Files.writeString(path, root.toString());
            assertTrue(ConfigManager.loadFromPath(path, modules));
            var layout = (HudLayoutModule) modules.get("hud-layout");
            assertEquals(HudLayoutModule.Anchor.TOP_RIGHT, layout.anchor("watermark"));
            assertEquals(42.0, layout.offsetX("watermark"));
            assertEquals(18.0, layout.offsetY("watermark"));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void partialHudSettingsPreserveExplicitValuesAndMigrateMissingFields() throws Exception {
        ModuleManager modules = new ModuleManager();
        modules.registerDefaults();

        JsonObject root = new JsonObject();
        JsonObject moduleRoot = new JsonObject();
        JsonObject layoutData = new JsonObject();
        JsonObject settings = new JsonObject();
        settings.addProperty("watermark-x", 99);
        layoutData.add("settings", settings);
        moduleRoot.add("hud-layout", layoutData);
        root.add("modules", moduleRoot);

        JsonObject legacy = new JsonObject();
        JsonObject watermark = new JsonObject();
        watermark.addProperty("x", 42);
        watermark.addProperty("y", 18);
        watermark.addProperty("anchor", "TOP_RIGHT");
        legacy.add("watermark", watermark);
        root.add("hudLayout", legacy);

        var path = Files.createTempFile("arson-v3-partial-layout-", ".json");
        try {
            Files.writeString(path, root.toString());
            assertTrue(ConfigManager.loadFromPath(path, modules));
            var layout = (HudLayoutModule) modules.get("hud-layout");
            assertEquals(99.0, layout.offsetX("watermark"));
            assertEquals(18.0, layout.offsetY("watermark"));
            assertEquals(HudLayoutModule.Anchor.TOP_RIGHT, layout.anchor("watermark"));
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
