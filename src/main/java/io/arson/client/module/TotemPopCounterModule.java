package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks server entity-status totem pops per player for the current session. */
public final class TotemPopCounterModule extends Module {
    private final BooleanSetting self = setting(new BooleanSetting("self", "Track Self", true));
    private final BooleanSetting players = setting(new BooleanSetting("players", "Track Players", true));
    private final BooleanSetting chat = setting(new BooleanSetting("chat", "Chat Notification", true));
    private final DoubleSetting resetAfterSeconds = setting(new DoubleSetting("reset-after", "Reset After Seconds", 0.0, 0.0, 600.0, 5.0));

    private final Map<UUID, Integer> pops = new HashMap<>();
    private final Map<UUID, Long> lastPopTick = new HashMap<>();
    private Object trackedLevel;
    private long lastObservedGameTime = Long.MIN_VALUE;

    public TotemPopCounterModule() {
        super("totem-pop-counter", "Totem Pop Counter", Category.COMBAT);
    }

    @Override
    protected void onDisable() {
        clearSession();
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.level == null) {
            clearSession();
            return;
        }
        long now = client.level.getGameTime();
        if (trackedLevel != client.level || now < lastObservedGameTime) {
            pops.clear();
            lastPopTick.clear();
        }
        trackedLevel = client.level;
        lastObservedGameTime = now;
        double seconds = resetAfterSeconds.get();
        if (seconds <= 0.0) return;
        long timeout = (long) Math.ceil(seconds * 20.0);
        lastPopTick.entrySet().removeIf(entry -> now - entry.getValue() > timeout);
        pops.keySet().removeIf(uuid -> !lastPopTick.containsKey(uuid));
    }

    private void clearSession() {
        pops.clear();
        lastPopTick.clear();
        trackedLevel = null;
        lastObservedGameTime = Long.MIN_VALUE;
    }

    /** Called by the client packet mixin when vanilla receives entity-status 35. */
    public void onTotemPop(Entity entity) {
        if (!enabled() || !(entity instanceof Player player)) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        if (player == client.player && !self.enabled()) return;
        if (player != client.player && !players.enabled()) return;

        UUID uuid = player.getUUID();
        int count = pops.getOrDefault(uuid, 0) + 1;
        pops.put(uuid, count);
        lastPopTick.put(uuid, client.level.getGameTime());

        if (chat.enabled()) {
            String name = player.getName().getString();
            String message = name + " popped " + count + " totem" + (count == 1 ? "" : "s");
            client.player.displayClientMessage(Component.literal(message), false);
        }
    }

    public int getPops(Player player) {
        return player == null ? 0 : pops.getOrDefault(player.getUUID(), 0);
    }

    public void reset(Player player) {
        if (player == null) return;
        UUID uuid = player.getUUID();
        pops.remove(uuid);
        lastPopTick.remove(uuid);
    }

    public Map<UUID, Integer> snapshot() {
        return Map.copyOf(pops);
    }
}
