package io.arson.client.mixin;

import net.minecraft.world.inventory.Slot;

public interface HandledScreenAccessor {
    Slot arson$getHoveredSlot();
}
