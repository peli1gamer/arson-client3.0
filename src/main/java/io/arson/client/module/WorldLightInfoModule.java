package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LightLayer;

/** Live block/sky-light telemetry at the local player's position. */
public final class WorldLightInfoModule extends Module {
    private final BooleanSetting showSky = setting(new BooleanSetting("show-sky", "Show Sky Light", true));
    private int blockLight;
    private int skyLight;

    public WorldLightInfoModule() {
        super("world-light-info", "World Light Info", Category.WORLD,
            "Reports live block and sky light at the local player position.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null || client.player == null) { blockLight = 0; skyLight = 0; return; }
        var pos = client.player.blockPosition();
        blockLight = client.level.getBrightness(LightLayer.BLOCK, pos);
        skyLight = client.level.getBrightness(LightLayer.SKY, pos);
    }

    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }
    public boolean showSky() { return showSky.enabled(); }
    public String formatted() {
        return showSky() ? "Light Block " + blockLight + "  Sky " + skyLight : "Light Block " + blockLight;
    }
}
