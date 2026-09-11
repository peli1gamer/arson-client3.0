package io.arson.client;

import io.arson.client.config.ConfigManager;
import io.arson.client.module.ModuleManager;
import io.arson.client.ui.ArsonScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class ArsonClient implements ClientModInitializer {
    public static final String MOD_ID = "arson";

    private static ArsonClient instance;
    private ModuleManager moduleManager;
    private KeyMapping openMenuKey;

    public static ArsonClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        moduleManager = new ModuleManager();
        moduleManager.registerDefaults();

        KeyMapping.Category category = KeyMapping.Category.register(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "main")
        );

        openMenuKey = KeyBindingHelper.registerKeyMapping(new KeyMapping(
                "key.arson.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                Screen current = client.gui.screen();
                if (current instanceof ArsonScreen) {
                    client.gui.setScreen(null);
                } else {
                    client.gui.setScreen(new ArsonScreen(current));
                }
            }

            moduleManager.tick(client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null && client.level.getGameTime() % 200 == 0) {
                ConfigManager.save(client, moduleManager);
            }
        });

        Minecraft client = Minecraft.getInstance();
        ConfigManager.load(client, moduleManager);

        Component startup = Component.literal("Arson V3 initialized");
        if (client.player != null) {
            client.player.displayClientMessage(startup, true);
        }
    }

    public ModuleManager modules() {
        return moduleManager;
    }
}
