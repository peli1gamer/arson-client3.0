package io.arson.client.mixin;

import io.arson.client.ArsonClient;
import io.arson.client.module.TotemPopCounterModule;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observes vanilla entity-status packets so the Totem Pop Counter can track status 35. */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "onEntityStatus", at = @At("HEAD"))
    private void arson$handleEntityEvent(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() != 35) return;
        if (ArsonClient.getInstance() == null) return;
        if (ArsonClient.getInstance().modules() == null) return;
        if (ArsonClient.getInstance().modules().get("totem-pop-counter") instanceof TotemPopCounterModule module) {
            if (net.minecraft.client.MinecraftClient.getInstance().world == null) return;
            Entity entity = packet.getEntity(net.minecraft.client.MinecraftClient.getInstance().world);
            module.onTotemPop(entity);
        }
    }
}
