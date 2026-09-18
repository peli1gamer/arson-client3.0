package io.arson.client.api;

import io.arson.client.ArsonClient;
import io.arson.client.module.HudModule;
import io.arson.client.module.ClickGuiPreferencesModule;
import io.arson.client.module.Module;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

/** Stable public facade for addons that need Arson module discovery/control. */
public final class ArsonApi {
    private ArsonApi() {}
    public static Optional<Module> module(String id) { if (ArsonClient.getInstance() == null || id == null || id.isBlank()) return Optional.empty(); return Optional.ofNullable(ArsonClient.getInstance().modules().get(id)); }
    public static List<Module> modules() { return ArsonClient.getInstance() == null ? List.of() : List.copyOf(ArsonClient.getInstance().modules().all()); }
    public static List<Module> modules(Module.Category category) { if (ArsonClient.getInstance() == null || category == null) return List.of(); return List.copyOf(ArsonClient.getInstance().modules().organized(category)); }
    public static List<Module> favorites() { return modules().stream().filter(Module::favorite).toList(); }
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
    public static int categoryCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().categoryCount(category); }
    public static int enabledCount() { return ArsonClient.getInstance() == null ? 0 : ArsonClient.getInstance().modules().enabledCount(); }
    public static int enabledCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().enabledCount(category); }
    public static int setEnabled(Module.Category category, boolean enabled) { if (ArsonClient.getInstance() == null || category == null) return 0; int changed = 0; for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == category && module.enabled() != enabled) { module.setEnabled(enabled); changed++; } if (changed > 0) save(); return changed; }
    public static boolean setEnabled(String id, boolean enabled) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().setEnabled(enabled); save(); return true; }
    public static boolean setFavorite(String id, boolean favorite) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().setFavorite(favorite); save(); return true; }
    public static boolean resetSettings(String id) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().resetSettings(); save(); return true; }
    public static boolean setKeyCode(String id, int keyCode) { Optional<Module> module = module(id); if (module.isEmpty() || keyCode < 0) return false; module.get().setKeyCode(keyCode); save(); return true; }
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
    public static void save() { if (ArsonClient.getInstance() != null) ArsonClient.getInstance().saveConfig(); }
}
