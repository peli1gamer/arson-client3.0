package io.arson.client.mixin;

import io.arson.client.ArsonClient;
import io.arson.client.module.TotemPopCounterModule;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observes vanilla entity-status packets so the Totem Pop Counter can track status 35. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void arson$handleEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (packet.getEventId() != 35) return;
        if (ArsonClient.getInstance() == null) return;
        if (ArsonClient.getInstance().modules() == null) return;
        if (ArsonClient.getInstance().modules().get("totem-pop-counter") instanceof TotemPopCounterModule module) {
            if (net.minecraft.client.Minecraft.getInstance().level == null) return;
            Entity entity = packet.getEntity(net.minecraft.client.Minecraft.getInstance().level);
            module.onTotemPop(entity);
        }
    }
}
