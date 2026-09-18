package io.arson.client.module;
import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
/** Live local render camera distance and view state telemetry. */
public final class RenderCameraInfoModule extends Module {
 private final BooleanSetting showRotation=setting(new BooleanSetting("show-rotation","Show Rotation",true));
 private double distance; private String type="FIRST_PERSON"; private float yaw,pitch;
 public RenderCameraInfoModule(){super("render-camera-info","Render Camera Info",Category.RENDER,"Reports live camera perspective and local viewpoint distance without changing render state.");}
 @Override protected void onTick(Minecraft client){if(client.player==null){distance=0;type="FIRST_PERSON";yaw=pitch=0;return;}var p=client.player;type=client.options.getCameraType().name();yaw=p.getYRot();pitch=p.getXRot();var pos=p.position();var eye=p.getEyePosition();double dx=pos.x-eye.x,dy=pos.y-eye.y,dz=pos.z-eye.z;distance=Math.sqrt(dx*dx+dy*dy+dz*dz);}
 public double distance(){return distance;} public String type(){return type;} public float yaw(){return yaw;} public float pitch(){return pitch;} public boolean showRotation(){return showRotation.enabled();}
 public String formatted(){return showRotation()?String.format(java.util.Locale.ROOT,"Camera %s  Eye %.2f  Yaw %.1f  Pitch %.1f",type,distance,yaw,pitch):String.format(java.util.Locale.ROOT,"Camera %s  Eye %.2f",type,distance);}
}