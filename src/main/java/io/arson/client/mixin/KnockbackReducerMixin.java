package io.arson.client.mixin;

import io.arson.client.ArsonClient;
import io.arson.client.module.KnockbackReducerModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Scales incoming velocity only for the local player when Knockback Reducer is enabled. */
@Mixin(ClientPacketListener.class)
public abstract class KnockbackReducerMixin {
    @Redirect(
            method = "handleSetEntityMotion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void arson$reduceIncomingKnockback(Entity entity, Vec3 velocity, ClientboundSetEntityMotionPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (ArsonClient.getInstance() != null && ArsonClient.getInstance().modules() != null
                && ArsonClient.getInstance().modules().get("knockback-reducer") instanceof KnockbackReducerModule module
                && module.appliesToLocalPlayer(client, packet.getId())) {
            velocity = new Vec3(
                    velocity.x * module.horizontalScale(),
                    velocity.y * module.verticalScale(),
                    velocity.z * module.horizontalScale()
            );
        }
        entity.setDeltaMovement(velocity);
    }
}
