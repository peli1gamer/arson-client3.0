package io.arson.client.module;
import net.minecraft.client.MinecraftClient;
/** Live local lunar-cycle telemetry derived from world time. */
public final class WorldMoonInfoModule extends Module {
 private int phase; private float angle;
 public WorldMoonInfoModule(){super("world-moon-info","World Moon Info",Category.WORLD,"Reports the live lunar phase and cycle position from local world time.");}
 @Override protected void onTick(MinecraftClient client){if(client.world==null){phase=0;angle=0;return;}long day=Math.floorMod(client.world.getTimeOfDay(),24000L);phase=(int)Math.floorMod(client.world.getTimeOfDay()/24000L,8L);angle=day/24000.0f;}
 public int phase(){return phase;} public float angle(){return angle;} public String formatted(){return String.format(java.util.Locale.ROOT,"Moon Phase %d/7  Cycle %.3f",phase,angle);}
}