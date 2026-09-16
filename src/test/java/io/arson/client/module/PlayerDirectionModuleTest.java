package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerDirectionModuleTest {
    @Test
    void cardinalDirectionBucketsWrapAround() {
        assertEquals("South", PlayerDirectionModule.cardinal(0));
        assertEquals("West", PlayerDirectionModule.cardinal(90));
        assertEquals("North", PlayerDirectionModule.cardinal(180));
        assertEquals("East", PlayerDirectionModule.cardinal(270));
        assertEquals("South", PlayerDirectionModule.cardinal(360));
        assertEquals("South", PlayerDirectionModule.cardinal(-360));
        assertEquals("Southwest", PlayerDirectionModule.cardinal(45));
    }

    @Test
    void moduleProvidesUsefulDiscoveryMetadata() {
        PlayerDirectionModule module = new PlayerDirectionModule();
        assertEquals("player-direction", module.id());
        assertEquals(Module.Category.PLAYER, module.category());
        assertEquals("Player Direction [Player]: Tracks the local player's horizontal facing direction for HUD and addon integrations without changing rotation.", module.help());
    }
}
