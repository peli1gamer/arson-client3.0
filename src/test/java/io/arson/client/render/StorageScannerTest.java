package io.arson.client.render;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageScannerTest {
    @Test
    void mergesOnlyOppositeHalvesOfSameFacingDoubleChest() {
        assertTrue(pair(ChestType.LEFT, ChestType.RIGHT, Direction.NORTH, Direction.NORTH, true, true));
        assertTrue(pair(ChestType.RIGHT, ChestType.LEFT, Direction.SOUTH, Direction.SOUTH, true, true));
    }

    @Test
    void doesNotMergeSingleChestsOrSameHalfTypes() {
        assertFalse(pair(ChestType.SINGLE, ChestType.LEFT, Direction.NORTH, Direction.NORTH, true, true));
        assertFalse(pair(ChestType.LEFT, ChestType.LEFT, Direction.NORTH, Direction.NORTH, true, true));
        assertFalse(pair(ChestType.RIGHT, ChestType.RIGHT, Direction.NORTH, Direction.NORTH, true, true));
    }

    @Test
    void doesNotMergeDifferentChestBlocksOrFacingDirections() {
        assertFalse(pair(ChestType.LEFT, ChestType.RIGHT, Direction.NORTH, Direction.NORTH, false, true));
        assertFalse(pair(ChestType.LEFT, ChestType.RIGHT, Direction.NORTH, Direction.EAST, true, true));
    }

    @Test
    void doesNotMergeChestsAdjacentAlongTheirFacingDirection() {
        assertFalse(pair(ChestType.LEFT, ChestType.RIGHT, Direction.NORTH, Direction.NORTH, true, false));
    }

    private static boolean pair(ChestType firstType, ChestType secondType,
                                Direction firstFacing, Direction secondFacing,
                                boolean sameBlock, boolean sideAdjacent) {
        return StorageScanner.areCompatibleChestHalves(firstType, secondType,
                firstFacing, secondFacing, sameBlock, sideAdjacent);
    }
}
