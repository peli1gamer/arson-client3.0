package io.arson.client.module;

import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable ore/valuable block visualization. Scanning is cached by BlockRenderStage. */
public final class BlockESPModule extends VisualModule {
    private final BooleanSetting diamond = setting(new BooleanSetting("diamond", "Diamond", true));
    private final BooleanSetting emerald = setting(new BooleanSetting("emerald", "Emerald", true));
    private final BooleanSetting gold = setting(new BooleanSetting("gold", "Gold", false));
    private final BooleanSetting iron = setting(new BooleanSetting("iron", "Iron", false));
    private final BooleanSetting copper = setting(new BooleanSetting("copper", "Copper", false));
    private final BooleanSetting coal = setting(new BooleanSetting("coal", "Coal", false));
    private final BooleanSetting redstone = setting(new BooleanSetting("redstone", "Redstone", false));
    private final BooleanSetting lapis = setting(new BooleanSetting("lapis", "Lapis", false));
    private final BooleanSetting quartz = setting(new BooleanSetting("quartz", "Quartz", false));
    private final BooleanSetting ancientDebris = setting(new BooleanSetting("ancient-debris", "Ancient Debris", true));
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 32.0, 8.0, 64.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 8.0, 1.0, 20.0, 1.0));

    private final ColorSetting diamondColor = setting(new ColorSetting("diamond-color", "Diamond Color", 0xD855FFFF));
    private final ColorSetting emeraldColor = setting(new ColorSetting("emerald-color", "Emerald Color", 0xD855FF78));
    private final ColorSetting goldColor = setting(new ColorSetting("gold-color", "Gold Color", 0xD8FFD34E));
    private final ColorSetting ironColor = setting(new ColorSetting("iron-color", "Iron Color", 0xD8E0E0E0));
    private final ColorSetting copperColor = setting(new ColorSetting("copper-color", "Copper Color", 0xD8E0784E));
    private final ColorSetting coalColor = setting(new ColorSetting("coal-color", "Coal Color", 0xD8505050));
    private final ColorSetting redstoneColor = setting(new ColorSetting("redstone-color", "Redstone Color", 0xD8FF4040));
    private final ColorSetting lapisColor = setting(new ColorSetting("lapis-color", "Lapis Color", 0xD84D74FF));
    private final ColorSetting quartzColor = setting(new ColorSetting("quartz-color", "Quartz Color", 0xD8FFF0D0));
    private final ColorSetting ancientDebrisColor = setting(new ColorSetting("ancient-debris-color", "Ancient Debris Color", 0xD88A4B35));

    public BlockESPModule() {
        super("block-esp", "Block ESP", false);
    }

    public boolean showDiamond() { return diamond.enabled(); }
    public boolean showEmerald() { return emerald.enabled(); }
    public boolean showGold() { return gold.enabled(); }
    public boolean showIron() { return iron.enabled(); }
    public boolean showCopper() { return copper.enabled(); }
    public boolean showCoal() { return coal.enabled(); }
    public boolean showRedstone() { return redstone.enabled(); }
    public boolean showLapis() { return lapis.enabled(); }
    public boolean showQuartz() { return quartz.enabled(); }
    public boolean showAncientDebris() { return ancientDebris.enabled(); }
    public boolean distanceFade() { return distanceFade.enabled(); }
    public double range() { return range.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }

    public RenderStyle diamondStyle() { return renderStyle(diamondColor.get()); }
    public RenderStyle emeraldStyle() { return renderStyle(emeraldColor.get()); }
    public RenderStyle goldStyle() { return renderStyle(goldColor.get()); }
    public RenderStyle ironStyle() { return renderStyle(ironColor.get()); }
    public RenderStyle copperStyle() { return renderStyle(copperColor.get()); }
    public RenderStyle coalStyle() { return renderStyle(coalColor.get()); }
    public RenderStyle redstoneStyle() { return renderStyle(redstoneColor.get()); }
    public RenderStyle lapisStyle() { return renderStyle(lapisColor.get()); }
    public RenderStyle quartzStyle() { return renderStyle(quartzColor.get()); }
    public RenderStyle ancientDebrisStyle() { return renderStyle(ancientDebrisColor.get()); }
}
