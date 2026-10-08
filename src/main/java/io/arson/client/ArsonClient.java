package io.arson.client;

import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import io.arson.client.command.ArsonCommand;
import io.arson.client.config.ConfigManager;
import io.arson.client.context.FeatureContext;
import io.arson.client.module.AimAssistModule;
import io.arson.client.module.BlockESPModule;
import io.arson.client.module.ContainerESPModule;
import io.arson.client.module.EntityESPModule;
import io.arson.client.module.EntityInfoModule;
import io.arson.client.module.EntityTracerModule;
import io.arson.client.module.ItemESPModule;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.notification.NotificationCenter;
import io.arson.client.platform.FabricFeatureContextAdapter;
import io.arson.client.platform.FeatureContextAdapter;
import io.arson.client.render.AimAssistRenderStage;
import io.arson.client.render.ArrayListRenderer;
import io.arson.client.render.BlockRenderStage;
import io.arson.client.render.BlockScanner;
import io.arson.client.render.CombatInfoRenderer;
import io.arson.client.render.EntityInfoStage;
import io.arson.client.render.EntityRenderStage;
import io.arson.client.render.EntityScanner;
import io.arson.client.render.EntityTracerStage;
import io.arson.client.render.HudRenderer;
import io.arson.client.render.ItemRenderStage;
import io.arson.client.render.StorageRenderStage;
import io.arson.client.render.StorageScanner;
import io.arson.client.render.WorldRenderBridge;
import io.arson.client.ui.ArsonScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public final class ArsonClient implements ClientModInitializer {
    public static final String MOD_ID = "arson";
    private static ArsonClient instance;
    private ModuleManager moduleManager;
    private KeyMapping openMenuKey;
    private WorldRenderBridge worldRenderBridge;
    private FeatureContext featureContext;
    private FeatureContextAdapter contextAdapter;
    private final Map<String, Boolean> moduleKeyStates = new HashMap<>();
    private int runtimeSmokeTick;
    private boolean runtimeSaveFailureNotified;
    public static ArsonClient getInstance() { return instance; }

    @Override public void onInitializeClient() {
        instance = this;
        RenderBoxRenderer.initialize();
        moduleManager = new ModuleManager();
        moduleManager.registerDefaults();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.arson.open_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ArsonCommand.register();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "hud"), HudRenderer::render);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "array_list"), ArrayListRenderer::render);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "combat_info"), CombatInfoRenderer::render);

        Minecraft client = Minecraft.getInstance();
        featureContext = new FeatureContext();
        contextAdapter = new FabricFeatureContextAdapter(client);
        contextAdapter.initialize(featureContext);
        worldRenderBridge = new WorldRenderBridge(featureContext, contextAdapter);

        StorageScanner storageScanner = new StorageScanner();
        ClientChunkEvents.CHUNK_LOAD.register(storageScanner::onChunkLoaded);
        ClientChunkEvents.CHUNK_UNLOAD.register(storageScanner::onChunkUnloaded);
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) ->
                storageScanner.invalidateBlockEntity(level, blockEntity.getBlockPos()));
        ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) ->
                storageScanner.invalidateBlockEntity(level, blockEntity.getBlockPos()));
        StorageOverlay storageOverlay = new StorageOverlay(new StorageRenderProfile());
        worldRenderBridge.register(new StorageRenderStage(client, storageOverlay, storageScanner, (ContainerESPModule) moduleManager.get("container-esp")));
        EntityScanner entityScanner = new EntityScanner();
        worldRenderBridge.register(new EntityRenderStage(client, entityScanner, (EntityESPModule) moduleManager.get("entity-esp")));
        worldRenderBridge.register(new ItemRenderStage(client, entityScanner, (ItemESPModule) moduleManager.get("item-esp")));
        worldRenderBridge.register(new EntityInfoStage(client, entityScanner, (EntityInfoModule) moduleManager.get("entity-info")));
        worldRenderBridge.register(new EntityTracerStage(client, entityScanner, (EntityTracerModule) moduleManager.get("entity-tracers")));
        worldRenderBridge.register(new BlockRenderStage(client, new BlockScanner(), (BlockESPModule) moduleManager.get("block-esp")));
        worldRenderBridge.register(new AimAssistRenderStage(client, (AimAssistModule) moduleManager.get("aim-assist")));
        worldRenderBridge.attach();

        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            contextAdapter.tick(featureContext);
            while (openMenuKey.consumeClick()) {
                Screen current = clientTick.screen;
                if (current instanceof ArsonScreen arsonScreen) {
                    arsonScreen.onClose();
                    NotificationCenter.push("Arson", "GUI closed");
                } else {
                    clientTick.setScreen(new ArsonScreen(current));
                    NotificationCenter.push("Arson", "GUI opened");
                }
            }
            processModuleKeybinds(clientTick);
            moduleManager.tick(clientTick);
            runRuntimeSmoke(clientTick);
        });
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            if (clientTick.player != null && clientTick.level != null && clientTick.level.getGameTime() % 200 == 0) saveConfigFromRuntime();
        });
        java.nio.file.Path configPath = client.gameDirectory.toPath().resolve("config").resolve("arson-v3.json");
        boolean existingConfig = java.nio.file.Files.isRegularFile(configPath);
        boolean configLoaded = ConfigManager.load(client, moduleManager);
        if (existingConfig && !configLoaded) {
            NotificationCenter.push("Arson config", "Could not load the existing config; the file was left unchanged.");
        }
        if (client.player != null) client.player.displayClientMessage(Component.literal("Arson V3 initialized"), true);
    }

    public boolean saveConfig() { return ConfigManager.save(Minecraft.getInstance(), moduleManager); }

    private boolean saveConfigFromRuntime() {
        boolean saved = saveConfig();
        if (saved) {
            runtimeSaveFailureNotified = false;
        } else if (!runtimeSaveFailureNotified) {
            NotificationCenter.push("Arson", "Config save failed; use /arson save to retry.");
            runtimeSaveFailureNotified = true;
        }
        return saved;
    }

    private void runRuntimeSmoke(Minecraft client) {
        int limit = Integer.getInteger("arson.runtimeSmokeTicks", 0);
        if (limit <= 0) return;
        runtimeSmokeTick++;
        if (runtimeSmokeTick == 20) client.setScreen(new ArsonScreen(client.screen));
        if (runtimeSmokeTick == 50) client.setScreen(null);
        if (runtimeSmokeTick >= limit) client.stop();
    }

    static boolean consumeModuleKeyPress(Map<String, Boolean> keyStates, Module module, boolean down) {
        String id = module.id();
        boolean wasDown = keyStates.getOrDefault(id, false);
        keyStates.put(id, down);
        return down && !wasDown;
    }

    private void processModuleKeybinds(Minecraft client) {
        if (client.getWindow() == null || client.screen != null) return;
        long window = client.getWindow().handle();
        for (Module module : moduleManager.all()) {
            int keyCode = module.keyCode();
            if (keyCode <= 0) continue;
            boolean down = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
            if (consumeModuleKeyPress(moduleKeyStates, module, down)) {
                module.toggle();
                saveConfigFromRuntime();
                NotificationCenter.push(module.name(), module.enabled() ? "Enabled" : "Disabled");
            }
        }
    }
    public ModuleManager modules() { return moduleManager; }
    public WorldRenderBridge worldRenderBridge() { return worldRenderBridge; }
    public FeatureContext featureContext() { return featureContext; }
}
