package io.arson.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;

/** Validates vanilla double-chest state before the overlay combines two block bounds. */
final class StorageChestPairing {
    private StorageChestPairing() {}

    static boolean canPair(BlockState first, BlockState second) {
        if (first == null || second == null || !(first.getBlock() instanceof ChestBlock)
                || first.getBlock() != second.getBlock()) return false;
        ChestType firstType = first.get(ChestBlock.CHEST_TYPE);
        ChestType secondType = second.get(ChestBlock.CHEST_TYPE);
        return firstType != ChestType.SINGLE && secondType != ChestType.SINGLE
                && firstType != secondType
                && first.get(ChestBlock.FACING) == second.get(ChestBlock.FACING);
    }
}
