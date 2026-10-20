package io.arson.client.api;

import io.arson.client.module.CombatInfoModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArsonApiSnapshotTest {
    @Test
    void settingSnapshotsExposeStableTypedCurrentValuesAndAreImmutable() {
        CombatInfoModule module = new CombatInfoModule();
        var settings = ArsonApi.settingSnapshots(module);

        var state = settings.stream().filter(value -> value.id().equals("show-target-state")).findFirst().orElseThrow();
        assertEquals("Show Target State", state.name());
        assertEquals("boolean", state.type());
        assertEquals("true", state.value());
        assertTrue(state.visible());

        var cooldown = settings.stream()
                .filter(value -> value.id().equals("cooldown-ready-threshold")).findFirst().orElseThrow();
        assertEquals("number", cooldown.type());
        assertEquals("0.9", cooldown.value());

        var color = settings.stream().filter(value -> value.id().equals("health-color")).findFirst().orElseThrow();
        assertEquals("color", color.type());
        assertEquals("#FFFF5555", color.value());

        assertThrows(UnsupportedOperationException.class, () -> settings.add(state));
        ((io.arson.client.settings.BooleanSetting) module.settings().stream()
                .filter(value -> value.id().equals("show-target-state")).findFirst().orElseThrow()).set(false);
        assertEquals("false", ArsonApi.settingSnapshots(module).stream()
                .filter(value -> value.id().equals("show-target-state")).findFirst().orElseThrow().value());
    }

    @Test
    void settingSnapshotHelpersHandleMissingModulesSafely() {
        assertTrue(ArsonApi.settingSnapshots((io.arson.client.module.Module) null).isEmpty());
        assertTrue(ArsonApi.settingSnapshots("missing-module").isEmpty());
    }
}
