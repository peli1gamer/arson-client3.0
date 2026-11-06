package io.arson.client.module;

import io.arson.client.ArsonClient;
import io.arson.client.settings.BooleanSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

/** Exposes real Arson and Minecraft version metadata to HUD integrations. */
public final class ClientInfoModule extends Module {
    private final BooleanSetting showVersion = setting(new BooleanSetting(
            "show-version", "Show Version", true));

    public ClientInfoModule() {
        super("client-info", "Client Info", Category.MISC,
                "Reports the installed Arson Client and Minecraft versions in the World Info HUD.");
    }

    public boolean showVersion() {
        return showVersion.enabled();
    }

    public String formatted() {
        if (!showVersion()) return "Arson Client";
        String arsonVersion = FabricLoader.getInstance().getModContainer(ArsonClient.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        String minecraftVersion = SharedConstants.getGameVersion().name();
        return "Arson " + arsonVersion + "  ·  Minecraft " + minecraftVersion;
    }
}
