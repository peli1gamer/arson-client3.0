package io.arson.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageScannerTest {
    @Test
    void mergesOnlyOppositeHalvesOfSameFacingDoubleChest() {
        BlockPos firstPos = new BlockPos(0, 64, 0);
        Direction facing = Direction.NORTH;
        BlockPos partnerPos = firstPos.relative(facing.getClockWise());
        BlockState left = chest(ChestType.LEFT, facing);
        BlockState right = chest(ChestType.RIGHT, facing);

        assertTrue(StorageScanner.areCompatibleChestStates(left, right, firstPos, partnerPos));
        assertTrue(StorageScanner.areCompatibleChestStates(right, left, partnerPos, firstPos));
    }

    @Test
    void doesNotMergeSingleChestsOrSameHalfTypes() {
        BlockPos firstPos = new BlockPos(0, 64, 0);
        BlockPos partnerPos = firstPos.east();
        BlockState single = chest(ChestType.SINGLE, Direction.NORTH);
        BlockState left = chest(ChestType.LEFT, Direction.NORTH);

        assertFalse(StorageScanner.areCompatibleChestStates(single, left, firstPos, partnerPos));
        assertFalse(StorageScanner.areCompatibleChestStates(left, left, firstPos, partnerPos));
    }

    @Test
    void doesNotMergeChestsWithDifferentFacingOrDifferentChestBlocks() {
        BlockPos firstPos = new BlockPos(0, 64, 0);
        BlockPos partnerPos = firstPos.east();
        BlockState northLeft = chest(ChestType.LEFT, Direction.NORTH);
        BlockState eastRight = chest(ChestType.RIGHT, Direction.EAST);
        BlockState trappedRight = Blocks.TRAPPED_CHEST.defaultBlockState()
                .setValue(ChestBlock.TYPE, ChestType.RIGHT)
                .setValue(ChestBlock.FACING, Direction.NORTH);

        assertFalse(StorageScanner.areCompatibleChestStates(northLeft, eastRight, firstPos, partnerPos));
        assertFalse(StorageScanner.areCompatibleChestStates(northLeft, trappedRight, firstPos, partnerPos));
    }

    @Test
    void doesNotMergeChestBlocksAdjacentAlongTheirFacingDirection() {
        BlockPos firstPos = new BlockPos(0, 64, 0);
        BlockPos partnerPos = firstPos.north();
        BlockState left = chest(ChestType.LEFT, Direction.NORTH);
        BlockState right = chest(ChestType.RIGHT, Direction.NORTH);

        assertFalse(StorageScanner.areCompatibleChestStates(left, right, firstPos, partnerPos));
    }

    private static BlockState chest(ChestType type, Direction facing) {
        return Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.TYPE, type)
                .setValue(ChestBlock.FACING, facing);
    }
}
