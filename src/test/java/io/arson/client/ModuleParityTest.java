package io.arson.client;

import io.arson.client.api.ArsonApi;
import io.arson.client.module.HudModule;
import io.arson.client.module.Module;
import io.arson.client.module.ModuleManager;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.StringSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleParityTest {
    @Test
    void moduleSurfaceHasMeteorStyleCoreCategoriesAndUsefulDiscovery() {
        ModuleManager manager = new ModuleManager();
        manager.registerDefaults();
        assertTrue(manager.categoryCount(Module.Category.COMBAT) > 0);
        assertTrue(manager.categoryCount(Module.Category.MOVEMENT) > 0);
        assertTrue(manager.categoryCount(Module.Category.RENDER) > 0);
        assertTrue(manager.categoryCount(Module.Category.PLAYER) > 0);
        assertTrue(manager.categoryCount(Module.Category.WORLD) > 0);
        assertTrue(manager.categoryCount(Module.Category.MISC) > 0);
        assertNotNull(manager.get("sprint")); assertNotNull(manager.get("hud")); assertNotNull(manager.get("entity-esp"));
    }

    @Test
    void expandedInformationSurfaceContainsConcreteTelemetryModules() {
        ModuleManager manager = new ModuleManager(); manager.registerDefaults();
        assertNotNull(manager.get("fps")); assertNotNull(manager.get("player-coordinates")); assertNotNull(manager.get("player-direction"));
        assertNotNull(manager.get("world-clock")); assertNotNull(manager.get("world-environment")); assertNotNull(manager.get("inventory-info"));
        assertTrue(manager.get("fps").description().contains("frames per second"));
        assertTrue(manager.get("world-environment").description().contains("dimension"));
    }

    @Test
    void addonApiExposesStableCategoryAndFavoriteViews() {
        assertNotNull(ArsonApi.modules());
        assertTrue(ArsonApi.modules(Module.Category.PLAYER).stream().anyMatch(m -> m.id().equals("inventory-info")));
        assertTrue(ArsonApi.favorites().stream().allMatch(Module::favorite));
        assertTrue(ArsonApi.module("inventory-info").isPresent());
    }

    @Test
    void moduleKeybindIsMutableAndResettableForGuiCustomization() {
        ModuleManager manager = new ModuleManager(); manager.registerDefaults(); Module sprint = manager.get("sprint");
        assertNotNull(sprint); assertFalse(sprint.hasKeybind()); sprint.setKeyCode(65); assertTrue(sprint.hasKeybind()); assertEquals(65, sprint.keyCode());
        sprint.resetToDefaults(); assertEquals(0, sprint.keyCode()); assertFalse(sprint.enabled());
    }

    @Test
    void organizedCategoriesAreStableAndPutEnabledModulesFirst() {
        ModuleManager manager = new ModuleManager(); manager.registerDefaults(); Module sprint = manager.get("sprint");
        assertNotNull(sprint); sprint.setEnabled(true); assertEquals("sprint", manager.organized(Module.Category.MOVEMENT).iterator().next().id());
    }

    @Test
    void favoritesSortBeforeEnabledAndCanBeCounted() {
        ModuleManager manager = new ModuleManager(); manager.registerDefaults(); Module sprint = manager.get("sprint"); Module performance = manager.get("performance");
        assertNotNull(sprint); assertNotNull(performance); sprint.setFavorite(true); performance.setFavorite(true); assertEquals(2, manager.favoriteCount());
        assertEquals("performance", manager.organized(Module.Category.MISC).iterator().next().id());
    }

    @Test
    void typedSettingsExposeDefaultsAndResetToThem() {
        ModuleManager manager = new ModuleManager(); manager.registerDefaults(); HudModule hud = (HudModule) manager.get("hud"); assertNotNull(hud);
        BooleanSetting background = (BooleanSetting) hud.settings().stream().filter(s -> s.id().equals("background")).findFirst().orElseThrow();
        DoubleSetting scale = (DoubleSetting) hud.settings().stream().filter(s -> s.id().equals("scale")).findFirst().orElseThrow();
        StringSetting text = (StringSetting) hud.settings().stream().filter(s -> s.id().equals("watermark-text")).findFirst().orElseThrow();
        ColorSetting color = (ColorSetting) hud.settings().stream().filter(s -> s.id().equals("watermark-color")).findFirst().orElseThrow();
        background.set(false); background.reset(); scale.set(1.75); scale.reset(); text.set("Changed"); text.reset(); color.set(0xFF123456); color.reset();
        assertEquals(background.defaultValue(), background.get()); assertEquals(scale.defaultValue(), scale.get()); assertEquals(text.defaultValue(), text.get()); assertEquals(color.defaultValue(), color.get());
    }
}
