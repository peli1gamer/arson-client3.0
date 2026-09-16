package io.arson.client.accessor;

import net.minecraft.world.inventory.Slot;

/** Accessor contract implemented by the handled-screen mixin without living in a mixin-owned package. */
public interface HandledScreenAccessor {
    Slot arson$getHoveredSlot();
}
