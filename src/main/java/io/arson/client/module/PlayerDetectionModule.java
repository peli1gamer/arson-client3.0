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
    private final DoubleSetting displayLimit = setting(new DoubleSetting("display-limit", "Displayed Players", 3.0, 1.0, 8.0, 1.0));
    private final List<PlayerSnapshot> nearby = new ArrayList<>();

    public PlayerDetectionModule() {
        super("player-detection", "Player Detection", Category.RENDER,
                "Tracks nearby players for HUDs and visual integrations without altering player or world state.");
        displayLimit.description("Maximum nearby player rows shown in the player HUD.");
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
    public int displayLimit() { return (int) Math.round(displayLimit.get()); }
    public List<String> formattedLines() { return formatSnapshots(nearby, displayLimit()); }
    static List<String> formatSnapshots(List<PlayerSnapshot> snapshots, int limit) {
        List<PlayerSnapshot> safe = snapshots == null ? List.of() : snapshots;
        List<String> rows = new ArrayList<>();
        rows.add("Nearby Players " + safe.size());
        if (safe.isEmpty()) rows.add("  None");
        else safe.stream().limit(Math.max(1, limit)).forEach(player -> rows.add(String.format(
                java.util.Locale.ROOT, "  %s  %.1fm  %.1f HP", player.name(), player.distance(), player.health())));
        return List.copyOf(rows);
    }
    public record PlayerSnapshot(String name, double distance, float health) {}
}
