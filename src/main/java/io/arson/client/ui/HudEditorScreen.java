package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.HudModule;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Set;

/** 1.21.11 HUD editor with deterministic multi-selection and group dragging. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS={"watermark","coordinates","fps","player-info","world-info","array-list"};
    private final Screen parent; private HudModule hud; private ArrayListModule arrayList;
    private final HudSelectionModel selection=new HudSelectionModel("watermark");
    private boolean dragging; private double offsetX,offsetY;
    public HudEditorScreen(Screen parent){super(Component.literal("HUD Editor"));this.parent=parent;}
    @Override protected void init(){hud=(HudModule)ArsonClient.getInstance().modules().get("hud");arrayList=(ArrayListModule)ArsonClient.getInstance().modules().get("array-list");if(hud==null)return;for(int i=0;i<ELEMENTS.length;i++){final String e=ELEMENTS[i];addRenderableWidget(Button.builder(Component.literal((selection.contains(e)?"> ":"")+e),b->{selection.select(e,false);rebuild();}).bounds(10+i*92,45,88,22).build());}int y=78;addRenderableWidget(Button.builder(Component.literal("Visible: "+(allVisible()?"ON":"MIXED")),b->{toggleVisible();save();rebuild();}).bounds(10,y,120,22).build());addRenderableWidget(Button.builder(Component.literal("Scale -"),b->{adjustScale(-.05);save();rebuild();}).bounds(135,y,70,22).build());addRenderableWidget(Button.builder(Component.literal("Scale +"),b->{adjustScale(.05);save();rebuild();}).bounds(210,y,70,22).build());addRenderableWidget(Button.builder(Component.literal("Background"),b->{toggleBackground();save();rebuild();}).bounds(285,y,100,22).build());addRenderableWidget(Button.builder(Component.literal("Reset Selected"),b->{resetSelected();save();rebuild();}).bounds(390,y,110,22).build());addRenderableWidget(Button.builder(Component.literal("Select All"),b->{selection.replace(Set.of(ELEMENTS));rebuild();}).bounds(505,y,85,22).build());addRenderableWidget(Button.builder(Component.literal("Clear Selection"),b->{selection.clear();rebuild();}).bounds(595,y,100,22).build());addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(width-85,height-30,75,22).build());}
    private void rebuild(){clearWidgets();init();}
    private boolean visible(String e){return "array-list".equals(e)?arrayList!=null&&arrayList.enabled():hud.elementVisible(e);}
    private boolean allVisible(){if(selection.size()==0)return false;for(String e:selection.elements())if(!visible(e))return false;return true;}
    private void toggleVisible(){boolean target=!allVisible();for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null&&arrayList.enabled()!=target)arrayList.toggle();}else hud.setElementVisible(e,target);}
    private void adjustScale(double delta){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.adjustScale(delta);continue;}for(Setting<?> setting:hud.settings())if(setting instanceof DoubleSetting d&&d.id().equals(e+"-scale"))d.set(Math.max(d.min(),Math.min(d.max(),d.get()+delta)));}}
    private void toggleBackground(){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.toggleBackground();continue;}for(Setting<?> setting:hud.settings())if(setting instanceof BooleanSetting b&&b.id().equals(e+"-background"))b.set(!b.enabled());}}
    private void resetSelected(){for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null)arrayList.resetSettings();}else hud.resetElement(e);}
    private void save(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private double[] position(String e){if("array-list".equals(e))return new double[]{arrayList.x(),arrayList.y()};return switch(e){case "coordinates"->new double[]{hud.coordinatesX(),hud.coordinatesY()};case "fps"->new double[]{hud.fpsX(),hud.fpsY()};case "player-info"->new double[]{hud.playerInfoX(),hud.playerInfoY()};case "world-info"->new double[]{hud.worldInfoX(),hud.worldInfoY()};default->new double[]{hud.x(),hud.y()};};}
    private void setPosition(String e,double x,double y){if("array-list".equals(e)){arrayList.setEditorPosition(x,y);return;}hud.setEditorPosition(e,x,y);}
    private boolean hit(String e,double x,double y){if(!visible(e))return false;double[]p=position(e);double s="array-list".equals(e)?arrayList.scale():hud.scale()*hud.elementScale(e);return x>=p[0]-8&&x<=p[0]+190*s&&y>=p[1]-8&&y<=p[1]+35*s;}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(event.button()==0){for(String e:ELEMENTS)if(hit(e,event.x(),event.y())){selection.select(e,event.hasShiftDown());double[]p=position(e);offsetX=event.x()-p[0];offsetY=event.y()-p[1];dragging=true;return true;}}return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dragX,double dragY){if(dragging&&event.button()==0){double x=event.x()-offsetX,y=event.y()-offsetY;if(selection.size()>0){String anchor=selection.elements().iterator().next();double[] a=position(anchor);double dx=x-a[0],dy=y-a[1];for(String e:selection.elements()){double[]p=position(e);setPosition(e,p[0]+dx,p[1]+dy);}}return true;}return super.mouseDragged(event,dragX,dragY);}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(event.button()==0&&dragging){dragging=false;save();return true;}return super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}return super.keyPressed(event);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){graphics.fill(0,0,width,height,0x66000000);graphics.drawString(font,"HUD Editor — "+selection.size()+" selected",10,12,0xFFFFFFFF);graphics.drawString(font,"Shift-click elements to group; drag a selected element to move the group.",10,27,0xFFAAAAAA);for(String e:selection.elements())if(visible(e)){double[]p=position(e);int x=(int)p[0],y=(int)p[1];graphics.renderOutline(x-4,y-4,190,35,0xFF55AAFF);graphics.drawString(font,e,x,y+6,0xFFFFFFFF);}super.render(graphics,mouseX,mouseY,delta);}
    @Override public void onClose(){save();minecraft.setScreen(parent);}
}
