package io.arson.client.mixin;

import io.arson.client.accessor.FoodDataAccessor;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataAccessor {
    @Shadow @Final private float exhaustionLevel;

    @Override
    public float arson$getExhaustionLevel() {
        return exhaustionLevel;
    }
}
