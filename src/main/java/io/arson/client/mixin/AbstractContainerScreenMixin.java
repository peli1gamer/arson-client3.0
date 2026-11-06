package io.arson.client.mixin;

import io.arson.client.accessor.HandledScreenAccessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HandledScreen.class)
public abstract class AbstractContainerScreenMixin implements HandledScreenAccessor {
    @Shadow
    protected Slot focusedSlot;

    @Override
    public Slot arson$getHoveredSlot() {
        return focusedSlot;
    }
}
