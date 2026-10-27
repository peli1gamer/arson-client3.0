package io.arson.client.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final String FILE_NAME = "arson-v3.json";
    private static final String PROFILE_DIRECTORY = "arson-v3-profiles";
    private static final int CONFIG_VERSION = 4;
    private ConfigManager() {}
    public static void load(Minecraft client, ModuleManager modules) {
        if (client == null || client.gameDirectory == null) {
            LOGGER.warn("Cannot load config: Minecraft client or game directory is unavailable");
            return;
        }
        load(client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME), modules);
    }
    public static void save(Minecraft client, ModuleManager modules) {
        if (client == null || client.gameDirectory == null) {
            LOGGER.warn("Cannot save config: Minecraft client or game directory is unavailable");
            return;
        }
        save(client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME), modules);
    }
    public static boolean load(Path path, ModuleManager modules) {
        if (path == null || modules == null) {
            LOGGER.warn("Cannot load config: path or module manager is null");
            return false;
        }
        return loadFromPath(path, modules);
    }
    public static boolean save(Path path, ModuleManager modules) {
        if (path == null || modules == null) {
            LOGGER.warn("Cannot save config: path or module manager is null");
            return false;
        }
        return saveToPath(path, modules);
    }
    public static boolean saveProfile(Minecraft client, ModuleManager modules, String profileName) {
        if (client == null || client.gameDirectory == null || modules == null) return false;
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        return saveToPath(directory.resolve(safeName + ".json"), modules);
    }
    public static boolean loadProfile(Minecraft client, ModuleManager modules, String profileName) {
        if (client == null || client.gameDirectory == null || modules == null) return false;
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path path = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY).resolve(safeName + ".json");
        if (!loadFromPath(path, modules)) return false;
        return saveToPath(client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME), modules);
    }
    public static List<String> listProfiles(Minecraft client) {
        if (client == null || client.gameDirectory == null) {
            LOGGER.warn("Cannot list config profiles: Minecraft client or game directory is unavailable");
            return List.of();
        }
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        if (!Files.isDirectory(directory)) return List.of();
        ArrayList<String> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(path -> path.getFileName().toString().substring(0, path.getFileName().toString().length() - 5))
                .sorted(String.CASE_INSENSITIVE_ORDER).forEach(result::add);
        } catch (IOException exception) { LOGGER.warn("Could not list config profiles in {}: {}", directory, exception.toString()); }
        return List.copyOf(result);
    }
    public static boolean deleteProfile(Minecraft client, String profileName) {
        if (client == null || client.gameDirectory == null) return false;
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path path = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY).resolve(safeName + ".json");
        try { return Files.deleteIfExists(path); } catch (IOException exception) { LOGGER.warn("Could not delete config profile {}: {}", path, exception.toString()); return false; }
    }
    static boolean loadFromPath(Path path, ModuleManager modules) {
        if (!Files.isRegularFile(path)) { LOGGER.debug("Config file does not exist: {}", path); return false; }
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject()) { LOGGER.warn("Config is not a JSON object: {}", path); return loadBackup(path, modules); }
            JsonObject root = parsed.getAsJsonObject();
            int version = readVersion(root);
            if (version > CONFIG_VERSION) { LOGGER.warn("Config {} uses unsupported version {} (supported up to {})", path, version, CONFIG_VERSION); return loadBackup(path, modules); }
            JsonObject moduleRoot = root.has("modules") && root.get("modules").isJsonObject() ? root.getAsJsonObject("modules") : new JsonObject();
            migrateLegacyLayout(root, moduleRoot, modules);
            for (Module module : modules.all()) {
                if (!moduleRoot.has(module.id()) || !moduleRoot.get(module.id()).isJsonObject()) continue;
                JsonObject data = moduleRoot.getAsJsonObject(module.id());
                // Treat each module field independently: one malformed value must not abort the
                // remaining configuration or leave later modules unapplied.
                try {
                    JsonElement enabled = data.get("enabled");
                    if (enabled != null && enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean())
                        module.setEnabled(enabled.getAsBoolean());
                } catch (RuntimeException ignored) {}
                try {
                    JsonElement favorite = data.get("favorite");
                    if (favorite != null && favorite.isJsonPrimitive() && favorite.getAsJsonPrimitive().isBoolean())
                        module.setFavorite(favorite.getAsBoolean());
                } catch (RuntimeException ignored) {}
                try {
                    JsonElement keyCode = data.get("keyCode");
                    if (keyCode != null && keyCode.isJsonPrimitive() && keyCode.getAsJsonPrimitive().isNumber())
                        module.setKeyCode(keyCode.getAsInt());
                } catch (RuntimeException ignored) {}
                JsonObject settings = data.has("settings") && data.get("settings").isJsonObject() ? data.getAsJsonObject("settings") : new JsonObject();
                for (Setting<?> setting : module.settings()) {
                    JsonElement value = settings.get(setting.id()); if (value == null || !value.isJsonPrimitive()) continue;
                    try {
                        var primitive = value.getAsJsonPrimitive();
                        if (setting instanceof BooleanSetting bool && primitive.isBoolean()) bool.set(primitive.getAsBoolean());
                        else if (setting instanceof DoubleSetting number && primitive.isNumber()) number.set(primitive.getAsDouble());
                        else if (setting instanceof ColorSetting color && primitive.isNumber()) color.set(primitive.getAsInt());
                        else if (setting instanceof StringSetting text && primitive.isString()) text.set(primitive.getAsString());
                        else if (setting instanceof EnumSetting<?> select && primitive.isString()) setEnum(select, primitive.getAsString());
                    } catch (RuntimeException ignored) {}
                }
            }
            return true;
        } catch (Exception exception) {
            LOGGER.warn("Failed to load config {}: {}", path, exception.toString());
            return loadBackup(path, modules);
        }
    }

    private static boolean loadBackup(Path path, ModuleManager modules) {
        if (path == null || path.getFileName() == null || path.getFileName().toString().endsWith(".bak")) return false;
        Path backup = path.resolveSibling(path.getFileName() + ".bak");
        if (!Files.isRegularFile(backup)) return false;
        LOGGER.warn("Attempting config backup recovery from {}", backup);
        return loadFromPath(backup, modules);
    }

    /** Migrates pre-v4 HUD layout objects into the persistent hud-layout module settings. */
    private static void migrateLegacyLayout(JsonObject root, JsonObject moduleRoot, ModuleManager modules) {
        Module layout = modules.get("hud-layout");
        if (layout == null) return;
        JsonObject legacy = null;
        for (String key : List.of("hudLayout", "layout", "hud_layout")) {
            JsonElement value = root.get(key);
            if (value != null && value.isJsonObject()) { legacy = value.getAsJsonObject(); break; }
        }
        if (legacy == null) return;
        JsonObject elements = legacy.has("elements") && legacy.get("elements").isJsonObject()
                ? legacy.getAsJsonObject("elements") : legacy;
        for (String element : List.of("watermark", "coordinates", "fps", "player-info", "world-info")) {
            JsonElement raw = elements.get(element);
            if (raw == null || !raw.isJsonObject()) continue;
            JsonObject value = raw.getAsJsonObject();
            JsonObject moduleData = moduleRoot.has("hud-layout") && moduleRoot.get("hud-layout").isJsonObject()
                    ? moduleRoot.getAsJsonObject("hud-layout") : new JsonObject();
            JsonElement existingSettings = moduleData.get("settings");
            JsonObject settings = existingSettings != null && existingSettings.isJsonObject()
                    ? existingSettings.getAsJsonObject() : new JsonObject();
            if (settings.has(element + "-x") || settings.has(element + "-y") || settings.has(element + "-anchor")) continue;

            JsonElement x = value.get("x");
            if (x != null && x.isJsonPrimitive() && x.getAsJsonPrimitive().isNumber()) {
                try { settings.addProperty(element + "-x", x.getAsDouble()); } catch (RuntimeException ignored) {}
            }
            JsonElement y = value.get("y");
            if (y != null && y.isJsonPrimitive() && y.getAsJsonPrimitive().isNumber()) {
                try { settings.addProperty(element + "-y", y.getAsDouble()); } catch (RuntimeException ignored) {}
            }
            JsonElement anchor = value.get("anchor");
            if (anchor != null && anchor.isJsonPrimitive() && anchor.getAsJsonPrimitive().isString()) {
                settings.addProperty(element + "-anchor", anchor.getAsString());
            }
            moduleData.add("settings", settings);
            moduleRoot.add("hud-layout", moduleData);
        }
    }
    private static void setEnum(EnumSetting<?> setting, String value) { for (Enum<?> candidate : setting.values()) if (candidate.name().equalsIgnoreCase(value)) { setEnumUnchecked(setting, candidate); return; } }
    @SuppressWarnings({"rawtypes", "unchecked"}) private static void setEnumUnchecked(EnumSetting setting, Enum value) { setting.set(value); }
    private static int readVersion(JsonObject root) { try { JsonElement value = root.get("version"); return value != null && value.isJsonPrimitive() ? value.getAsInt() : 1; } catch (RuntimeException ignored) { return 1; } }
    static boolean saveToPath(Path path, ModuleManager modules) {
        Path directory = path.getParent(); if (directory == null) return false;
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp"), backupPath = path.resolveSibling(path.getFileName() + ".bak");
        JsonObject root = new JsonObject(); root.addProperty("version", CONFIG_VERSION); JsonObject moduleRoot = new JsonObject(); root.add("modules", moduleRoot);
        for (Module module : modules.all()) {
            JsonObject data = new JsonObject(); data.addProperty("enabled", module.enabled()); data.addProperty("favorite", module.favorite()); data.addProperty("keyCode", module.keyCode()); JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.settings()) { Object value = setting.get(); if (value instanceof Boolean bool) settings.addProperty(setting.id(), bool); else if (value instanceof Number number) settings.addProperty(setting.id(), number); else if (value instanceof String text) settings.addProperty(setting.id(), text); else if (value instanceof Enum<?> select) settings.addProperty(setting.id(), select.name()); }
            data.add("settings", settings); moduleRoot.add(module.id(), data);
        }
        try {
            Files.createDirectories(directory); Files.writeString(tempPath, root.toString(), StandardCharsets.UTF_8);
            if (Files.isRegularFile(path)) try { Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING); } catch (IOException exception) { LOGGER.warn("Could not create config backup {}: {}", backupPath, exception.toString()); }
            try { Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); } catch (IOException ignored) { Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING); }
            return true;
        } catch (IOException exception) { LOGGER.warn("Failed to save config {}: {}", path, exception.toString()); try { Files.deleteIfExists(tempPath); } catch (IOException cleanupException) { LOGGER.debug("Could not remove temporary config {}: {}", tempPath, cleanupException.toString()); } return false; }
    }
    static String sanitizeProfileName(String name) { if (name == null) return ""; StringBuilder result = new StringBuilder(); for (int i = 0; i < name.length() && result.length() < 32; i++) { char c = name.charAt(i); if (Character.isLetterOrDigit(c) || c == '-' || c == '_') result.append(c); } return result.toString(); }
}
