package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Exposes chunk/region coordinates derived from the local player's position. */
public final class ChunkPositionModule extends Module {
    private final BooleanSetting showRegion = setting(new BooleanSetting("show-region", "Show Region", true));
    private int chunkX;
    private int chunkZ;
    private int regionX;
    private int regionZ;

    public ChunkPositionModule() {
        super("chunk-position", "Chunk Position", Category.WORLD,
                "Tracks the local chunk and region coordinates for navigation and HUD integrations.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) return;
        chunkX = client.player.blockPosition().getX() >> 4;
        chunkZ = client.player.blockPosition().getZ() >> 4;
        regionX = chunkX >> 5;
        regionZ = chunkZ >> 5;
    }

    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }
    public int regionX() { return regionX; }
    public int regionZ() { return regionZ; }
    public boolean showRegion() { return showRegion.enabled(); }
    public String formatted() {
        String result = "Chunk " + chunkX + " " + chunkZ;
        return showRegion.enabled() ? result + " • Region " + regionX + " " + regionZ : result;
    }
}
