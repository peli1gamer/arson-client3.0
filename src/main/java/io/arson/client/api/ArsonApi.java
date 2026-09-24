package io.arson.client.api;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import io.arson.client.module.HudModule;
import io.arson.client.module.ClickGuiPreferencesModule;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleControl;
import io.arson.client.settings.Setting;
import io.arson.client.settings.SettingValueParser;
import io.arson.client.module.ServerInfoModule;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

/** Stable public facade for addons that need Arson module discovery/control. */
public final class ArsonApi {
    private ArsonApi() {}
    public static String moduleSummary(Module.Category category) {
        if (ArsonClient.getInstance() == null || category == null) return "";
        var modules = ArsonClient.getInstance().modules().organized(category);
        return modules.stream().map(m -> m.id() + (m.enabled() ? "[on]" : "[off]")).collect(java.util.stream.Collectors.joining(", "));
    }
    public static int resetCategory(Module.Category category) {
        if (ArsonClient.getInstance() == null || category == null) return 0;
        int changed = 0;
        for (Module module : ArsonClient.getInstance().modules().all()) {
            if (module.category() == category) { module.resetToDefaults(); changed++; }
        }
        if (changed > 0) save();
        return changed;
    }

    public static boolean setHudElementPosition(String element, double x, double y) {
        if (ArsonClient.getInstance() == null || element == null || element.isBlank()) return false;
        Module module = ArsonClient.getInstance().modules().get("hud");
        if (!(module instanceof io.arson.client.module.HudModule hud)) return false;
        try { hud.setEditorPosition(element, x, y); save(); return true; }
        catch (IllegalArgumentException ignored) { return false; }
    }

    public static int resetAllModules() {
        if (ArsonClient.getInstance() == null) return 0;
        int changed = 0;
        for (Module module : ArsonClient.getInstance().modules().all()) { module.resetToDefaults(); changed++; }
        if (changed > 0) save();
        return changed;
    }

    public static Optional<Module> module(String id) { if (ArsonClient.getInstance() == null || id == null || id.isBlank()) return Optional.empty(); return Optional.ofNullable(ArsonClient.getInstance().modules().get(id)); }
    public static boolean setSetting(String moduleId, String settingId, String value) {
        if (moduleId == null || moduleId.isBlank() || settingId == null || settingId.isBlank()) return false;
        Optional<Module> found = module(moduleId);
        if (found.isEmpty()) return false;
        for (Setting<?> setting : found.get().settings()) {
            if (setting.id().equalsIgnoreCase(settingId.trim())) {
                boolean applied = SettingValueParser.apply(setting, value);
                if (applied) save();
                return applied;
            }
        }
        return false;
    }
    public static List<Module> modules() { return ArsonClient.getInstance() == null ? List.of() : List.copyOf(ArsonClient.getInstance().modules().all()); }
    public static List<Module> modules(Module.Category category) { if (ArsonClient.getInstance() == null || category == null) return List.of(); return List.copyOf(ArsonClient.getInstance().modules().organized(category)); }
    public static List<Module> favorites() { return modules().stream().filter(Module::favorite).toList(); }
    public static String serverSummary() {
        return module("server-info").filter(ServerInfoModule.class::isInstance)
                .map(ServerInfoModule.class::cast).map(ServerInfoModule::formatted)
                .orElse("Server info unavailable");
    }
    public static List<String> profiles() {
        return ArsonClient.getInstance() == null ? List.of() : ConfigManager.listProfiles(Minecraft.getInstance());
    }
    public static boolean duplicateProfile(String source, String target) {
        return ArsonClient.getInstance() != null && ConfigManager.duplicateProfile(Minecraft.getInstance(), source, target);
    }
    public static boolean renameProfile(String source, String target) {
        return ArsonClient.getInstance() != null && ConfigManager.renameProfile(Minecraft.getInstance(), source, target);
    }
    /** Stable case-insensitive discovery surface for addons and integrations. */
    public static List<Module> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return modules().stream().filter(m -> m.id().toLowerCase(Locale.ROOT).contains(needle) || m.name().toLowerCase(Locale.ROOT).contains(needle) || m.description().toLowerCase(Locale.ROOT).contains(needle)).toList();
    }
    public static int setFavorite(Module.Category category, boolean favorite) {
        if (ArsonClient.getInstance() == null || category == null) return 0;
        int changed = 0;
        for (Module module : modules(category)) if (module.favorite() != favorite) { module.setFavorite(favorite); changed++; }
        if (changed > 0) save();
        return changed;
    }
    public static boolean applyHudPreset(String preset) {
        if (ArsonClient.getInstance() == null || preset == null || preset.isBlank()) return false;
        Module module = ArsonClient.getInstance().modules().get("hud");
        if (!(module instanceof HudModule hud)) return false;
        try { hud.applyPreset(preset); save(); return true; }
        catch (IllegalArgumentException ignored) { return false; }
    }
    public static boolean setHudRowFormat(HudModule.RowFormat format) {
        if (ArsonClient.getInstance() == null || format == null) return false;
        Module module = ArsonClient.getInstance().modules().get("hud");
        if (!(module instanceof HudModule hud)) return false;
        for (var setting : hud.settings()) if (setting.id().equals("row-format") && setting instanceof io.arson.client.settings.EnumSetting<?> select) {
            @SuppressWarnings({"rawtypes", "unchecked"}) io.arson.client.settings.EnumSetting raw = select;
            raw.set(format); save(); return true;
        }
        return false;
    }
    public static Optional<HudModule.ElementRowFormat> hudElementRowFormat(String element) {
        if (ArsonClient.getInstance() == null || element == null) return Optional.empty();
        Module module = ArsonClient.getInstance().modules().get("hud");
        if (!(module instanceof HudModule hud)) return Optional.empty();
        return switch (element.trim().toLowerCase(Locale.ROOT)) {
            case "player-info" -> Optional.of(hud.playerInfoRowFormat());
            case "world-info" -> Optional.of(hud.worldInfoRowFormat());
            default -> Optional.empty();
        };
    }
    public static boolean setHudElementRowFormat(String element, HudModule.ElementRowFormat format) {
        if (ArsonClient.getInstance() == null || element == null || format == null) return false;
        Module module = ArsonClient.getInstance().modules().get("hud");
        if (!(module instanceof HudModule hud)) return false;
        try { hud.setElementRowFormat(element.trim().toLowerCase(Locale.ROOT), format); save(); return true; }
        catch (IllegalArgumentException ignored) { return false; }
    }
    public static int categoryCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().categoryCount(category); }
    public static int enabledCount() { return ArsonClient.getInstance() == null ? 0 : ArsonClient.getInstance().modules().enabledCount(); }
    public static int enabledCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().enabledCount(category); }
    public static int setEnabled(Module.Category category, boolean enabled) { if (ArsonClient.getInstance() == null || category == null) return 0; int changed = 0; for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == category && module.enabled() != enabled) { module.setEnabled(enabled); changed++; } if (changed > 0) save(); return changed; }
    public static boolean setEnabled(String id, boolean enabled) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().setEnabled(enabled); save(); return true; }
    public static boolean setFavorite(String id, boolean favorite) {
        if (ArsonClient.getInstance() == null
                || !ModuleControl.setFavorite(ArsonClient.getInstance().modules(), id, favorite)) return false;
        save();
        return true;
    }
    public static boolean toggleFavorite(String id) {
        if (ArsonClient.getInstance() == null
                || !ModuleControl.toggleFavorite(ArsonClient.getInstance().modules(), id)) return false;
        save();
        return true;
    }
    public static boolean resetSettings(String id) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().resetSettings(); save(); return true; }
    public static boolean setKeyCode(String id, int keyCode) {
        if (ArsonClient.getInstance() == null
                || !ModuleControl.setKeyCode(ArsonClient.getInstance().modules(), id, keyCode)) return false;
        save();
        return true;
    }
    public static boolean setClickGuiTheme(ClickGuiPreferencesModule.Theme theme) {
        if (ArsonClient.getInstance() == null || theme == null) return false;
        Module module = ArsonClient.getInstance().modules().get("clickgui-preferences");
        if (!(module instanceof ClickGuiPreferencesModule prefs)) return false;
        while (prefs.theme() != theme) prefs.cycleTheme();
        save(); return true;
    }
    public static boolean setClickGuiPanelScale(double scale) { if (ArsonClient.getInstance() == null || Double.isNaN(scale) || Double.isInfinite(scale) || scale < 0.75 || scale > 1.25) return false; Module module = ArsonClient.getInstance().modules().get("clickgui-preferences"); if (!(module instanceof ClickGuiPreferencesModule prefs)) return false; prefs.setPanelScale(scale); save(); return true; }
    public static boolean setClickGuiFilters(boolean favoritesOnly, boolean enabledOnly, boolean alphabetical) {
        if (ArsonClient.getInstance() == null) return false;
        Module module = ArsonClient.getInstance().modules().get("clickgui-preferences");
        if (!(module instanceof ClickGuiPreferencesModule prefs)) return false;
        prefs.setFavoritesOnly(favoritesOnly); prefs.setEnabledOnly(enabledOnly); prefs.setAlphabetical(alphabetical);
        save(); return true;
    }
    /** Read-only module status for addons and command integrations. */
    public static Optional<String> moduleStatus(String id) {
        if (ArsonClient.getInstance() == null) return Optional.empty();
        return ModuleControl.status(ArsonClient.getInstance().modules(), id);
    }

    /** Typed module state snapshot for addon integrations. */
    public static Optional<ModuleControl.Snapshot> moduleState(String id) {
        if (ArsonClient.getInstance() == null) return Optional.empty();
        return ModuleControl.snapshot(ArsonClient.getInstance().modules(), id);
    }

    public static void save() { if (ArsonClient.getInstance() != null) ArsonClient.getInstance().saveConfig(); }
}
