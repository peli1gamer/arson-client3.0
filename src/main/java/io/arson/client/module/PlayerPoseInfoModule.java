package io.arson.client.module;
import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
/** Live local player pose and movement-state telemetry. */
public final class PlayerPoseInfoModule extends Module {
 private final BooleanSetting showVelocity=setting(new BooleanSetting("show-velocity","Show Velocity",true));
 private String pose="STANDING"; private boolean swimming,fallFlying,gliding; private boolean showVelocityState; private double speed;
 public PlayerPoseInfoModule(){super("player-pose-info","Player Pose Info",Category.PLAYER,"Reports the local player's live pose and movement state for HUD presentation.");}
 @Override protected void onTick(Minecraft client){if(client.player==null){pose="STANDING";swimming=fallFlying=gliding=false;speed=0;return;}var p=client.player;pose=p.getPose().name();swimming=p.isSwimming();fallFlying=p.isFallFlying();gliding=p.isFallFlying();var v=p.getDeltaMovement();speed=Math.sqrt(v.x*v.x+v.y*v.y+v.z*v.z);showVelocityState=showVelocity.enabled();}
 public String pose(){return pose;} public boolean swimming(){return swimming;} public boolean fallFlying(){return fallFlying;} public boolean gliding(){return gliding;} public double speed(){return speed;} public boolean showVelocity(){return showVelocityState;}
 public String formatted(){String out="Pose "+pose+(swimming?"  Swimming":"")+(fallFlying?"  Elytra":"");return showVelocity()?String.format(java.util.Locale.ROOT,"%s  Speed %.2f",out,speed):out;}
}