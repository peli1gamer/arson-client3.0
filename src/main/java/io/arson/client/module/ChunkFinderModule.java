package io.arson.client.module;

import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Scores loaded chunks for independent signs of player-built activity. */
public final class ChunkFinderModule extends Module {
    private final BooleanSetting storage = setting(new BooleanSetting("storage", "Storage Clusters", true));
    private final BooleanSetting redstone = setting(new BooleanSetting("redstone", "Redstone Components", true));
    private final BooleanSetting obsidian = setting(new BooleanSetting("obsidian", "Obsidian Concentrations", true));
    private final BooleanSetting deepslate = setting(new BooleanSetting("deepslate", "Processed Deepslate", true));
    private final BooleanSetting spawners = setting(new BooleanSetting("spawners", "Mob Spawners", true));
    private final BooleanSetting verticalVeins = setting(new BooleanSetting("vertical-veins", "Vertical Ore Veins", true));
    private final BooleanSetting concealedOre = setting(new BooleanSetting("concealed-ore", "Concealed Ore", false));

    private final DoubleSetting radius = setting(new DoubleSetting("radius", "Scan Radius (Chunks)", 4.0, 1.0, 8.0, 1.0));
    private final DoubleSetting chunksPerTick = setting(new DoubleSetting("chunks-per-tick", "Chunks Per Tick", 2.0, 1.0, 8.0, 1.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval (Ticks)", 5.0, 1.0, 20.0, 1.0));
    private final DoubleSetting minimumScore = setting(new DoubleSetting("minimum-score", "Minimum Score", 12.0, 1.0, 100.0, 1.0));
    private final DoubleSetting minimumStorage = setting(new DoubleSetting("minimum-storage", "Minimum Storage Blocks", 3.0, 1.0, 20.0, 1.0));
    private final BooleanSetting labels = setting(new BooleanSetting("labels", "Chunk Labels", true));
    private final BooleanSetting notifications = setting(new BooleanSetting("notifications", "Discovery Notifications", true));
    private final ColorSetting lowColor = setting(new ColorSetting("low-color", "Lower Score Color", 0xD8FFD84A));
    private final ColorSetting mediumColor = setting(new ColorSetting("medium-color", "Medium Score Color", 0xD8FF9D35));
    private final ColorSetting highColor = setting(new ColorSetting("high-color", "High Score Color", 0xD8FF3939));

    public ChunkFinderModule() {
        super("chunk-finder", "Chunk Finder", Category.WORLD,
                "Analyses already-loaded chunks for weighted signs of player-built bases and infrastructure.");
        radius.description("Maximum horizontal radius around the player, measured in chunks.");
        chunksPerTick.description("Maximum number of loaded chunks analysed during one scan step.");
        scanInterval.description("Ticks between scan steps. Higher values reduce client workload.");
        minimumScore.description("Only chunks reaching this score are displayed.");
        minimumStorage.description("Storage block count required before the storage signal contributes.");
    }

    public int radiusChunks() { return (int) Math.round(radius.get()); }
    public int chunksPerTick() { return (int) Math.round(chunksPerTick.get()); }
    public int scanIntervalTicks() { return (int) Math.round(scanInterval.get()); }
    public int minimumScore() { return (int) Math.round(minimumScore.get()); }
    public int minimumStorage() { return (int) Math.round(minimumStorage.get()); }
    public boolean detectStorage() { return storage.enabled(); }
    public boolean detectRedstone() { return redstone.enabled(); }
    public boolean detectObsidian() { return obsidian.enabled(); }
    public boolean detectDeepslate() { return deepslate.enabled(); }
    public boolean detectSpawners() { return spawners.enabled(); }
    public boolean detectVerticalVeins() { return verticalVeins.enabled(); }
    public boolean detectConcealedOre() { return concealedOre.enabled(); }
    public boolean showLabels() { return labels.enabled(); }
    public boolean notifyDiscoveries() { return notifications.enabled(); }

    public RenderStyle styleForScore(int score) {
        int argb = score >= 30 ? highColor.get() : score >= 20 ? mediumColor.get() : lowColor.get();
        return RenderStyle.fromArgb(argb, true, true, 0.10, 1.5);
    }
}
