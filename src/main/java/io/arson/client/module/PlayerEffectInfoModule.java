package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Live local status-effect telemetry for HUD presentation. */
public final class PlayerEffectInfoModule extends Module {
 private int count; private String summary="Effects 0";
 public PlayerEffectInfoModule(){super("player-effect-info","Player Effect Info",Category.PLAYER,"Reports the local player's active status effects and remaining durations.");}
 @Override protected void onTick(Minecraft client){if(client.player==null){count=0;summary="Effects 0";return;}var effects=client.player.getActiveEffects();count=effects.size();if(count==0){summary="Effects 0";return;}var first=effects.iterator().next();var name=first.getEffect().value().getDisplayName().getString();int seconds=Math.max(0,first.getDuration()/20);summary="Effects "+count+"  "+name+" "+seconds+"s";}
 public int count(){return count;} public String summary(){return summary;} public String formatted(){return summary;}
}