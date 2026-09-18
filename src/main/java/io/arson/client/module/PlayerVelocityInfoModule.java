package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Live local velocity telemetry for HUD and addon integrations. */
public final class PlayerVelocityInfoModule extends Module {
 private double horizontal,vertical,total;
 public PlayerVelocityInfoModule(){super("player-velocity-info","Player Velocity Info",Category.PLAYER,"Reports the local player's live horizontal, vertical, and total velocity.");}
 @Override protected void onTick(Minecraft client){if(client.player==null){horizontal=vertical=total=0;return;}var v=client.player.getDeltaMovement();horizontal=Math.sqrt(v.x*v.x+v.z*v.z);vertical=v.y;total=Math.sqrt(v.x*v.x+v.y*v.y+v.z*v.z);}
 public double horizontal(){return horizontal;} public double vertical(){return vertical;} public double total(){return total;}
 public String formatted(){return String.format(java.util.Locale.ROOT,"Velocity H %.3f  V %.3f  T %.3f",horizontal,vertical,total);}
}