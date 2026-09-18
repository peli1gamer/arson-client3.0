package io.arson.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LightLayer;

/** Tracks live light and sea-level information at the local player position. */
public final class WorldLightInfoModule extends Module {
    private int blockLight;
    private int skyLight;
    private int seaLevel;
    private int y;

    public WorldLightInfoModule() {
        super("world-light-info", "World Light Info", Category.WORLD,
                "Tracks block light, sky light, sea level, and current Y for local environmental HUD presentation.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            blockLight = skyLight = seaLevel = y = 0;
            return;
        }
        var level = client.level;
        var pos = client.player.blockPosition();
        blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        skyLight = level.getBrightness(LightLayer.SKY, pos);
        seaLevel = level.getSeaLevel();
        y = pos.getY();
    }

    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }
    public int seaLevel() { return seaLevel; }
    public int y() { return y; }

    public String formatted() {
        return "Light Block " + blockLight + "  Sky " + skyLight + "  Sea " + seaLevel;
    }
}
