package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** World-state HUD configuration. Information only; no gameplay automation. */
public final class WorldInfoModule extends Module {
    private static final DateTimeFormatter LOCAL_CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private final BooleanSetting time = setting(new BooleanSetting("time", "Time", true));
    private final BooleanSetting localClock = setting(new BooleanSetting("local-clock", "Local Clock", true));
    private final BooleanSetting dimension = setting(new BooleanSetting("dimension", "Dimension", true));
    private final BooleanSetting weather = setting(new BooleanSetting("weather", "Weather", true));

    public WorldInfoModule() {
        super("world-info", "World Info", Category.WORLD);
    }

    public boolean showTime() { return time.enabled(); }
    public boolean showLocalClock() { return localClock.enabled(); }
    public String formattedLocalClock() { return formatLocalClock(LocalTime.now()); }
    public static String formatLocalClock(LocalTime time) { return LOCAL_CLOCK_FORMAT.format(time); }
    public boolean showDimension() { return dimension.enabled(); }
    public boolean showWeather() { return weather.enabled(); }
}
