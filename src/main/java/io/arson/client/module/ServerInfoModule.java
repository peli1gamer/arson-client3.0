package io.arson.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;

/** Tracks connection metadata for the HUD and addon API without changing networking state. */
public final class ServerInfoModule extends Module {
    private String name = "Singleplayer";
    private String address = "";
    private boolean multiplayer;
    private boolean lan;
    private int latency = -1;
    private int onlinePlayers = -1;

    public ServerInfoModule() {
        super("server-info", "Server Info", Category.MISC,
                "Tracks the current server name/address, latency, and connection type for HUD integrations.");
    }

    @Override protected void onTick(Minecraft client) {
        ServerData server = client.getCurrentServer();
        if (server == null) {
            name = "Singleplayer";
            address = "";
            multiplayer = false;
            lan = false;
            latency = -1;
            onlinePlayers = -1;
            return;
        }
        name = server.name == null || server.name.isBlank() ? "Server" : server.name;
        address = server.ip == null ? "" : server.ip;
        multiplayer = true;
        lan = server.isLan();
        var connection = client.getConnection();
        PlayerInfo currentPlayer = connection == null || client.player == null
                ? null : connection.getPlayerInfo(client.player.getUUID());
        latency = currentPlayer == null ? -1 : Math.max(0, currentPlayer.getLatency());
        onlinePlayers = connection == null ? -1 : connection.getOnlinePlayers().size();
    }

    public String serverName() { return name; }
    public String address() { return address; }
    public boolean multiplayer() { return multiplayer; }
    public boolean lan() { return lan; }
    public int latency() { return latency; }
    public int onlinePlayers() { return onlinePlayers; }
    public String formatted() {
        if (!multiplayer) return name;
        return name + " • " + (lan ? "LAN" : address) + " • " + formatLatency(latency)
                + " • " + formatPlayerCount(onlinePlayers);
    }
    public static String formatPlayerCount(int count) {
        return count < 0 ? "Players —" : "Players " + count;
    }
    public static String formatLatency(int latency) {
        return latency < 0 ? "Ping —" : "Ping " + latency + " ms";
    }
}
