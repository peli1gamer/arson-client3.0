package io.arson.client.render;

import io.arson.client.module.EntityInfoModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

/** Renders configurable name, distance and health labels above entity targets. */
@Environment(EnvType.CLIENT)
public final class EntityInfoStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client; private final EntityScanner scanner; private final EntityInfoModule module; private List<EntityTarget> cachedTargets=List.of(); private long lastScanTick=Long.MIN_VALUE;
    public EntityInfoStage(Minecraft client,EntityScanner scanner,EntityInfoModule module){this.client=client;this.scanner=scanner;this.module=module;}
    @Override public void render(WorldRenderContext context){
        if(!module.enabled()||client.level==null||client.player==null)return; long gameTime=client.level.getGameTime(); if(gameTime-lastScanTick>=module.scanInterval()||gameTime<lastScanTick){cachedTargets=scanner.scan(client,module);lastScanTick=gameTime;} if(cachedTargets.isEmpty())return;
        var camera=context.worldState().cameraRenderState; PoseStack matrices=context.matrices(); MultiBufferSource consumers=context.consumers(); Font font=client.font; float scale=(float)module.scale();
        for(EntityTarget target:cachedTargets){float fade=distanceFade(target);if(fade<=0)continue;String text=buildText(target);if(text.isEmpty())continue;double x=target.centerX()-camera.pos.x,y=target.maxY()-camera.pos.y+module.heightOffset(),z=target.centerZ()-camera.pos.z;matrices.pushPose();matrices.translate(x,y,z);matrices.mulPose(camera.orientation);matrices.scale(-.025f*scale,-.025f*scale,.025f*scale);int width=font.width(text),left=-width/2,textColor=fadeColor(module.textColorFor(target.type()),fade);if(module.background()){float padding=(float)module.backgroundPadding();fillQuad(matrices,consumers,left-padding,-2,left+width+padding,9,fadeColor(module.backgroundColor(),fade));}Matrix4f pose=matrices.last().pose();font.drawInBatch(Component.literal(text),left,0,textColor,false,pose,consumers,Font.DisplayMode.NORMAL,0,0xF000F0);matrices.popPose();}
    }
    private String buildText(EntityTarget target){StringBuilder text=new StringBuilder();if(module.showName())text.append(target.displayName());if(module.showDistance()){appendSeparator(text);text.append("[").append(Math.round(target.distance())).append("m]");}if(module.showHealth()&&target.maxHealth()>0){appendSeparator(text);int percent=Math.round(Math.max(0,Math.min(1,target.health()/target.maxHealth()))*100);text.append("HP ").append(percent).append('%');}return text.toString();}
    private static void appendSeparator(StringBuilder text){if(!text.isEmpty())text.append(' ');}
    private float distanceFade(EntityTarget target){if(!module.distanceFade())return 1;double range=Math.max(1,module.range()),n=Math.max(0,Math.min(1,target.distance()/range));return(float)Math.max(0,Math.min(1,1-Math.max(0,n-.45)/.55));}
    private static int fadeColor(int argb,float fade){int alpha=Math.round(((argb>>>24)&255)*fade);return(argb&0x00FFFFFF)|(alpha<<24);}
    private static void fillQuad(PoseStack matrices,MultiBufferSource consumers,float x1,float y1,float x2,float y2,int argb){var pose=matrices.last();var buffer=consumers.getBuffer(RenderType.guiOverlay());float a=((argb>>>24)&255)/255f,r=((argb>>>16)&255)/255f,g=((argb>>>8)&255)/255f,b=(argb&255)/255f;buffer.addVertex(pose,x1,y1,0).setColor(r,g,b,a);buffer.addVertex(pose,x1,y2,0).setColor(r,g,b,a);buffer.addVertex(pose,x2,y2,0).setColor(r,g,b,a);buffer.addVertex(pose,x2,y1,0).setColor(r,g,b,a);}
}
