package io.arson.client.module;

import io.arson.client.settings.DoubleSetting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EntityInfoModuleTest {
    @Test
    void exposesTheSharedDistanceFadeStartControl() {
        EntityInfoModule module = new EntityInfoModule();
        DoubleSetting fadeStart = module.settings().stream()
                .filter(setting -> setting.id().equals("fade-start"))
                .map(setting -> assertInstanceOf(DoubleSetting.class, setting))
                .findFirst()
                .orElseThrow();

        assertEquals(0.45, module.fadeStart(), 0.0001);
        fadeStart.set(0.73);
        assertEquals(0.75, module.fadeStart(), 0.0001);
    }
}
