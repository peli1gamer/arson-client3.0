package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.ItemESPModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ItemRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client; private final EntityScanner scanner; private final ItemESPModule module; private List<EntityTarget> cachedTargets=List.of(); private long lastScanTick=Long.MIN_VALUE;
    public ItemRenderStage(Minecraft client,EntityScanner scanner,ItemESPModule module){this.client=client;this.scanner=scanner;this.module=module;}
    @Override public void render(WorldRenderContext context){if(!module.enabled()||client.level==null||client.player==null)return;long gameTime=client.level.getGameTime();if(gameTime-lastScanTick>=module.scanInterval()||gameTime<lastScanTick){cachedTargets=scanItems();lastScanTick=gameTime;}if(cachedTargets.isEmpty())return;var camera=context.worldState().cameraRenderState.pos;List<RenderBox>boxes=new ArrayList<>(cachedTargets.size());for(EntityTarget target:cachedTargets){float fade=distanceFade(target.distance());if(fade>0)boxes.add(new RenderBox(target.minX(),target.minY(),target.minZ(),target.maxX(),target.maxY(),target.maxZ(),fadeStyle(module.renderStyle(),fade)));}if(!boxes.isEmpty()){RenderBoxRenderer.fill(context.matrices(),context.consumers(),camera.x,camera.y,camera.z,boxes);RenderBoxRenderer.outline(context.matrices(),context.consumers(),camera.x,camera.y,camera.z,boxes);}if(module.showLabels())renderLabels(context,cachedTargets,camera.x,camera.y,camera.z);}
    private List<EntityTarget>scanItems(){return scanner.scan(client,new ItemScanConfig(module));}
    private void renderLabels(WorldRenderContext context,List<EntityTarget>targets,double cameraX,double cameraY,double cameraZ){PoseStack matrices=context.matrices();MultiBufferSource consumers=context.consumers();Font font=client.font;for(EntityTarget target:targets){float fade=distanceFade(target.distance());if(fade<=0)continue;String text=module.showCount()&&target.itemCount()>1?target.displayName()+" x"+target.itemCount():target.displayName();if(module.labelDistance())text+=" ["+formatDistance(target.distance())+"]";float scale=module.labelScale();matrices.pushPose();matrices.translate(target.centerX()-cameraX,target.maxY()-cameraY+module.labelHeight(),target.centerZ()-cameraZ);matrices.mulPose(context.worldState().cameraRenderState.orientation);matrices.scale(-.025f*scale,-.025f*scale,.025f*scale);int width=font.width(text);font.drawInBatch(Component.literal(text),-width/2f,0,fadeAlpha(module.labelColor(),fade),false,matrices.last().pose(),consumers,Font.DisplayMode.NORMAL,0,0xF000F0);matrices.popPose();}}
    private float distanceFade(double distance){if(!module.distanceFade())return 1;double n=Math.max(0,Math.min(1,distance/Math.max(1,module.range())));return(float)Math.max(0,Math.min(1,1-Math.max(0,n-.45)/.55));}
    private static RenderStyle fadeStyle(RenderStyle style,float fade){return new RenderStyle(style.color(),style.fill(),style.outline(),style.fillAlpha()*fade,style.outlineAlpha()*fade,style.lineWidth());}private static int fadeAlpha(int argb,float fade){int alpha=Math.round(((argb>>>24)&255)*fade);return(argb&0x00FFFFFF)|(alpha<<24);}private static String formatDistance(double distance){return String.format(java.util.Locale.ROOT,"%.1fm",distance);}
    private static final class ItemScanConfig implements EntityScanConfig{private final ItemESPModule module;private ItemScanConfig(ItemESPModule module){this.module=module;}public boolean enabled(){return module.enabled();}public boolean showPlayers(){return false;}public boolean showMobs(){return false;}public boolean showAnimals(){return false;}public boolean showItems(){return true;}public double range(){return module.range();}public int playerColor(){return module.colorArgb();}public int mobColor(){return module.colorArgb();}public int animalColor(){return module.colorArgb();}public int itemColor(){return module.colorArgb();}}
}
