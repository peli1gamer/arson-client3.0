package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks local world clock/weather state for presentation integrations. */
public final class WorldClockModule extends Module {
    private long dayTime;
    private boolean raining;
    private boolean thundering;

    public WorldClockModule() {
        super("world-clock", "World Clock", Category.WORLD,
                "Tracks local world time and weather for HUD and addon integrations; it never modifies the world.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null) {
            dayTime = 0;
            raining = false;
            thundering = false;
            return;
        }
        dayTime = Math.floorMod(client.level.getDayTime(), 24000L);
        raining = client.level.isRaining();
        thundering = client.level.isThundering();
    }

    public long dayTime() { return dayTime; }
    public boolean raining() { return raining; }
    public boolean thundering() { return thundering; }

    public String formattedTime() {
        long hours = (dayTime / 1000L + 6L) % 24L;
        long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
        if (minutes == 60) { minutes = 0; hours = (hours + 1) % 24; }
        return String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    public String formattedWeather() {
        return thundering ? "Thunder" : raining ? "Rain" : "Clear";
    }
}
