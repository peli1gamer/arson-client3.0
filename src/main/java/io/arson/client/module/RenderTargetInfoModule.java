package io.arson.client.module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
/** Live client crosshair target telemetry; never changes the target or sends actions. */
public final class RenderTargetInfoModule extends Module {
 private String type="MISS"; private double distance;
 public RenderTargetInfoModule(){super("render-target-info","Render Target Info",Category.RENDER,"Reports the local crosshair hit type and distance.");}
 @Override protected void onTick(MinecraftClient client){if(client.crosshairTarget==null){type="MISS";distance=0;return;}HitResult hit=client.crosshairTarget;type=hit.getType().name();distance=client.player==null?0:Math.sqrt(client.player.squaredDistanceTo(hit.getPos()));}
 public String type(){return type;} public double distance(){return distance;} public String formatted(){return String.format(java.util.Locale.ROOT,"Target %s  %.2f m",type,distance);}
}