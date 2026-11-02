package io.arson.client.module;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
public final class HoleTunnelStairsESPModule extends StructureESPModule {
 private final BooleanSetting holes=setting(new BooleanSetting("holes","Holes",true));
 private final BooleanSetting tunnels=setting(new BooleanSetting("tunnels","Tunnels",true));
 private final BooleanSetting diagonal=setting(new BooleanSetting("diagonal-tunnels","Diagonal Tunnels",true));
 private final BooleanSetting stairs=setting(new BooleanSetting("stairs","Staircases",true));
 private final DoubleSetting holeDepth=setting(new DoubleSetting("minimum-hole-depth","Minimum Hole Depth",4,2,16,1));
 private final DoubleSetting tunnelLength=setting(new DoubleSetting("minimum-tunnel-length","Minimum Tunnel Length",3,2,12,1));
 private final DoubleSetting stairLength=setting(new DoubleSetting("minimum-stair-length","Minimum Stair Length",3,2,12,1));
 public HoleTunnelStairsESPModule(){super("hole-tunnel-stairs-esp","Hole / Tunnel / Stairs ESP",Detector.HOLE_TUNNEL_STAIRS);}
 public boolean holes(){return holes.enabled();} public boolean tunnels(){return tunnels.enabled();} public boolean diagonalTunnels(){return diagonal.enabled();} public boolean stairs(){return stairs.enabled();}
 public int minimumHoleDepth(){return (int)Math.round(holeDepth.get());} public int minimumTunnelLength(){return (int)Math.round(tunnelLength.get());} public int minimumStairLength(){return (int)Math.round(stairLength.get());}
}