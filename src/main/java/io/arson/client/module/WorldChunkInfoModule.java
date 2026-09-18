package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Live local chunk and section telemetry. */
public final class WorldChunkInfoModule extends Module {
 private int chunkX,chunkZ,sectionY; private long inhabitedTime;
 public WorldChunkInfoModule(){super("world-chunk-info","World Chunk Info",Category.WORLD,"Reports the local player's live chunk coordinates, section, and inhabited time.");}
 @Override protected void onTick(Minecraft client){if(client.level==null||client.player==null){chunkX=chunkZ=sectionY=0;inhabitedTime=0;return;}var p=client.player;var pos=p.blockPosition();chunkX=pos.getX()>>4;chunkZ=pos.getZ()>>4;sectionY=pos.getY()>>4;inhabitedTime=client.level.getChunk(chunkX,chunkZ).getInhabitedTime();}
 public int chunkX(){return chunkX;} public int chunkZ(){return chunkZ;} public int sectionY(){return sectionY;} public long inhabitedTime(){return inhabitedTime;}
 public String formatted(){return "Chunk "+chunkX+", "+chunkZ+"  Section "+sectionY+"  Inhabited "+inhabitedTime;}
}