package io.arson.client;

import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.module.HudLayoutModule;
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
}
