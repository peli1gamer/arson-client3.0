package io.arson.client.api;

import io.arson.client.ArsonClient;
import io.arson.client.module.Module;
import java.util.List;
import java.util.Optional;

/** Stable public facade for addons that need Arson module discovery/control. */
public final class ArsonApi {
    private ArsonApi() {}
    public static Optional<Module> module(String id) { if (ArsonClient.getInstance() == null || id == null || id.isBlank()) return Optional.empty(); return Optional.ofNullable(ArsonClient.getInstance().modules().get(id)); }
    public static List<Module> modules() { return ArsonClient.getInstance() == null ? List.of() : List.copyOf(ArsonClient.getInstance().modules().all()); }
    public static List<Module> modules(Module.Category category) { if (ArsonClient.getInstance() == null || category == null) return List.of(); return List.copyOf(ArsonClient.getInstance().modules().organized(category)); }
    public static List<Module> favorites() { return modules().stream().filter(Module::favorite).toList(); }
    public static int categoryCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().categoryCount(category); }
    public static int enabledCount() { return ArsonClient.getInstance() == null ? 0 : ArsonClient.getInstance().modules().enabledCount(); }
    public static List<Module> search(String query) { return ArsonClient.getInstance() == null ? List.of() : List.copyOf(ArsonClient.getInstance().modules().search(query)); }
    public static int resetCategory(Module.Category category) { if (ArsonClient.getInstance() == null || category == null) return 0; int changed = 0; for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == category) { module.resetToDefaults(); changed++; } if (changed > 0) save(); return changed; }
    public static int enabledCount(Module.Category category) { return ArsonClient.getInstance() == null || category == null ? 0 : ArsonClient.getInstance().modules().enabledCount(category); }
    public static int setEnabled(Module.Category category, boolean enabled) { if (ArsonClient.getInstance() == null || category == null) return 0; int changed = 0; for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == category && module.enabled() != enabled) { module.setEnabled(enabled); changed++; } if (changed > 0) save(); return changed; }
    public static boolean setEnabled(String id, boolean enabled) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().setEnabled(enabled); save(); return true; }
    public static boolean setFavorite(String id, boolean favorite) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().setFavorite(favorite); save(); return true; }
    public static boolean resetSettings(String id) { Optional<Module> module = module(id); if (module.isEmpty()) return false; module.get().resetSettings(); save(); return true; }
    public static boolean setKeyCode(String id, int keyCode) { Optional<Module> module = module(id); if (module.isEmpty() || keyCode < 0) return false; module.get().setKeyCode(keyCode); save(); return true; }
    public static void save() { if (ArsonClient.getInstance() != null) ArsonClient.getInstance().saveConfig(); }
}
