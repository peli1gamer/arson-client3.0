package io.arson.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Small, defensive JSON config layer. A broken config never prevents the client from starting. */
public final class ConfigManager {
    private static final String FILE_NAME = "arson-v3.json";
    private static final String TEMP_FILE_NAME = "arson-v3.json.tmp";

    private ConfigManager() {}

    public static void load(Minecraft client, ModuleManager modules) {
        Path path = client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
        if (!Files.isRegularFile(path)) return;

        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();
            JsonObject moduleRoot = root.has("modules") && root.get("modules").isJsonObject()
                    ? root.getAsJsonObject("modules") : new JsonObject();

            for (Module module : modules.all()) {
                if (!moduleRoot.has(module.id()) || !moduleRoot.get(module.id()).isJsonObject()) continue;
                JsonObject data = moduleRoot.getAsJsonObject(module.id());
                if (data.has("enabled") && data.get("enabled").isJsonPrimitive()) {
                    module.setEnabled(data.get("enabled").getAsBoolean());
                }
                if (data.has("keyCode") && data.get("keyCode").isJsonPrimitive()) {
                    module.setKeyCode(data.get("keyCode").getAsInt());
                }
                JsonObject settings = data.has("settings") && data.get("settings").isJsonObject()
                        ? data.getAsJsonObject("settings") : new JsonObject();
                for (Setting<?> setting : module.settings()) {
                    JsonElement value = settings.get(setting.id());
                    if (value == null || !value.isJsonPrimitive()) continue;
                    try {
                        if (setting instanceof BooleanSetting bool) bool.set(value.getAsBoolean());
                        else if (setting instanceof DoubleSetting number) number.set(value.getAsDouble());
                        else if (setting instanceof ColorSetting color) color.set(value.getAsInt());
                        else if (setting instanceof StringSetting text) text.set(value.getAsString());
                    } catch (RuntimeException ignored) {
                        // Keep the declared default when an individual value is invalid.
                    }
                }
            }
        } catch (Exception ignored) {
            // Config is optional; never make startup dependent on it.
        }
    }

    public static void save(Minecraft client, ModuleManager modules) {
        Path directory = client.gameDirectory.toPath().resolve("config");
        Path path = directory.resolve(FILE_NAME);
        Path tempPath = directory.resolve(TEMP_FILE_NAME);
        JsonObject root = new JsonObject();
        JsonObject moduleRoot = new JsonObject();
        root.add("modules", moduleRoot);

        for (Module module : modules.all()) {
            JsonObject data = new JsonObject();
            data.addProperty("enabled", module.enabled());
            data.addProperty("keyCode", module.keyCode());
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                Object value = setting.get();
                if (value instanceof Boolean bool) settings.addProperty(setting.id(), bool);
                else if (value instanceof Number number) settings.addProperty(setting.id(), number);
                else if (value instanceof String text) settings.addProperty(setting.id(), text);
            }
            data.add("settings", settings);
            moduleRoot.add(module.id(), data);
        }

        try {
            Files.createDirectories(directory);
            Files.writeString(tempPath, root.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnsupported) {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignoredCleanup) {
                // Best-effort cleanup only.
            }
        }
    }
}
