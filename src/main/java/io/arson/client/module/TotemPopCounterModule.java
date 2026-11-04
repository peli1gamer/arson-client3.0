package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/** Tracks server entity-status totem pops per player for the current session. */
public final class TotemPopCounterModule extends Module {
    private final BooleanSetting self = setting(new BooleanSetting("self", "Track Self", true));
    private final BooleanSetting players = setting(new BooleanSetting("players", "Track Players", true));
    private final BooleanSetting chat = setting(new BooleanSetting("chat", "Chat Notification", true));
    private final DoubleSetting resetAfterSeconds = setting(new DoubleSetting("reset-after", "Reset After Seconds", 0.0, 0.0, 600.0, 5.0));

    private final Map<UUID, Integer> pops = new HashMap<>();
    private final Map<UUID, Long> lastPopTick = new HashMap<>();

    public TotemPopCounterModule() {
        super("totem-pop-counter", "Totem Pop Counter", Category.COMBAT);
    }

    @Override
    protected void onDisable() {
        pops.clear();
        lastPopTick.clear();
    }

    @Override
    protected void onTick(MinecraftClient client) {
        if (client.world == null) return;
        double seconds = resetAfterSeconds.get();
        if (seconds <= 0.0) return;
        long now = client.world.getTime();
        long timeout = (long) Math.ceil(seconds * 20.0);
        lastPopTick.entrySet().removeIf(entry -> now - entry.getValue() > timeout);
        pops.keySet().removeIf(uuid -> !lastPopTick.containsKey(uuid));
    }

    /** Called by the client packet mixin when vanilla receives entity-status 35. */
    public void onTotemPop(Entity entity) {
        if (!enabled() || !(entity instanceof PlayerEntity player)) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (player == client.player && !self.enabled()) return;
        if (player != client.player && !players.enabled()) return;

        UUID uuid = player.getUuid();
        int count = pops.getOrDefault(uuid, 0) + 1;
        pops.put(uuid, count);
        lastPopTick.put(uuid, client.world.getTime());

        if (chat.enabled()) {
            String name = player.getName().getString();
            String message = name + " popped " + count + " totem" + (count == 1 ? "" : "s");
            client.player.sendMessage(Text.literal(message), false);
        }
    }

    public int getPops(PlayerEntity player) {
        return player == null ? 0 : pops.getOrDefault(player.getUuid(), 0);
    }

    public void reset(PlayerEntity player) {
        if (player == null) return;
        UUID uuid = player.getUuid();
        pops.remove(uuid);
        lastPopTick.remove(uuid);
    }

    public Map<UUID, Integer> snapshot() {
        return Map.copyOf(pops);
    }
}
