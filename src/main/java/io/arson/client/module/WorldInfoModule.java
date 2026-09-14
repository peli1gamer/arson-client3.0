package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;

/** World-state HUD configuration. Information only; no gameplay automation. */
public final class WorldInfoModule extends Module {
    private final BooleanSetting time = setting(new BooleanSetting("time", "Time", true));
    private final BooleanSetting dimension = setting(new BooleanSetting("dimension", "Dimension", true));
    private final BooleanSetting weather = setting(new BooleanSetting("weather", "Weather", true));

    public WorldInfoModule() {
        super("world-info", "World Info", Category.WORLD);
    }

    public boolean showTime() { return time.enabled(); }
    public boolean showDimension() { return dimension.enabled(); }
    public boolean showWeather() { return weather.enabled(); }
}
