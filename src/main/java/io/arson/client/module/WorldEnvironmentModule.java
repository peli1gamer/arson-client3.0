package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.LightType;

/** Tracks local dimension, biome, weather, difficulty, chunk, and light information for HUDs. */
public final class WorldEnvironmentModule extends Module {
    private final BooleanSetting weather = setting(new BooleanSetting("weather", "Weather", true));
    private final BooleanSetting difficulty = setting(new BooleanSetting("difficulty", "Difficulty", true));
    private final BooleanSetting chunk = setting(new BooleanSetting("chunk", "Chunk", true));
    private final BooleanSetting light = setting(new BooleanSetting("light", "Block Light", true));
    private String dimension = "unknown";
    private String biome = "unknown";
    private String weatherState = "unknown";
    private String difficultyName = "unknown";
    private int chunkX;
    private int chunkZ;
    private int blockLight;

    public WorldEnvironmentModule() {
        super("world-environment", "World Environment", Category.WORLD,
                "Tracks live dimension, biome, weather, difficulty, chunk, and local block-light state for information HUDs; it never changes world state.");
    }

    @Override
    protected void onTick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            dimension = "unknown";
            biome = "unknown";
            weatherState = "unknown";
            difficultyName = "unknown";
            chunkX = chunkZ = 0;
            blockLight = 0;
            return;
        }
        var pos = client.player.getBlockPos();
        dimension = client.world.getRegistryKey().toString();
        var holder = client.world.getBiome(pos);
        biome = holder.getKey()
                .map(key -> key.getValue().toString())
                .orElse("unknown");
        weatherState = client.world.isThundering() ? "Thunder" : client.world.isRaining() ? "Rain" : "Clear";
        difficultyName = client.world.getDifficulty().toString();
        chunkX = pos.getX() >> 4;
        chunkZ = pos.getZ() >> 4;
        blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
    }

    public String dimension() { return dimension; }
    public String biome() { return biome; }
    public String weather() { return weatherState; }
    public String difficulty() { return difficultyName; }
    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }
    public int blockLight() { return blockLight; }
    public boolean showWeather() { return weather.enabled(); }
    public boolean showDifficulty() { return difficulty.enabled(); }
    public boolean showChunk() { return chunk.enabled(); }
    public boolean showLight() { return light.enabled(); }
    public String formatted() { return dimension + " · " + biome; }
}
