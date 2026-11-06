package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;

/** Arson's Meteor Modules registry adapter and default module catalogue. */
public final class ModuleManager extends meteordevelopment.meteorclient.systems.modules.Modules {
    @Override
    protected String key(meteordevelopment.meteorclient.systems.modules.Module module) {
        return ((Module) module).id();
    }

    public void register(Module module) {
        add(module);
    }

    @Override
    public void init() {
        registerDefaults();
    }

    public void registerDefaults(){
        register(new ClientInfoModule());register(new SprintModule());register(new PerformanceModule());
        register(new ContainerESPModule());register(new EntityESPModule());register(new ItemESPModule());register(new EntityInfoModule());register(new EntityTracerModule());register(new BlockESPModule());register(new ChunkFinderModule());
        register(new HudModule());register(new HudLayoutModule());register(new ArrayListModule());register(new CombatInfoModule());register(new TargetingModule());register(new AimAssistModule());register(new HoverTotemModule());register(new AutoTotemModule());register(new FriendsModule());register(new TotemPopCounterModule());register(new AttributeSwapModule());register(new PlayerInfoModule());register(new PlayerVitalsModule());register(new PlayerEquipmentModule());register(new PlayerMovementInfoModule());register(new PlayerCoordinatesModule());register(new PlayerDirectionModule());register(new PlayerExperienceInfoModule());register(new PlayerPoseInfoModule());register(new PlayerEffectInfoModule());register(new PlayerVelocityInfoModule());register(new PlayerInputInfoModule());register(new PlayerInteractionInfoModule());register(new PlayerAirInfoModule());register(new PlayerDetectionModule());register(new WorldInfoModule());register(new WorldClockModule());register(new WorldEnvironmentModule());register(new WorldDetailsModule());register(new WorldPositionInfoModule());register(new WorldLightInfoModule());register(new WorldWeatherInfoModule());register(new WorldEntityCountInfoModule());register(new WorldMoonInfoModule());register(new WorldChunkInfoModule());register(new WorldHeightInfoModule());register(new WorldBorderInfoModule());register(new WorldSpawnInfoModule());register(new ChunkPositionModule());register(new FpsModule());register(new RenderInfoModule());register(new RenderDisplayInfoModule());register(new RenderCameraInfoModule());register(new RenderTargetInfoModule());register(new RenderViewportInfoModule());register(new RenderFrameInfoModule());register(new RenderResolutionInfoModule());register(new RenderGuiInfoModule());register(new CameraInfoModule());register(new ClientPerformanceInfoModule());register(new InventoryInfoModule());register(new ServerInfoModule());
        register(new CombatStatusModule());register(new PlayerStatusModule());register(new MovementStatusModule());register(new RenderProfileModule());register(new WorldStatusModule());register(new StorageStatusModule());register(new UtilityStatusModule());register(new ClickGuiPreferencesModule());
        register(GameStateModule.create());register(ScreenInfoModule.create());
    }


    @Override
    public Module get(String id) {
        return (Module) super.get(id);
    }

    public Collection<Module> all() {
        return arsonModules();
    }

    private ArrayList<Module> arsonModules() {
        ArrayList<Module> result = new ArrayList<>();
        for (meteordevelopment.meteorclient.systems.modules.Module module : getAll()) {
            if (module instanceof Module arsonModule) result.add(arsonModule);
        }
        return result;
    }

    public Collection<Module> organized(Module.Category category) {
        ArrayList<Module> result = new ArrayList<>();
        for (meteordevelopment.meteorclient.systems.modules.Module module : getGroup(meteorCategory(category))) {
            if (module instanceof Module arsonModule) result.add(arsonModule);
        }
        sort(result);
        return result;
    }

    public Collection<Module> search(String query) {
        ArrayList<Module> result = new ArrayList<>();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (Module module : arsonModules()) {
            String haystack = (module.name() + " " + module.id() + " " + module.description()).toLowerCase(Locale.ROOT);
            if (q.isEmpty() || haystack.contains(q)) result.add(module);
        }
        sort(result);
        return result;
    }

    public int categoryCount(Module.Category category) {
        return getGroup(meteorCategory(category)).size();
    }

    public int enabledCount(Module.Category category) {
        int count = 0;
        for (meteordevelopment.meteorclient.systems.modules.Module module : getGroup(meteorCategory(category))) {
            if (module.isActive()) count++;
        }
        return count;
    }

    public int favoriteCount() {
        int count = 0;
        for (Module module : arsonModules()) if (module.favorite()) count++;
        return count;
    }

    public int enabledCount() {
        int count = 0;
        for (Module module : arsonModules()) if (module.enabled()) count++;
        return count;
    }

    public void tick(MinecraftClient client) {
        for (Module module : arsonModules()) module.tick(client);
    }

    /** Returns the optional Chunk Finder work budget; disabled means no additional time cap. */
    public long chunkScanBudgetNanos() {
        Module module = get("performance");
        if (module instanceof PerformanceModule performance && performance.enabled()) {
            return performance.scanBudgetNanos();
        }
        return Long.MAX_VALUE;
    }

    private static meteordevelopment.meteorclient.systems.modules.Category meteorCategory(Module.Category category) {
        return switch (category) {
            case COMBAT -> meteordevelopment.meteorclient.systems.modules.Categories.Combat;
            case MOVEMENT -> meteordevelopment.meteorclient.systems.modules.Categories.Movement;
            case RENDER -> meteordevelopment.meteorclient.systems.modules.Categories.Render;
            case PLAYER -> meteordevelopment.meteorclient.systems.modules.Categories.Player;
            case WORLD -> meteordevelopment.meteorclient.systems.modules.Categories.World;
            case MISC -> meteordevelopment.meteorclient.systems.modules.Categories.Misc;
        };
    }

    private static void sort(ArrayList<Module> modules) {
        modules.sort(Comparator.comparing(Module::favorite).reversed()
            .thenComparing(Comparator.comparing(Module::enabled).reversed())
            .thenComparing(Module::name, String.CASE_INSENSITIVE_ORDER));
    }

    public enum DisplayMode{COMPACT,DETAILED} public enum RenderMode{STANDARD,HIGH_CONTRAST,MINIMAL}
    private static final class ClientInfoModule extends Module{private ClientInfoModule(){super("client-info","Client Info",Category.MISC,"Shows client/version metadata used by the local HUD.");setting(new BooleanSetting("show-version","Show Version",true));}}
    private static final class SprintModule extends Module{private final BooleanSetting forwardOnly=setting(new BooleanSetting("forward-only","Forward Only",true));private SprintModule(){super("sprint","Sprint",Category.MOVEMENT,"Local sprint state helper; it only affects the local player's sprint flag while enabled.");}@Override protected void onTick(MinecraftClient client){if(client.player==null)return;if(forwardOnly.enabled()&&!client.options.forwardKey.isPressed())return;client.player.setSprinting(true);}}
    private static final class CombatStatusModule extends Module{private final EnumSetting<DisplayMode> mode=setting(new EnumSetting<>("mode","Display Mode",DisplayMode.COMPACT));private final DoubleSetting threshold=setting(new DoubleSetting("ready-threshold","Ready Threshold",0.90,0.0,1.0,0.05));private boolean ready;private CombatStatusModule(){super("combat-status","Combat Status",Category.COMBAT,"Reports local attack-cooldown readiness without issuing attacks.");mode.description("Choose the amount of local combat state shown by integrations.");threshold.description("Attack cooldown ratio considered ready.");}@Override protected void onTick(MinecraftClient client){ready=client.player!=null&&client.player.getAttackCooldownProgress(0.0f)>=threshold.get();}public boolean ready(){return ready;}public DisplayMode mode(){return mode.get();}}
    private static final class PlayerStatusModule extends Module{private final EnumSetting<DisplayMode> mode=setting(new EnumSetting<>("mode","Display Mode",DisplayMode.COMPACT));private int hunger;private float health;private PlayerStatusModule(){super("player-status","Player Status",Category.PLAYER,"Tracks local health and hunger for HUD presentation.");mode.description("Choose compact or detailed local player telemetry.");}@Override protected void onTick(MinecraftClient client){if(client.player==null)return;hunger=client.player.getHungerManager().getFoodLevel();health=client.player.getHealth();}public int hunger(){return hunger;}public float health(){return health;}public DisplayMode mode(){return mode.get();}}
    private static final class MovementStatusModule extends Module{private final EnumSetting<DisplayMode> mode=setting(new EnumSetting<>("mode","Display Mode",DisplayMode.COMPACT));private double horizontalSpeed;private MovementStatusModule(){super("movement-status","Movement Status",Category.MOVEMENT,"Reports local horizontal movement speed for HUDs and diagnostics.");}@Override protected void onTick(MinecraftClient client){if(client.player==null){horizontalSpeed=0;return;}var v=client.player.getVelocity();horizontalSpeed=Math.sqrt(v.x*v.x+v.z*v.z);}public double horizontalSpeed(){return horizontalSpeed;}}
    private static final class RenderProfileModule extends Module{private final EnumSetting<RenderMode> mode=setting(new EnumSetting<>("mode","Render Profile",RenderMode.STANDARD));private RenderProfileModule(){super("render-profile","Render Profile",Category.RENDER,"Selects a local presentation profile consumed by visual integrations.");mode.description("Select a local visual profile; integrations may consume it.");}public RenderMode mode(){return mode.get();}}
    private static final class WorldStatusModule extends Module{private final EnumSetting<DisplayMode> mode=setting(new EnumSetting<>("mode","Display Mode",DisplayMode.COMPACT));private long time;private boolean raining;private WorldStatusModule(){super("world-status","World Status",Category.WORLD,"Reports local world time and weather state.");}@Override protected void onTick(MinecraftClient client){if(client.world==null){time=0;raining=false;return;}time=client.world.getTimeOfDay();raining=client.world.isRaining();}public long time(){return time;}public boolean raining(){return raining;}}
    private static final class StorageStatusModule extends Module{private final BooleanSetting showScreenState=setting(new BooleanSetting("show-screen-state","Show Screen State",true));private boolean containerOpen;private StorageStatusModule(){super("storage-status","Storage Status",Category.MISC,"Reports whether a local container screen is currently open.");showScreenState.description("Track whether a local container screen is open.");}@Override protected void onTick(MinecraftClient client){containerOpen=showScreenState.enabled()&&client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?>;}public boolean containerOpen(){return containerOpen;}}
    private static final class UtilityStatusModule extends Module{private final EnumSetting<DisplayMode> mode=setting(new EnumSetting<>("mode","Display Mode",DisplayMode.COMPACT));private UtilityStatusModule(){super("utility-status","Utility Status",Category.MISC,"Local utility telemetry for compact HUD presentation.");}public DisplayMode mode(){return mode.get();}}
}


