package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.LightType;

/** Live block/sky-light telemetry at the local player's position. */
public final class WorldLightInfoModule extends Module {
    private final BooleanSetting showSky = setting(new BooleanSetting("show-sky", "Show Sky Light", true));
    private int blockLight;
    private int skyLight;

    public WorldLightInfoModule() {
        super("world-light-info", "World Light Info", Category.WORLD,
            "Reports live block and sky light at the local player position.");
    }

    @Override protected void onTick(MinecraftClient client) {
        if (client.world == null || client.player == null) { blockLight = 0; skyLight = 0; return; }
        var pos = client.player.getBlockPos();
        blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
        skyLight = client.world.getLightLevel(LightType.SKY, pos);
    }

    public int blockLight() { return blockLight; }
    public int skyLight() { return skyLight; }
    public boolean showSky() { return showSky.enabled(); }
    public String formatted() {
        return showSky() ? "Light Block " + blockLight + "  Sky " + skyLight : "Light Block " + blockLight;
    }
}
