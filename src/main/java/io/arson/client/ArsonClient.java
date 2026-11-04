package io.arson.client;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import io.arson.client.command.ArsonCommand;
import io.arson.client.config.ArsonConfigSystem;
import io.arson.client.context.FeatureContext;
import io.arson.client.module.AimAssistModule;
import io.arson.client.module.BlockESPModule;
import io.arson.client.module.ContainerESPModule;
import io.arson.client.module.ChunkFinderModule;
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
import io.arson.client.render.ChunkFinderRenderStage;
import io.arson.client.render.ChunkFinderScanner;
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
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.Systems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public final class ArsonClient implements ClientModInitializer {
    public static final String MOD_ID = "arson";
    private static ArsonClient instance;
    private ModuleManager moduleManager;
    private KeyBinding openMenuKey;
    private WorldRenderBridge worldRenderBridge;
    private FeatureContext featureContext;
    private FeatureContextAdapter contextAdapter;
    private final Map<String, Boolean> moduleKeyStates = new HashMap<>();
    private int runtimeSmokeTick;
    public static ArsonClient getInstance() { return instance; }

    @Override public void onInitializeClient() {
        instance = this;
        MinecraftClient client = MinecraftClient.getInstance();
        MeteorClient.initialize(client);
        moduleManager = Systems.add(new ModuleManager());
        Systems.add(new ArsonConfigSystem(client, moduleManager));
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.arson.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        ArsonCommand.register();
        HudElementRegistry.addLast(Identifier.of(MOD_ID, "hud"), HudRenderer::render);
        HudElementRegistry.addLast(Identifier.of(MOD_ID, "array_list"), ArrayListRenderer::render);
        HudElementRegistry.addLast(Identifier.of(MOD_ID, "combat_info"), CombatInfoRenderer::render);

        featureContext = new FeatureContext();
        contextAdapter = new FabricFeatureContextAdapter(client);
        contextAdapter.initialize(featureContext);
        worldRenderBridge = new WorldRenderBridge(featureContext, contextAdapter);

        StorageOverlay storageOverlay = new StorageOverlay(new StorageRenderProfile());
        worldRenderBridge.register(new StorageRenderStage(client, storageOverlay, new StorageScanner(), (ContainerESPModule) moduleManager.get("container-esp")));
        EntityScanner entityScanner = new EntityScanner();
        worldRenderBridge.register(new EntityRenderStage(client, entityScanner, (EntityESPModule) moduleManager.get("entity-esp")));
        worldRenderBridge.register(new ItemRenderStage(client, entityScanner, (ItemESPModule) moduleManager.get("item-esp")));
        worldRenderBridge.register(new EntityInfoStage(client, entityScanner, (EntityInfoModule) moduleManager.get("entity-info")));
        worldRenderBridge.register(new EntityTracerStage(client, entityScanner, (EntityTracerModule) moduleManager.get("entity-tracers")));
        worldRenderBridge.register(new BlockRenderStage(client, new BlockScanner(), (BlockESPModule) moduleManager.get("block-esp")));
        worldRenderBridge.register(new ChunkFinderRenderStage(client, new ChunkFinderScanner(), (ChunkFinderModule) moduleManager.get("chunk-finder")));
        worldRenderBridge.register(new AimAssistRenderStage(client, (AimAssistModule) moduleManager.get("aim-assist")));
        worldRenderBridge.attach();

        ClientTickEvents.START_CLIENT_TICK.register(clientTick ->
            MeteorClient.EVENT_BUS.post(TickEvent.Pre.get()));
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            contextAdapter.tick(featureContext);
            while (openMenuKey.wasPressed()) {
                Screen current = clientTick.currentScreen;
                clientTick.setScreen(current instanceof ArsonScreen ? null : new ArsonScreen(current));
                NotificationCenter.push("Arson", current instanceof ArsonScreen ? "GUI closed" : "GUI opened");
            }
            processModuleKeybinds(clientTick);
            moduleManager.tick(clientTick);
            runRuntimeSmoke(clientTick);
            MeteorClient.EVENT_BUS.post(TickEvent.Post.get());
        });
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            if (clientTick.player != null && clientTick.world != null && clientTick.world.getTime() % 200 == 0) saveConfig();
        });
        if (client.player != null) client.player.sendMessage(Text.literal("Arson V3 initialized"), true);
    }

    public void saveConfig() { Systems.save(); }

    private void runRuntimeSmoke(MinecraftClient client) {
        int limit = Integer.getInteger("arson.runtimeSmokeTicks", 0);
        if (limit <= 0) return;
        runtimeSmokeTick++;
        if (runtimeSmokeTick == 20) client.setScreen(new ArsonScreen(client.currentScreen));
        if (runtimeSmokeTick == 50) client.setScreen(null);
        if (runtimeSmokeTick >= limit) client.scheduleStop();
    }

    static boolean consumeModuleKeyPress(Map<String, Boolean> keyStates, Module module, boolean down) {
        String id = module.id();
        boolean wasDown = keyStates.getOrDefault(id, false);
        keyStates.put(id, down);
        return down && !wasDown;
    }

    private void processModuleKeybinds(MinecraftClient client) {
        if (client.getWindow() == null || client.currentScreen != null) return;
        long window = client.getWindow().getHandle();
        for (Module module : moduleManager.all()) {
            if (!module.hasKeybind()) continue;
            boolean down = module.keybind().isPressed(window);
            if (consumeModuleKeyPress(moduleKeyStates, module, down)) {
                module.toggle();
                NotificationCenter.push(module.name(), module.enabled() ? "Enabled" : "Disabled");
            }
        }
    }
    public ModuleManager modules() { return moduleManager; }
    public WorldRenderBridge worldRenderBridge() { return worldRenderBridge; }
    public FeatureContext featureContext() { return featureContext; }
}
