package io.arson.client;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.BlockESPModule;
import io.arson.client.module.ContainerESPModule;
import io.arson.client.module.EntityESPModule;
import io.arson.client.module.EntityInfoModule;
import io.arson.client.module.EntityTracerModule;
import io.arson.client.module.ModuleManager;
import io.arson.client.render.BlockRenderStage;
import io.arson.client.render.BlockScanner;
import io.arson.client.render.CombatInfoRenderer;
import io.arson.client.render.EntityInfoStage;
import io.arson.client.render.EntityRenderStage;
import io.arson.client.render.EntityScanner;
import io.arson.client.render.EntityTracerStage;
import io.arson.client.render.HudRenderer;
import io.arson.client.render.StorageRenderStage;
import io.arson.client.render.StorageScanner;
import io.arson.client.render.WorldRenderBridge;
import io.arson.client.ui.ArsonScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class ArsonClient implements ClientModInitializer {
    public static final String MOD_ID = "arson";

    private static ArsonClient instance;
    private ModuleManager moduleManager;
    private KeyMapping openMenuKey;
    private WorldRenderBridge worldRenderBridge;

    public static ArsonClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        moduleManager = new ModuleManager();
        moduleManager.registerDefaults();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main")
        );

        openMenuKey = KeyBindingHelper.registerKeyMapping(new KeyMapping(
                "key.arson.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
        ));

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                HudRenderer::render
        );
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "combat_info"),
                CombatInfoRenderer::render
        );

        worldRenderBridge = new WorldRenderBridge();
        Minecraft client = Minecraft.getInstance();

        StorageRenderProfile storageProfile = new StorageRenderProfile();
        StorageOverlay storageOverlay = new StorageOverlay(storageProfile);
        ContainerESPModule containerESP = (ContainerESPModule) moduleManager.get("container-esp");
        worldRenderBridge.register(new StorageRenderStage(
                client, storageOverlay, new StorageScanner(), containerESP));

        EntityScanner entityScanner = new EntityScanner();
        EntityESPModule entityESP = (EntityESPModule) moduleManager.get("entity-esp");
        worldRenderBridge.register(new EntityRenderStage(client, entityScanner, entityESP));

        EntityInfoModule entityInfo = (EntityInfoModule) moduleManager.get("entity-info");
        worldRenderBridge.register(new EntityInfoStage(client, entityScanner, entityInfo));

        EntityTracerModule entityTracers = (EntityTracerModule) moduleManager.get("entity-tracers");
        worldRenderBridge.register(new EntityTracerStage(client, entityScanner, entityTracers));

        BlockESPModule blockESP = (BlockESPModule) moduleManager.get("block-esp");
        worldRenderBridge.register(new BlockRenderStage(client, new BlockScanner(), blockESP));

        worldRenderBridge.attach();

        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            while (openMenuKey.consumeClick()) {
                Screen current = clientTick.gui.screen();
                if (current instanceof ArsonScreen) {
                    clientTick.gui.setScreen(null);
                } else {
                    clientTick.gui.setScreen(new ArsonScreen(current));
                }
            }

            moduleManager.tick(clientTick);
        });

        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            if (clientTick.player != null && clientTick.level != null && clientTick.level.getGameTime() % 200 == 0) {
                ConfigManager.save(clientTick, moduleManager);
            }
        });

        ConfigManager.load(client, moduleManager);

        Component startup = Component.literal("Arson V3 initialized");
        if (client.player != null) {
            client.player.displayClientMessage(startup, true);
        }
    }

    public ModuleManager modules() {
        return moduleManager;
    }

    public WorldRenderBridge worldRenderBridge() {
        return worldRenderBridge;
    }
}
