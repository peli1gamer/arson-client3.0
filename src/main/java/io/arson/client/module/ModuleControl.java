package io.arson.client.module;

/** Pure module state operations shared by the public API and command surface. */
public final class ModuleControl {
    private ModuleControl() {}

    public static boolean setFavorite(ModuleManager modules, String id, boolean favorite) {
        Module module = find(modules, id);
        if (module == null) return false;
        module.setFavorite(favorite);
        return true;
    }

    /** Returns the new favorite state, or false when the requested module does not exist. */
    public static boolean toggleFavorite(ModuleManager modules, String id) {
        Module module = find(modules, id);
        if (module == null) return false;
        module.setFavorite(!module.favorite());
        return true;
    }

    public static boolean toggleEnabled(ModuleManager modules, String id) {
        Module module = find(modules, id);
        if (module == null) return false;
        module.toggle();
        return true;
    }

    public static boolean setKeyCode(ModuleManager modules, String id, int keyCode) {
        if (keyCode < 0) return false;
        Module module = find(modules, id);
        if (module == null) return false;
        module.setKeyCode(keyCode);
        return true;
    }

    public record Snapshot(String id, boolean enabled, boolean favorite, int keyCode, int settingCount) {
        public boolean hasKeybind() { return keyCode != 0; }
        public String formatted() {
            return id + " enabled=" + enabled + " favorite=" + favorite
                    + " keybind=" + (hasKeybind() ? keyCode : "none") + " settings=" + settingCount;
        }
    }

    public static java.util.Optional<Snapshot> snapshot(ModuleManager modules, String id) {
        Module module = find(modules, id);
        if (module == null) return java.util.Optional.empty();
        return java.util.Optional.of(new Snapshot(module.id(), module.enabled(), module.favorite(),
                module.keyCode(), module.settings().size()));
    }

    public static java.util.Optional<String> status(ModuleManager modules, String id) {
        return snapshot(modules, id).map(Snapshot::formatted);
    }

    private static Module find(ModuleManager modules, String id) {
        return modules == null || id == null || id.isBlank() ? null : modules.get(id.trim());
    }
}
