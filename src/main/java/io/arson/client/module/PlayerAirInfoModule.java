package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Tracks live local air, pose, and movement state for HUD presentation. */
public final class PlayerAirInfoModule extends Module {
 private int air; private int maxAir; private boolean swimming; private boolean fallFlying;
 public PlayerAirInfoModule(){super("player-air-info","Player Air Info",Category.PLAYER,"Tracks live air supply, swimming, and fall-flying state without changing player behavior.");}
 @Override protected void onTick(Minecraft client){if(client.player==null){air=maxAir=0;swimming=fallFlying=false;return;}air=client.player.getAirSupply();maxAir=client.player.getMaxAirSupply();swimming=client.player.isSwimming();fallFlying=client.player.isFallFlying();}
 public int air(){return air;} public int maxAir(){return maxAir;} public boolean swimming(){return swimming;} public boolean fallFlying(){return fallFlying;}
 public String formatted(){return "Air "+air+"/"+maxAir+(swimming?"  Swim":"")+(fallFlying?"  Glide":"");}
}