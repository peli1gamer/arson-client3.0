package io.arson.client.render;

import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageChestPairingTest {
    @Test
    void pairsComplementaryHalvesOfSameChestBlockAndFacing() {
        var left = Blocks.CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.LEFT)
                .with(ChestBlock.FACING, Direction.NORTH);
        var right = Blocks.CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.RIGHT)
                .with(ChestBlock.FACING, Direction.NORTH);

        assertTrue(StorageChestPairing.canPair(left, right));
    }

    @Test
    void rejectsSingleOrMismatchedChestHalves() {
        var single = Blocks.CHEST.getDefaultState();
        var left = Blocks.CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.LEFT)
                .with(ChestBlock.FACING, Direction.NORTH);
        var rightWrongFacing = Blocks.CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.RIGHT)
                .with(ChestBlock.FACING, Direction.SOUTH);

        assertFalse(StorageChestPairing.canPair(single, left));
        assertFalse(StorageChestPairing.canPair(left, rightWrongFacing));
    }

    @Test
    void doesNotMergeNormalAndTrappedChests() {
        var left = Blocks.CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.LEFT)
                .with(ChestBlock.FACING, Direction.NORTH);
        var trappedRight = Blocks.TRAPPED_CHEST.getDefaultState()
                .with(ChestBlock.CHEST_TYPE, ChestType.RIGHT)
                .with(ChestBlock.FACING, Direction.NORTH);

        assertFalse(StorageChestPairing.canPair(left, trappedRight));
    }
}
