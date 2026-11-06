package io.arson.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

/** Tracks connection metadata for the HUD and addon API without changing networking state. */
public final class ServerInfoModule extends Module {
    private String name = "Singleplayer";
    private String address = "";
    private boolean multiplayer;
    private boolean lan;

    public ServerInfoModule() {
        super("server-info", "Server Info", Category.MISC,
                "Tracks the current server name/address and connection type for HUD integrations.");
    }

    @Override protected void onTick(MinecraftClient client) {
        ServerInfo server = client.getCurrentServerEntry();
        if (server == null) {
            name = "Singleplayer";
            address = "";
            multiplayer = false;
            lan = false;
            return;
        }
        name = server.name == null || server.name.isBlank() ? "Server" : server.name;
        address = server.address == null ? "" : server.address;
        multiplayer = true;
        lan = server.isLocal();
    }

    public String serverName() { return name; }
    public String address() { return address; }
    public boolean multiplayer() { return multiplayer; }
    public boolean lan() { return lan; }
    public String formatted() { return multiplayer ? name + " • " + address : name; }
}
