package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContainerESPModuleTest {
    @Test
    void visibleStorageControlsMatchSettingsUsedByTheRenderStyles() {
        ContainerESPModule module = new ContainerESPModule();
        var chestStyle = module.chestStyle();
        assertTrue(chestStyle.fill());
        assertTrue(chestStyle.outline());

        ((BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("chest-fill"))
                .findFirst().orElseThrow()).set(false);
        ((BooleanSetting) module.settings().stream()
                .filter(setting -> setting.id().equals("chest-outline"))
                .findFirst().orElseThrow()).set(false);

        assertFalse(module.chestStyle().fill());
        assertFalse(module.chestStyle().outline());
    }

    @Test
    void doesNotExposeUnusedGenericVisualOrLabelPaddingControls() {
        ContainerESPModule module = new ContainerESPModule();
        assertFalse(module.settings().stream().anyMatch(setting ->
                setting.id().equals("fill") || setting.id().equals("outline")
                        || setting.id().equals("fill-alpha") || setting.id().equals("outline-alpha")
                        || setting.id().equals("line-width") || setting.id().equals("color")
                        || setting.id().equals("label-padding")));
    }
}
