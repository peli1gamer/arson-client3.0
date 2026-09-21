package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Live local-world entity counts for HUD and diagnostics. */
public final class WorldEntityCountInfoModule extends Module {
    private int total;
    private int players;
    private int living;
    private int items;

    public WorldEntityCountInfoModule() {
        super("world-entity-count-info", "World Entity Counts", Category.WORLD,
                "Reports live client-side entity counts currently available in the local world.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null) {
            total = players = living = items = 0;
            return;
        }
        total = 0; players = 0; living = 0; items = 0;
        for (var entity : client.level.entitiesForRendering()) {
            total++;
            if (entity instanceof net.minecraft.world.entity.player.Player) players++;
            if (entity instanceof net.minecraft.world.entity.LivingEntity) living++;
            if (entity instanceof net.minecraft.world.entity.item.ItemEntity) items++;
        }
    }

    public int total() { return total; }
    public int players() { return players; }
    public int living() { return living; }
    public int items() { return items; }

    public String formatted() {
        return "Entities " + total + "  Players " + players + "  Living " + living + "  Items " + items;
    }
}
