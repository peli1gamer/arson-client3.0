package io.arson.client;

import io.arson.client.module.ModuleManager;
import io.arson.client.platform.FabricFeatureContextAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeSmokeContractTest {
    @Test
    void runtimeEntryPointExposesRequiredInitializationSurfaces() {
        assertNotNull(ArsonClient.MOD_ID);
        assertEquals("arson", ArsonClient.MOD_ID);
        assertNotNull(ModuleManager.class);
        assertNotNull(FabricFeatureContextAdapter.class);
    }
}
