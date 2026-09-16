package io.arson.client;

import io.arson.client.module.Module;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleKeybindTest {
    @Test
    void samePhysicalKeyTracksPressStateIndependentlyPerModule() {
        Module first = new TestModule("first");
        Module second = new TestModule("second");
        Map<String, Boolean> states = new HashMap<>();

        assertTrue(ArsonClient.consumeModuleKeyPress(states, first, true));
        assertTrue(ArsonClient.consumeModuleKeyPress(states, second, true));
        assertFalse(ArsonClient.consumeModuleKeyPress(states, first, true));
        assertFalse(ArsonClient.consumeModuleKeyPress(states, second, true));
        assertFalse(ArsonClient.consumeModuleKeyPress(states, first, false));
        assertFalse(ArsonClient.consumeModuleKeyPress(states, second, false));
        assertTrue(ArsonClient.consumeModuleKeyPress(states, first, true));
        assertTrue(ArsonClient.consumeModuleKeyPress(states, second, true));
    }

    private static final class TestModule extends Module {
        private TestModule(String id) { super(id, id, Category.MISC); }
    }
}
