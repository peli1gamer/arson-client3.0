package io.arson.client.module;
import net.minecraft.client.Minecraft;
/** Live local key-state telemetry for HUD and addon integrations. */
public final class PlayerInputInfoModule extends Module {
 private boolean forward,back,left,right,jump,sneak;
 public PlayerInputInfoModule(){super("player-input-info","Player Input Info",Category.PLAYER,"Reports the local movement-key state without changing input.");}
 @Override protected void onTick(Minecraft client){var o=client.options;forward=o.keyUp.isDown();back=o.keyDown.isDown();left=o.keyLeft.isDown();right=o.keyRight.isDown();jump=o.keyJump.isDown();sneak=o.keyShift.isDown();}
 public String formatted(){StringBuilder s=new StringBuilder("Input ");if(forward)s.append('W');if(back)s.append('S');if(left)s.append('A');if(right)s.append('D');if(jump)s.append(" Jump");if(sneak)s.append(" Sneak");return s.toString();}
 public boolean forward(){return forward;} public boolean back(){return back;} public boolean left(){return left;} public boolean right(){return right;} public boolean jump(){return jump;} public boolean sneak(){return sneak;}
}