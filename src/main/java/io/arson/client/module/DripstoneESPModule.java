package io.arson.client.module;
import io.arson.client.settings.DoubleSetting;
public final class DripstoneESPModule extends StructureESPModule {
 private final DoubleSetting stalactite=setting(new DoubleSetting("minimum-stalactite-length","Minimum Stalactite Length",4,1,16,1));
 private final DoubleSetting stalagmite=setting(new DoubleSetting("minimum-stalagmite-length","Minimum Stalagmite Length",8,1,24,1));
 public DripstoneESPModule(){super("dripstone-esp","Dripstone ESP",Detector.DRIPSTONE);}
 public int minimumStalactiteLength(){return (int)Math.round(stalactite.get());} public int minimumStalagmiteLength(){return (int)Math.round(stalagmite.get());}
}