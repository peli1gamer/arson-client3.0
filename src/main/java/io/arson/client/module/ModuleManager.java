package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void register(Module module) {
        if (modules.putIfAbsent(module.id(), module) != null) {
            throw new IllegalArgumentException("Duplicate module: " + module.id());
        }
    }

    public void registerDefaults() {
        register(new SprintModule());
        register(new ContainerESPModule());
        register(new EntityESPModule());
        register(new ItemESPModule());
        register(new EntityInfoModule());
        register(new EntityTracerModule());
        register(new BlockESPModule());
        register(new HudModule());
        register(new HudLayoutModule());
        register(new ArrayListModule());
        register(new CombatInfoModule());
        register(new TargetingModule());
        register(new AimAssistModule());
        register(new HoverTotemModule());
        register(new AutoTotemModule());
        register(new FriendsModule());
        register(new TotemPopCounterModule());
        register(new AttributeSwapModule());
        register(new PlayerInfoModule());
        register(new PlayerVitalsModule());
        register(new PlayerEquipmentModule());
        register(new PlayerMovementInfoModule());
        register(new PlayerCoordinatesModule());
        register(new PlayerDirectionModule());
        register(new PlayerExperienceInfoModule());
        register(new PlayerPoseInfoModule());
        register(new PlayerEffectInfoModule());
        register(new PlayerVelocityInfoModule());
        register(new PlayerInputInfoModule());
        register(new PlayerInteractionInfoModule());
        register(new PlayerAirInfoModule());
        register(new PlayerDetectionModule());
        register(new WorldInfoModule());
        register(new WorldClockModule());
        register(new WorldEnvironmentModule());
        register(new WorldDetailsModule());
        register(new WorldPositionInfoModule());
        register(new WorldLightInfoModule());
        register(new WorldWeatherInfoModule());
        register(new WorldEntityCountInfoModule());
        register(new WorldMoonInfoModule());
        register(new WorldChunkInfoModule());
        register(new WorldHeightInfoModule());
        register(new WorldBorderInfoModule());
        register(new WorldSpawnInfoModule());
        register(new ChunkPositionModule());
        register(new FpsModule());
        register(new RenderInfoModule());
        register(new RenderDisplayInfoModule());
        register(new RenderCameraInfoModule());
        register(new RenderTargetInfoModule());
        register(new RenderViewportInfoModule());
        register(new RenderFrameInfoModule());
        register(new RenderResolutionInfoModule());
        register(new RenderGuiInfoModule());
        register(new CameraInfoModule());
        register(new ClientPerformanceInfoModule());
        register(new InventoryInfoModule());
        register(new ServerInfoModule());
        register(new WaypointInfoModule());
        register(new ClickGuiPreferencesModule());
        register(GameStateModule.create());
        register(ScreenInfoModule.create());
    }

    public Module get(String id) { return modules.get(id); }
    public Collection<Module> all() { return new ArrayList<>(modules.values()); }

    public Collection<Module> organized(Module.Category category) {
        ArrayList<Module> result = new ArrayList<>();
        for (Module module : modules.values()) {
            if (module.category() == category) result.add(module);
        }
        result.sort(Comparator.comparing(Module::favorite).reversed()
                .thenComparing(Comparator.comparing(Module::enabled).reversed())
                .thenComparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public Collection<Module> search(String query) {
        ArrayList<Module> result = new ArrayList<>();
        String normalized = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        for (Module module : modules.values()) {
            String haystack = (module.name() + " " + module.id() + " " + module.description())
                    .toLowerCase(java.util.Locale.ROOT);
            if (normalized.isEmpty() || haystack.contains(normalized)) result.add(module);
        }
        result.sort(Comparator.comparing(Module::favorite).reversed()
                .thenComparing(Comparator.comparing(Module::enabled).reversed())
                .thenComparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public int categoryCount(Module.Category category) {
        int count = 0;
        for (Module module : modules.values()) if (module.category() == category) count++;
        return count;
    }

    public int enabledCount(Module.Category category) {
        int count = 0;
        for (Module module : modules.values()) {
            if (module.category() == category && module.enabled()) count++;
        }
        return count;
    }

    public int favoriteCount() {
        int count = 0;
        for (Module module : modules.values()) if (module.favorite()) count++;
        return count;
    }

    public int enabledCount() {
        int count = 0;
        for (Module module : modules.values()) if (module.enabled()) count++;
        return count;
    }

    public void tick(Minecraft client) {
        for (Module module : modules.values()) module.tick(client);
    }

    public enum DisplayMode { COMPACT, DETAILED }
    public enum RenderMode { STANDARD, HIGH_CONTRAST, MINIMAL }

    private static final class SprintModule extends Module {
        private final BooleanSetting forwardOnly = setting(new BooleanSetting("forward-only", "Forward Only", true));

        private SprintModule() {
            super("sprint", "Sprint", Category.MOVEMENT,
                    "Local sprint state helper; it only affects the local player's sprint flag while enabled.");
        }

        @Override protected void onTick(Minecraft client) {
            if (client.player == null) return;
            if (forwardOnly.enabled() && !client.options.keyUp.isDown()) return;
            client.player.setSprinting(true);
        }
    }
}
