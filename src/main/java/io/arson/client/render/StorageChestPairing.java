package io.arson.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;

/** Validates vanilla double-chest state before the overlay combines two block bounds. */
final class StorageChestPairing {
    private StorageChestPairing() {}

    static boolean canPair(BlockState first, BlockState second) {
        if (first == null || second == null || !(first.getBlock() instanceof ChestBlock)) return false;
        return canPair(first.getBlock() == second.getBlock(),
                first.get(ChestBlock.CHEST_TYPE), second.get(ChestBlock.CHEST_TYPE),
                first.get(ChestBlock.FACING), second.get(ChestBlock.FACING));
    }

    static boolean canPair(boolean sameBlock, ChestType firstType, ChestType secondType,
                           Direction firstFacing, Direction secondFacing) {
        return sameBlock && firstType != null && secondType != null
                && firstType != ChestType.SINGLE && secondType != ChestType.SINGLE
                && firstType != secondType
                && firstFacing != null && firstFacing == secondFacing;
    }
}
