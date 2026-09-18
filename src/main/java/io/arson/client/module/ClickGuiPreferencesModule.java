package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;

/** Persistent presentation preferences for the Arson ClickGUI. */
public final class ClickGuiPreferencesModule extends Module {
    public enum Theme { MIDNIGHT, GRAPHITE, CONTRAST }
    private final EnumSetting<Theme> theme = setting(new EnumSetting<>("theme", "Theme", Theme.MIDNIGHT));
    private final BooleanSetting alphabetical = setting(new BooleanSetting("alphabetical", "Alphabetical", false));
    private final BooleanSetting favoritesOnly = setting(new BooleanSetting("favorites-only", "Favorites Only", false));
    private final BooleanSetting enabledOnly = setting(new BooleanSetting("enabled-only", "Enabled Only", false));
    private final DoubleSetting panelScale = setting(new DoubleSetting("panel-scale", "Panel Scale", 1.0, 0.75, 1.25, 0.05));

    public ClickGuiPreferencesModule() {
        super("clickgui-preferences", "ClickGUI Preferences", Category.MISC,
                "Persists local ClickGUI theme, filters, sorting, and panel scale preferences.");
    }

    public Theme theme() { return theme.get(); }
    public void cycleTheme() { theme.cycle(1); }
    public boolean alphabetical() { return alphabetical.enabled(); }
    public void setAlphabetical(boolean value) { alphabetical.set(value); }
    public boolean favoritesOnly() { return favoritesOnly.enabled(); }
    public void setFavoritesOnly(boolean value) { favoritesOnly.set(value); }
    public boolean enabledOnly() { return enabledOnly.enabled(); }
    public void setEnabledOnly(boolean value) { enabledOnly.set(value); }
    public double panelScale() { return panelScale.get(); }
    public void setPanelScale(double value) { panelScale.set(value); }
}
