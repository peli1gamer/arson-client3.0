package io.arson.client.module;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
/** Tracks live render frame timing and window state for diagnostics. */
public final class RenderFrameInfoModule extends Module {
 private int fps; private long frameTimeNanos; private boolean focused;
 public RenderFrameInfoModule(){super("render-frame-info","Render Frame Info",Category.RENDER,"Tracks live FPS, frame timing, and window focus for HUD and render diagnostics.");}
 @Override protected void onTick(Minecraft client){fps=client.getFps();focused=GLFW.glfwGetWindowAttrib(client.getWindow().getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;frameTimeNanos=fps>0?1_000_000_000L/fps:0;}
 public int fps(){return fps;} public long frameTimeNanos(){return frameTimeNanos;} public boolean focused(){return focused;}
 public String formatted(){return String.format(java.util.Locale.ROOT,"Frame %d FPS  %.2f ms  %s",fps,frameTimeNanos/1_000_000.0,fps>0?"Active":"Idle")+(focused?"  Focus":"  Unfocused");}
}