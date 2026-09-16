package io.arson.client;

import io.arson.client.accessor.HandledScreenAccessor;
import io.arson.client.module.ModuleManager;
import io.arson.client.platform.FabricFeatureContextAdapter;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeSmokeContractTest {
    @Test
    void runtimeEntryPointExposesRequiredInitializationSurfaces() {
        assertEquals("arson", ArsonClient.MOD_ID);
        assertNotNull(ModuleManager.class);
        assertNotNull(FabricFeatureContextAdapter.class);
    }

    @Test
    void accessorContractsMustNotLiveInMixinOwnedPackage() throws Exception {
        assertFalse(HandledScreenAccessor.class.getPackageName().endsWith(".mixin"));
        try (InputStream stream = getClass().getResourceAsStream("/arson.mixins.json")) {
            assertNotNull(stream);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("AbstractContainerScreenMixin"));
            assertTrue(json.contains("ClientPacketListenerMixin"));
            assertFalse(json.contains("HandledScreenAccessor"));
        }
    }
}
