package io.arson.client.render;

import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageChestPairingTest {
    @Test
    void pairsComplementaryHalvesOfSameChestBlockAndFacing() {
        assertTrue(StorageChestPairing.canPair(true, ChestType.LEFT, ChestType.RIGHT,
                Direction.NORTH, Direction.NORTH));
    }

    @Test
    void rejectsSingleOrMismatchedChestHalves() {
        assertFalse(StorageChestPairing.canPair(true, ChestType.SINGLE, ChestType.LEFT,
                Direction.NORTH, Direction.NORTH));
        assertFalse(StorageChestPairing.canPair(true, ChestType.LEFT, ChestType.RIGHT,
                Direction.NORTH, Direction.SOUTH));
        assertFalse(StorageChestPairing.canPair(true, ChestType.LEFT, ChestType.LEFT,
                Direction.NORTH, Direction.NORTH));
    }

    @Test
    void doesNotMergeDifferentChestBlockTypes() {
        assertFalse(StorageChestPairing.canPair(false, ChestType.LEFT, ChestType.RIGHT,
                Direction.NORTH, Direction.NORTH));
    }
}
