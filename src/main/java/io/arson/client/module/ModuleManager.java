package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.EnumSetting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();

    public void register(Module module) {
        if (modules.putIfAbsent(module.id(), module) != null) {
            throw new IllegalArgumentException("Duplicate module: " + module.id());
        }
    }

    public void registerDefaults() {
        register(new ClientInfoModule());
        register(new SprintModule());
        register(new PerformanceModule());
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
        register(new CombatStatusModule());
        register(new PlayerStatusModule());
        register(new MovementStatusModule());
        register(new RenderProfileModule());
        register(new WorldStatusModule());
        register(new StorageStatusModule());
        register(new UtilityStatusModule());
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

    /** Formats enabled player and combat status modules for the HUD. */
    public List<String> playerStatusRows() {
        List<String> rows = new ArrayList<>();
        Module combat = get("combat-status");
        if (combat instanceof CombatStatusModule status && status.enabled()) rows.add(status.formatted());
        Module player = get("player-status");
        if (player instanceof PlayerStatusModule status && status.enabled()) rows.add(status.formatted());
        Module movement = get("movement-status");
        if (movement instanceof MovementStatusModule status && status.enabled()) rows.add(status.formatted());
        return List.copyOf(rows);
    }

    /** Formats enabled client, world, render, and storage status modules for the HUD. */
    public List<String> worldStatusRows() {
        List<String> rows = new ArrayList<>();
        Module clientInfo = get("client-info");
        if (clientInfo instanceof ClientInfoModule status && status.enabled()) rows.add(status.formatted());
        Module performance = get("performance");
        if (performance instanceof PerformanceModule status && status.enabled()) rows.add(status.formatted());
        Module renderProfile = get("render-profile");
        if (renderProfile instanceof RenderProfileModule status && status.enabled()) rows.add(status.formatted());
        Module world = get("world-status");
        if (world instanceof WorldStatusModule status && status.enabled()) rows.add(status.formatted());
        Module storage = get("storage-status");
        if (storage instanceof StorageStatusModule status && status.enabled()) rows.add(status.formatted());
        Module utility = get("utility-status");
        if (utility instanceof UtilityStatusModule status && status.enabled()) rows.add(status.formatted());
        return List.copyOf(rows);
    }

    public enum DisplayMode { COMPACT, DETAILED }
    public enum RenderMode { STANDARD, HIGH_CONTRAST, MINIMAL }

    private static final class ClientInfoModule extends Module {
        private final BooleanSetting showVersion = setting(new BooleanSetting("show-version", "Show Version", true));

        private ClientInfoModule() {
            super("client-info", "Client Info", Category.MISC, "Shows Arson and Minecraft version information in the HUD.");
        }

        private String formatted() {
            String clientVersion = FabricLoader.getInstance().getModContainer("arson")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
            if (!showVersion.enabled()) return "Arson";
            return "Arson " + clientVersion + " • Minecraft " + SharedConstants.getCurrentVersion().name();
        }
    }

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

    private static final class PerformanceModule extends Module {
        private int fps;
        private long usedMemoryMb;
        private long maxMemoryMb;

        private PerformanceModule() {
            super("performance", "Performance", Category.MISC,
                    "Reports live FPS and JVM memory use in the HUD without changing runtime settings.");
        }

        @Override protected void onTick(Minecraft client) {
            fps = client.getFps();
            Runtime runtime = Runtime.getRuntime();
            usedMemoryMb = Math.max(0L, (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L));
            maxMemoryMb = Math.max(0L, runtime.maxMemory() / (1024L * 1024L));
        }

        private String formatted() {
            return "Performance " + fps + " FPS • Memory " + usedMemoryMb + "/" + maxMemoryMb + " MB";
        }
    }

    private static final class CombatStatusModule extends Module {
        private final EnumSetting<DisplayMode> mode = setting(new EnumSetting<>("mode", "Display Mode", DisplayMode.COMPACT));
        private final io.arson.client.settings.DoubleSetting threshold =
                setting(new io.arson.client.settings.DoubleSetting("ready-threshold", "Ready Threshold", 0.90, 0.0, 1.0, 0.05));
        private boolean ready;
        private float cooldown;

        private CombatStatusModule() {
            super("combat-status", "Combat Status", Category.COMBAT,
                    "Reports local attack-cooldown readiness without issuing attacks.");
            mode.description("Choose the amount of local combat state shown in the HUD.");
            threshold.description("Attack cooldown ratio considered ready.");
        }

        @Override protected void onTick(Minecraft client) {
            cooldown = client.player == null ? 0.0f : client.player.getAttackStrengthScale(0.0f);
            ready = cooldown >= threshold.get();
        }

        private String formatted() {
            if (mode.get() == DisplayMode.COMPACT) return "Combat " + (ready ? "Ready" : "Charging");
            return String.format(java.util.Locale.ROOT, "Combat %.0f%% • %s", cooldown * 100.0f, ready ? "Ready" : "Charging");
        }
    }

    private static final class PlayerStatusModule extends Module {
        private final EnumSetting<DisplayMode> mode = setting(new EnumSetting<>("mode", "Display Mode", DisplayMode.COMPACT));
        private int hunger;
        private float health;
        private float maxHealth;

        private PlayerStatusModule() {
            super("player-status", "Player Status", Category.PLAYER,
                    "Tracks local health and hunger for the HUD.");
            mode.description("Choose compact or detailed local player telemetry.");
        }

        @Override protected void onTick(Minecraft client) {
            if (client.player == null) {
                hunger = 0;
                health = maxHealth = 0;
                return;
            }
            hunger = client.player.getFoodData().getFoodLevel();
            health = client.player.getHealth();
            maxHealth = client.player.getMaxHealth();
        }

        private String formatted() {
            return mode.get() == DisplayMode.COMPACT
                    ? String.format(java.util.Locale.ROOT, "Status %.1f HP • Food %d", health, hunger)
                    : String.format(java.util.Locale.ROOT, "Health %.1f/%.1f • Hunger %d/20", health, maxHealth, hunger);
        }
    }

    private static final class MovementStatusModule extends Module {
        private final EnumSetting<DisplayMode> mode = setting(new EnumSetting<>("mode", "Display Mode", DisplayMode.COMPACT));
        private double horizontalSpeed;
        private boolean sprinting;
        private boolean sneaking;

        private MovementStatusModule() {
            super("movement-status", "Movement Status", Category.MOVEMENT,
                    "Reports local horizontal speed and movement state in the HUD.");
        }

        @Override protected void onTick(Minecraft client) {
            if (client.player == null) {
                horizontalSpeed = 0;
                sprinting = sneaking = false;
                return;
            }
            var velocity = client.player.getDeltaMovement();
            horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            sprinting = client.player.isSprinting();
            sneaking = client.player.isShiftKeyDown();
        }

        private String formatted() {
            if (mode.get() == DisplayMode.COMPACT) {
                return String.format(java.util.Locale.ROOT, "Speed %.2f", horizontalSpeed);
            }
            return String.format(java.util.Locale.ROOT, "Horizontal Speed %.2f • %s%s", horizontalSpeed,
                    sprinting ? "Sprinting" : "Walking", sneaking ? " • Sneaking" : "");
        }
    }

    private static final class RenderProfileModule extends Module {
        private final EnumSetting<RenderMode> mode = setting(new EnumSetting<>("mode", "Render Profile", RenderMode.STANDARD));

        private RenderProfileModule() {
            super("render-profile", "Render Profile", Category.RENDER,
                    "Selects a visual profile used by Storage ESP.");
            mode.description("Standard keeps configured styles, High Contrast strengthens fill and outlines, and Minimal draws outlines only.");
        }

        private RenderMode mode() { return mode.get(); }
        private String formatted() { return "Render Profile " + mode.get().name().replace('_', ' '); }
    }

    private static final class WorldStatusModule extends Module {
        private final EnumSetting<DisplayMode> mode = setting(new EnumSetting<>("mode", "Display Mode", DisplayMode.COMPACT));
        private long time;
        private boolean raining;

        private WorldStatusModule() {
            super("world-status", "World Status", Category.WORLD, "Reports local world time and weather in the HUD.");
        }

        @Override protected void onTick(Minecraft client) {
            if (client.level == null) {
                time = 0;
                raining = false;
                return;
            }
            time = client.level.getDayTime();
            raining = client.level.isRaining();
        }

        private String formatted() {
            if (mode.get() == DisplayMode.COMPACT) return "World " + (raining ? "Rain" : "Clear");
            long dayTime = Math.floorMod(time, 24000L);
            int hours = (int) ((dayTime / 1000L + 6L) % 24L);
            int minutes = (int) ((dayTime % 1000L) * 60L / 1000L);
            return String.format(java.util.Locale.ROOT, "World %02d:%02d • %s", hours, minutes, raining ? "Rain" : "Clear");
        }
    }

    private static final class StorageStatusModule extends Module {
        private final BooleanSetting showScreenState = setting(new BooleanSetting("show-screen-state", "Show Screen State", true));
        private boolean containerOpen;

        private StorageStatusModule() {
            super("storage-status", "Storage Status", Category.MISC,
                    "Reports whether a local container screen is currently open in the HUD.");
        }

        @Override protected void onTick(Minecraft client) {
            containerOpen = showScreenState.enabled()
                    && client.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>;
        }

        private String formatted() { return containerOpen ? "Container Open" : "No Container Open"; }
    }

    private final class UtilityStatusModule extends Module {
        private final EnumSetting<DisplayMode> mode = setting(new EnumSetting<>("mode", "Display Mode", DisplayMode.COMPACT));

        private UtilityStatusModule() {
            super("utility-status", "Utility Status", Category.MISC,
                    "Reports active module and favorite counts in the HUD.");
        }

        private String formatted() {
            if (mode.get() == DisplayMode.COMPACT) return "Modules " + enabledCount() + " Active";
            return "Modules " + enabledCount() + " Active • " + favoriteCount() + " Favorites";
        }
    }

    public RenderMode renderMode() {
        Module module = get("render-profile");
        return module instanceof RenderProfileModule profile && profile.enabled() ? profile.mode() : RenderMode.STANDARD;
    }
}
