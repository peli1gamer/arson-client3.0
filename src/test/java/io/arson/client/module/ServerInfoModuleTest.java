package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerInfoModuleTest {
    @Test
    void unknownAndMeasuredLatencyAreFormattedClearly() {
        assertEquals("Ping —", ServerInfoModule.formatLatency(-1));
        assertEquals("Ping —", ServerInfoModule.formatLatency(-42));
        assertEquals("Ping 0 ms", ServerInfoModule.formatLatency(0));
        assertEquals("Ping 58 ms", ServerInfoModule.formatLatency(58));
    }

    @Test
    void moduleStartsWithSafeSingleplayerStateUntilLiveDataArrives() {
        ServerInfoModule module = new ServerInfoModule();
        assertEquals("Singleplayer", module.serverName());
        assertEquals("", module.address());
        assertEquals(-1, module.latency());
        assertEquals("Singleplayer", module.formatted());
    }
}
