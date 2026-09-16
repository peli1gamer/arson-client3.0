package io.arson.client.render;

import io.arson.client.module.EntityInfoModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class EntityInfoStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client; private final EntityScanner scanner; private final EntityInfoModule module; private List<EntityTarget> cachedTargets=List.of(); private long lastScanTick=Long.MIN_VALUE;
    public EntityInfoStage(Minecraft client,EntityScanner scanner,EntityInfoModule module){this.client=client;this.scanner=scanner;this.module=module;}
    @Override public void render(WorldRenderContext context){if(!module.enabled()||client.level==null||client.player==null)return;long gameTime=client.level.getGameTime();if(gameTime-lastScanTick>=module.scanInterval()||gameTime<lastScanTick){cachedTargets=scanner.scan(client,module);lastScanTick=gameTime;}if(cachedTargets.isEmpty())return;var camera=context.worldState().cameraRenderState;PoseStack matrices=context.matrices();MultiBufferSource consumers=context.consumers();Font font=client.font;float scale=(float)module.scale();for(EntityTarget target:cachedTargets){float fade=distanceFade(target);if(fade<=0)continue;String text=buildText(target);if(text.isEmpty())continue;double x=target.centerX()-camera.pos.x,y=target.maxY()-camera.pos.y+module.heightOffset(),z=target.centerZ()-camera.pos.z;matrices.pushPose();matrices.translate(x,y,z);matrices.mulPose(camera.orientation);matrices.scale(-.025f*scale,-.025f*scale,.025f*scale);int width=font.width(text),left=-width/2,textColor=fadeColor(module.textColorFor(target.type()),fade);Matrix4f pose=matrices.last().pose();font.drawInBatch(Component.literal(text),left,0,textColor,false,pose,consumers,Font.DisplayMode.NORMAL,0,0xF000F0);matrices.popPose();}}
    private String buildText(EntityTarget target){StringBuilder text=new StringBuilder();if(module.showName())text.append(target.displayName());if(module.showDistance())append(text,"["+Math.round(target.distance())+"m]");if(module.showHealth()&&target.maxHealth()>0)append(text,"HP "+Math.round(Math.max(0,Math.min(1,target.health()/target.maxHealth()))*100)+"%");return text.toString();}
    private static void append(StringBuilder s,String v){if(!s.isEmpty())s.append(' ');s.append(v);} private float distanceFade(EntityTarget target){if(!module.distanceFade())return 1;double n=Math.max(0,Math.min(1,target.distance()/Math.max(1,module.range())));return(float)Math.max(0,Math.min(1,1-Math.max(0,n-.45)/.55));} private static int fadeColor(int argb,float fade){int alpha=Math.round(((argb>>>24)&255)*fade);return(argb&0x00FFFFFF)|(alpha<<24);}
}
