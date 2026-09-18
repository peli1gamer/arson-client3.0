package io.arson.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Tracks the local crosshair hit target for HUD and render diagnostics. */
public final class RenderTargetInfoModule extends Module {
    private String targetType = "none";
    private String targetDetails = "No target";

    public RenderTargetInfoModule() {
        super("render-target-info", "Render Target Info", Category.RENDER,
                "Reports the live crosshair target type and concise block/entity details for local HUD diagnostics.");
    }

    @Override
    protected void onTick(Minecraft client) {
        HitResult hit = client.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            targetType = "none";
            targetDetails = "No target";
            return;
        }
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
            targetType = "block";
            var pos = blockHit.getBlockPos();
            targetDetails = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        } else if (hit instanceof EntityHitResult entityHit) {
            targetType = "entity";
            targetDetails = entityHit.getEntity().getType().toString();
        } else {
            targetType = hit.getType().name().toLowerCase(java.util.Locale.ROOT);
            targetDetails = targetType;
        }
    }

    public String targetType() { return targetType; }
    public String targetDetails() { return targetDetails; }
    public String formatted() { return "Target " + targetType + "  " + targetDetails; }
}
