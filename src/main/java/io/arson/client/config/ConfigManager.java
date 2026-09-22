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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {
    private static final String FILE_NAME = "arson-v3.json";
    private static final String PROFILE_DIRECTORY = "arson-v3-profiles";
    private static final int CONFIG_VERSION = 4;
    private ConfigManager() {}
    public static void load(Minecraft client, ModuleManager modules) { loadFromPath(client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME), modules); }
    public static void save(Minecraft client, ModuleManager modules) { saveToPath(client.gameDirectory.toPath().resolve("config").resolve(FILE_NAME), modules); }
    public static boolean saveProfile(Minecraft client, ModuleManager modules, String profileName) {
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        return saveToPath(directory.resolve(safeName + ".json"), modules);
    }
    public static boolean loadProfile(Minecraft client, ModuleManager modules, String profileName) {
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path path = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY).resolve(safeName + ".json");
        if (!loadFromPath(path, modules)) return false;
        save(client, modules); return true;
    }
    public static List<String> listProfiles(Minecraft client) {
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        if (!Files.isDirectory(directory)) return List.of();
        ArrayList<String> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(path -> path.getFileName().toString().substring(0, path.getFileName().toString().length() - 5))
                .sorted(String.CASE_INSENSITIVE_ORDER).forEach(result::add);
        } catch (IOException ignored) {}
        return List.copyOf(result);
    }
    public static boolean deleteProfile(Minecraft client, String profileName) {
        String safeName = sanitizeProfileName(profileName); if (safeName.isEmpty()) return false;
        Path path = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY).resolve(safeName + ".json");
        try { return Files.deleteIfExists(path); } catch (IOException ignored) { return false; }
    }

    /** Copies a saved profile without replacing an existing destination. */
    public static boolean duplicateProfile(Minecraft client, String sourceName, String targetName) {
        String source = sanitizeProfileName(sourceName), target = sanitizeProfileName(targetName);
        if (source.isEmpty() || target.isEmpty() || source.equalsIgnoreCase(target)) return false;
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        return copyProfileFile(directory.resolve(source + ".json"), directory.resolve(target + ".json"));
    }

    /** Renames a saved profile without replacing an existing destination. */
    public static boolean renameProfile(Minecraft client, String sourceName, String targetName) {
        String source = sanitizeProfileName(sourceName), target = sanitizeProfileName(targetName);
        if (source.isEmpty() || target.isEmpty() || source.equalsIgnoreCase(target)) return false;
        Path directory = client.gameDirectory.toPath().resolve("config").resolve(PROFILE_DIRECTORY);
        return moveProfileFile(directory.resolve(source + ".json"), directory.resolve(target + ".json"));
    }

    static boolean copyProfileFile(Path source, Path target) {
        if (source == null || target == null || samePath(source, target) || !Files.isRegularFile(source) || Files.exists(target)) return false;
        try { Files.copy(source, target); return true; } catch (IOException ignored) { return false; }
    }

    static boolean moveProfileFile(Path source, Path target) {
        if (source == null || target == null || samePath(source, target) || !Files.isRegularFile(source) || Files.exists(target)) return false;
        try { Files.move(source, target); return true; } catch (IOException ignored) { return false; }
    }

    private static boolean samePath(Path first, Path second) {
        return first.toAbsolutePath().normalize().equals(second.toAbsolutePath().normalize());
    }
    static boolean loadFromPath(Path path, ModuleManager modules) {
        if (!Files.isRegularFile(path)) return false;
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!parsed.isJsonObject()) return false;
            JsonObject root = parsed.getAsJsonObject();
            if (readVersion(root) > CONFIG_VERSION) return false;
            JsonObject moduleRoot = root.has("modules") && root.get("modules").isJsonObject() ? root.getAsJsonObject("modules") : new JsonObject();
            migrateLegacyLayout(root, moduleRoot, modules);
            for (Module module : modules.all()) {
                if (!moduleRoot.has(module.id()) || !moduleRoot.get(module.id()).isJsonObject()) continue;
                JsonObject data = moduleRoot.getAsJsonObject(module.id());
                if (data.has("enabled") && data.get("enabled").isJsonPrimitive()) module.setEnabled(data.get("enabled").getAsBoolean());
                if (data.has("favorite") && data.get("favorite").isJsonPrimitive()) module.setFavorite(data.get("favorite").getAsBoolean());
                if (data.has("keyCode") && data.get("keyCode").isJsonPrimitive()) module.setKeyCode(data.get("keyCode").getAsInt());
                JsonObject settings = data.has("settings") && data.get("settings").isJsonObject() ? data.getAsJsonObject("settings") : new JsonObject();
                for (Setting<?> setting : module.settings()) {
                    JsonElement value = settings.get(setting.id()); if (value == null || !value.isJsonPrimitive()) continue;
                    try {
                        if (setting instanceof BooleanSetting bool) bool.set(value.getAsBoolean());
                        else if (setting instanceof DoubleSetting number) number.set(value.getAsDouble());
                        else if (setting instanceof ColorSetting color) color.set(value.getAsInt());
                        else if (setting instanceof StringSetting text) text.set(value.getAsString());
                        else if (setting instanceof EnumSetting<?> select) setEnum(select, value.getAsString());
                    } catch (RuntimeException ignored) {}
                }
            }
            return true;
        } catch (Exception ignored) { return false; }
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
            JsonObject settings = moduleRoot.has("hud-layout") && moduleRoot.get("hud-layout").isJsonObject()
                    ? moduleRoot.getAsJsonObject("hud-layout").getAsJsonObject("settings") : new JsonObject();
            if (settings.has(element + "-x") || settings.has(element + "-y") || settings.has(element + "-anchor")) continue;
            if (value.has("x") && value.get("x").isJsonPrimitive()) settings.addProperty(element + "-x", value.get("x").getAsDouble());
            if (value.has("y") && value.get("y").isJsonPrimitive()) settings.addProperty(element + "-y", value.get("y").getAsDouble());
            if (value.has("anchor") && value.get("anchor").isJsonPrimitive()) settings.addProperty(element + "-anchor", value.get("anchor").getAsString());
            JsonObject moduleData = moduleRoot.has("hud-layout") && moduleRoot.get("hud-layout").isJsonObject()
                    ? moduleRoot.getAsJsonObject("hud-layout") : new JsonObject();
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
            if (Files.isRegularFile(path)) try { Files.copy(path, backupPath, StandardCopyOption.REPLACE_EXISTING); } catch (IOException ignored) {}
            try { Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); } catch (IOException ignored) { Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING); }
            return true;
        } catch (IOException ignored) { try { Files.deleteIfExists(tempPath); } catch (IOException ignoredCleanup) {} return false; }
    }
    static String sanitizeProfileName(String name) { if (name == null) return ""; StringBuilder result = new StringBuilder(); for (int i = 0; i < name.length() && result.length() < 32; i++) { char c = name.charAt(i); if (Character.isLetterOrDigit(c) || c == '-' || c == '_') result.append(c); } return result.toString(); }
}
