package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Live dimension height telemetry without modifying the world. */
public final class WorldHeightInfoModule extends Module {
 private int minY,height,maxY;
 public WorldHeightInfoModule(){super("world-height-info","World Height Info",Category.WORLD,"Reports the live dimension minimum, height, and maximum build coordinates.");}
 @Override protected void onTick(Minecraft client){if(client.level==null){minY=height=maxY=0;return;}var type=client.level.dimensionType();minY=type.minY();height=type.height();maxY=minY+height-1;}
 public int minY(){return minY;} public int height(){return height;} public int maxY(){return maxY;}
 public String formatted(){return "Height "+minY+".."+maxY+" ("+height+" blocks)";}
}