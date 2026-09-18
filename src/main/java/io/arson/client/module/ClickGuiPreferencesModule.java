package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.EnumSetting;

/** Persistent presentation preferences used by the Arson ClickGUI. */
public final class ClickGuiPreferencesModule extends Module {
    public enum Theme { MIDNIGHT, GRAPHITE, CONTRAST }
    public enum SortMode { FAVORITES_FIRST, ENABLED_FIRST, NAME }

    private final EnumSetting<Theme> theme = setting(new EnumSetting<>("theme", "Theme", Theme.MIDNIGHT));
    private final EnumSetting<SortMode> sortMode = setting(new EnumSetting<>("sort-mode", "Module Sort", SortMode.FAVORITES_FIRST));
    private final BooleanSetting favoritesOnly = setting(new BooleanSetting("favorites-only", "Favorites Filter", false));
    private final BooleanSetting enabledOnly = setting(new BooleanSetting("enabled-only", "Enabled Filter", false));

    public ClickGuiPreferencesModule() {
        super("clickgui-preferences", "ClickGUI Preferences", Category.MISC,
                "Persists ClickGUI theme, module ordering, and filter preferences.");
    }

    public Theme theme() { return theme.get(); }
    public SortMode sortMode() { return sortMode.get(); }
    public boolean favoritesOnly() { return favoritesOnly.enabled(); }
    public boolean enabledOnly() { return enabledOnly.enabled(); }
    public void cycleTheme() { theme.cycle(1); }
    public void cycleSortMode() { sortMode.cycle(1); }
    public void setFavoritesOnly(boolean value) { favoritesOnly.set(value); }
    public void setEnabledOnly(boolean value) { enabledOnly.set(value); }
}
