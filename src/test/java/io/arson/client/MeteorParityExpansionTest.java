package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HotkeysModule;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import io.arson.client.module.PerformanceModule;
import meteordevelopment.meteorclient.systems.Systems;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeteorParityExpansionTest {
    @Test void playerAndWorldTelemetryAreConcreteAndOrganized() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertNotNull(manager.get("player-coordinates"));
        assertNotNull(manager.get("world-clock"));
        assertTrue(manager.categoryCount(Module.Category.PLAYER) >= 3);
        assertTrue(manager.categoryCount(Module.Category.WORLD) >= 3);
        assertTrue(manager.get("player-coordinates").description().length() > 10);
        assertTrue(manager.get("world-clock").description().length() > 10);
    }

    @Test void hotkeysModuleListsBoundModulesInStableOrder() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        HotkeysModule hotkeys = (HotkeysModule) manager.get("hotkeys");
        manager.get("hud").setKeybind(Keybind.fromKey(72));
        manager.get("fps").setKeybind(Keybind.fromKey(70));
        manager.get("performance").setKeybind(Keybind.fromKey(80));

        assertEquals(java.util.List.of("FPS", "HUD", "Performance"),
                hotkeys.boundModules(manager.all()).stream().map(Module::name).toList());
        assertEquals(3, hotkeys.boundModuleCount(manager.all()));
    }

    @Test void favoritesSortAheadOfOrdinaryModules() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        Module hud = manager.get("hud");
        hud.setFavorite(true);
        assertSame(hud, manager.organized(Module.Category.RENDER).iterator().next());
        assertEquals(1, manager.favoriteCount());
    }

    @Test void hudAnchorsPreservePositionAcrossViewportChanges() {
        HudLayoutModule layout = new HudLayoutModule();
        layout.setAnchor("watermark", HudLayoutModule.Anchor.TOP_RIGHT);
        layout.setPositionPreservingAnchor("watermark", 900, 12, 1000, 600, 80, 12);
        double[] at1000 = layout.resolve("watermark", 1000, 600, 80, 12);
        double[] at1200 = layout.resolve("watermark", 1200, 600, 80, 12);
        assertEquals(900, at1000[0], 0.001);
        assertEquals(1100, at1200[0], 0.001);
        assertEquals(12, at1200[1], 0.001);
    }

    @Test void performanceModuleReportsRealChunkScanMetrics() {
        PerformanceModule performance = new PerformanceModule();

        assertEquals(1_000_000L, performance.scanBudgetNanos());
        assertTrue(performance.showStats());
        assertEquals("Chunk scan 1.25 ms · 3 processed · 7 queued",
                performance.formattedStats(1_250_000L, 3, 7));
        assertEquals("Chunk scan 0.00 ms · 0 processed · 0 queued",
                performance.formattedStats(-1L, -3, -7));
    }

    @Test void failedSystemInitializationRollsBackRegistryEntry() {
        assertThrows(IllegalStateException.class, () -> Systems.add(new FailingInitSystem()));
        assertNull(Systems.get(FailingInitSystem.class));
    }

    @Test void duplicateSystemRegistrationIsRejectedWithoutReplacingOriginal() {
        DuplicateSystem original = Systems.add(new DuplicateSystem());

        assertThrows(IllegalArgumentException.class, () -> Systems.add(new DuplicateSystem()));
        assertSame(original, Systems.get(DuplicateSystem.class));
    }

    private static final class FailingInitSystem extends meteordevelopment.meteorclient.systems.System<FailingInitSystem> {
        private FailingInitSystem() {
            super("test-failing-init");
        }

        @Override
        public void init() {
            throw new IllegalStateException("expected test failure");
        }
    }

    private static final class DuplicateSystem extends meteordevelopment.meteorclient.systems.System<DuplicateSystem> {
        private DuplicateSystem() {
            super("test-duplicate-registration");
        }
    }
}
