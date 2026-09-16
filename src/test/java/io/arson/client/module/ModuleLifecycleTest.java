package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleLifecycleTest {
    @Test
    void enableDisableHooksAreIdempotentAndCleanupRunsOncePerTransition() {
        CountingModule module = new CountingModule();
        module.setEnabled(true);
        module.setEnabled(true);
        module.setEnabled(false);
        module.setEnabled(false);

        assertTrue(module.enabledWasObserved());
        assertFalse(module.enabled());
        assertEquals(1, module.enableCount);
        assertEquals(1, module.disableCount);
    }

    private static final class CountingModule extends Module {
        int enableCount;
        int disableCount;
        private boolean enabledWasObserved;

        private CountingModule() { super("test", "Test", Category.MISC); }

        @Override protected void onEnable() { enableCount++; enabledWasObserved = true; }
        @Override protected void onDisable() { disableCount++; }
        boolean enabledWasObserved() { return enabledWasObserved; }
    }
}
