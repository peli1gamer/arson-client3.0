package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Maintains a lightweight nearby-player snapshot for HUD and utility integrations. */
public final class PlayerDetectionModule extends Module {
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 256.0, 4.0));
    private final BooleanSetting includeSelf = setting(new BooleanSetting("include-self", "Include Self", false));
    private final BooleanSetting sortByDistance = setting(new BooleanSetting("sort-distance", "Sort By Distance", true));
    private final List<PlayerSnapshot> nearby = new ArrayList<>();

    public PlayerDetectionModule() {
        super("player-detection", "Player Detection", Category.RENDER,
                "Tracks nearby players for HUDs and visual integrations without altering player or world state.");
    }

    @Override protected void onTick(Minecraft client) {
        nearby.clear();
        if (client.player == null || client.level == null) return;
        double maxDistance = range.get() * range.get();
        for (Player player : client.level.players()) {
            if (!includeSelf.enabled() && player == client.player) continue;
            double distance = client.player.distanceToSqr(player);
            if (distance <= maxDistance) nearby.add(new PlayerSnapshot(player.getName().getString(), Math.sqrt(distance), player.getHealth()));
        }
        if (sortByDistance.enabled()) nearby.sort(java.util.Comparator.comparingDouble(PlayerSnapshot::distance));
    }

    public double range() { return range.get(); }
    public List<PlayerSnapshot> nearby() { return Collections.unmodifiableList(nearby); }
    public int count() { return nearby.size(); }
    public record PlayerSnapshot(String name, double distance, float health) {}
}
