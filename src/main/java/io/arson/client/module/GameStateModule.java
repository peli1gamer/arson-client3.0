package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.EnumSetting;
import net.minecraft.client.Minecraft;

/** Local-only telemetry for HUDs and command inspection; never changes gameplay state. */
public final class GameStateModule extends Module {
    public enum Detail { COMPACT, DETAILED }
    private final EnumSetting<Detail> detail = setting(new EnumSetting<>("detail", "Detail", Detail.COMPACT));
    private final BooleanSetting showTicks = setting(new BooleanSetting("show-ticks", "Show Ticks", false));
    private long dayTime;
    private int localTick;
    private GameStateModule() { super("game-state", "Game State", Category.MISC, "Exposes local world time and client tick counters for HUDs and diagnostics."); }
    public static GameStateModule create() { return new GameStateModule(); }
    @Override protected void onTick(Minecraft client) {
        localTick++;
        dayTime = client.level == null ? 0L : client.level.getDayTime();
    }
    public Detail detail() { return detail.get(); }
    public boolean showTicks() { return showTicks.enabled(); }
    public long dayTime() { return dayTime; }
    public int localTick() { return localTick; }
}
