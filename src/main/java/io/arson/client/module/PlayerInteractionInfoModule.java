package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Live local interaction-target telemetry; it never performs an interaction. */
public final class PlayerInteractionInfoModule extends Module {
    private String target = "None";
    private double distance;

    public PlayerInteractionInfoModule() {
        super("player-interaction-info", "Interaction Info", Category.PLAYER,
                "Reports the local crosshair entity target and distance without issuing interactions.");
    }

    @Override protected void onTick(Minecraft client) {
        target = "None";
        distance = 0;
        if (client.player == null || client.crosshairPickEntity == null) return;
        var entity = client.crosshairPickEntity;
        target = entity.getType().toString();
        distance = client.player.distanceTo(entity);
    }

    public String target() { return target; }
    public double distance() { return distance; }

    public String formatted() {
        return target.equals("None") ? "Target None" : String.format(java.util.Locale.ROOT, "Target %s %.1fm", target, distance);
    }
}
