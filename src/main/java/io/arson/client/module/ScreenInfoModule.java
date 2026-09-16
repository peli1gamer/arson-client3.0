package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Local UI state inspection for HUDs and diagnostics; no input automation. */
public final class ScreenInfoModule extends Module {
    private final BooleanSetting includeClassName = setting(new BooleanSetting("class-name", "Class Name", true));
    private String screenName = "none";
    private ScreenInfoModule() { super("screen-info", "Screen Info", Category.MISC, "Reports the currently open local Minecraft screen for HUDs and troubleshooting."); }
    public static ScreenInfoModule create() { return new ScreenInfoModule(); }
    @Override protected void onTick(Minecraft client) {
        if (client.screen == null) { screenName = "none"; return; }
        screenName = includeClassName.enabled() ? client.screen.getClass().getSimpleName() : "open";
    }
    public boolean includeClassName() { return includeClassName.enabled(); }
    public String screenName() { return screenName; }
}
