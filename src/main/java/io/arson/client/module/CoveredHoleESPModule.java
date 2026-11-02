package io.arson.client.module;
import io.arson.client.settings.BooleanSetting;
public final class CoveredHoleESPModule extends StructureESPModule {
 private final BooleanSetting playerPlacedOnly=setting(new BooleanSetting("player-placed-only","Player-Placed Cover Only",true));
 public CoveredHoleESPModule(){super("covered-hole-esp","Covered Hole ESP",Detector.COVERED_HOLE);}
 public boolean playerPlacedOnly(){return playerPlacedOnly.enabled();}
}