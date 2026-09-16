package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks local dimension and biome identifiers for information HUDs. */
public final class WorldEnvironmentModule extends Module {
    private String dimension = "unknown";
    private String biome = "unknown";

    public WorldEnvironmentModule() {
        super("world-environment", "World Environment", Category.WORLD,
                "Tracks the current dimension and local biome identifier for information HUDs; it never changes world state.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            dimension = "unknown";
            biome = "unknown";
            return;
        }
        dimension = client.level.dimension().toString();
        var holder = client.level.getBiome(client.player.blockPosition());
        biome = holder.unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");
    }

    public String dimension() { return dimension; }
    public String biome() { return biome; }
    public String formatted() { return dimension + " · " + biome; }
}
