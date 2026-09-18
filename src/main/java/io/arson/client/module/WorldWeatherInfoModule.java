package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Tracks live world weather and thunder state for HUD presentation. */
public final class WorldWeatherInfoModule extends Module {
 private boolean raining; private boolean thundering; private float rainStrength; private float thunderStrength;
 public WorldWeatherInfoModule(){super("world-weather-info","World Weather Info",Category.WORLD,"Tracks live rain, thunder, and weather intensity for HUD presentation without changing world state.");}
 @Override protected void onTick(Minecraft client){if(client.level==null){raining=thundering=false;rainStrength=thunderStrength=0;return;}raining=client.level.isRaining();thundering=client.level.isThundering();rainStrength=client.level.getRainLevel(1.0f);thunderStrength=client.level.getThunderLevel(1.0f);}
 public boolean raining(){return raining;} public boolean thundering(){return thundering;} public float rainStrength(){return rainStrength;} public float thunderStrength(){return thunderStrength;}
 public String formatted(){return String.format(java.util.Locale.ROOT,"Weather %s  Rain %.0f%%  Thunder %.0f%%",raining?"Rain":"Clear",rainStrength*100,thunderStrength*100)+(thundering?"  Storm":"");}
}