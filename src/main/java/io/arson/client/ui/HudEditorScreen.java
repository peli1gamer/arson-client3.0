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

/** Compact 1.21.11 HUD editor with live selection and dragging. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS={"watermark","coordinates","fps","player-info","world-info","array-list"};
    private final Screen parent; private HudModule hud; private ArrayListModule arrayList; private String selected="watermark"; private boolean dragging; private double offsetX,offsetY;
    public HudEditorScreen(Screen parent){super(Component.literal("HUD Editor"));this.parent=parent;}
    @Override protected void init(){hud=(HudModule)ArsonClient.getInstance().modules().get("hud");arrayList=(ArrayListModule)ArsonClient.getInstance().modules().get("array-list");if(hud==null)return;for(int i=0;i<ELEMENTS.length;i++){final String e=ELEMENTS[i];addRenderableWidget(Button.builder(Component.literal((e.equals(selected)?"> ":"")+e),b->{selected=e;rebuild();}).bounds(10+i*92,45,88,22).build());}int y=78;addRenderableWidget(Button.builder(Component.literal("Visible: "+(visible()?"ON":"OFF")),b->{toggleVisible();save();rebuild();}).bounds(10,y,120,22).build());addRenderableWidget(Button.builder(Component.literal("Scale -"),b->{adjustScale(-.05);save();rebuild();}).bounds(135,y,70,22).build());addRenderableWidget(Button.builder(Component.literal("Scale +"),b->{adjustScale(.05);save();rebuild();}).bounds(210,y,70,22).build());addRenderableWidget(Button.builder(Component.literal("Background"),b->{toggleBackground();save();rebuild();}).bounds(285,y,100,22).build());addRenderableWidget(Button.builder(Component.literal("Reset"),b->{reset();save();rebuild();}).bounds(390,y,70,22).build());addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(width-85,height-30,75,22).build());}
    private void rebuild(){clearWidgets();init();}
    private boolean visible(){return "array-list".equals(selected)?arrayList!=null&&arrayList.enabled():hud.elementVisible(selected);}
    private void toggleVisible(){if("array-list".equals(selected)){if(arrayList!=null)arrayList.toggle();}else hud.setElementVisible(selected,!hud.elementVisible(selected));}
    private void adjustScale(double delta){if("array-list".equals(selected)){arrayList.adjustScale(delta);return;}for(Setting<?> setting:hud.settings())if(setting instanceof DoubleSetting d&&d.id().equals(selected+"-scale")){d.set(Math.max(d.min(),Math.min(d.max(),d.get()+delta)));return;}}
    private void toggleBackground(){if("array-list".equals(selected)){arrayList.toggleBackground();return;}for(Setting<?> setting:hud.settings())if(setting instanceof BooleanSetting b&&b.id().equals(selected+"-background")){b.set(!b.enabled());return;}}
    private void reset(){if("array-list".equals(selected))arrayList.resetSettings();else hud.resetElement(selected);}
    private void save(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private double[] position(){if("array-list".equals(selected))return new double[]{arrayList.x(),arrayList.y()};return switch(selected){case "coordinates"->new double[]{hud.coordinatesX(),hud.coordinatesY()};case "fps"->new double[]{hud.fpsX(),hud.fpsY()};case "player-info"->new double[]{hud.playerInfoX(),hud.playerInfoY()};case "world-info"->new double[]{hud.worldInfoX(),hud.worldInfoY()};default->new double[]{hud.x(),hud.y()};};}
    private boolean hit(double x,double y){if(!visible())return false;double[]p=position();double s="array-list".equals(selected)?arrayList.scale():hud.scale()*hud.elementScale(selected);return x>=p[0]-8&&x<=p[0]+190*s&&y>=p[1]-8&&y<=p[1]+35*s;}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(event.button()==0&&hit(event.x(),event.y())){dragging=true;double[]p=position();offsetX=event.x()-p[0];offsetY=event.y()-p[1];return true;}return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dragX,double dragY){if(dragging&&event.button()==0){double x=event.x()-offsetX,y=event.y()-offsetY;if("array-list".equals(selected))arrayList.setEditorPosition(x/arrayList.scale(),y/arrayList.scale());else hud.setEditorPosition(selected,x/hud.scale(),y/hud.scale());return true;}return super.mouseDragged(event,dragX,dragY);}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(event.button()==0&&dragging){dragging=false;save();return true;}return super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}return super.keyPressed(event);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){graphics.fill(0,0,width,height,0x66000000);graphics.drawString(font,"HUD Editor — "+selected,10,12,0xFFFFFFFF);graphics.drawString(font,"Drag the selected HUD element; changes save on release.",10,27,0xFFAAAAAA);if(visible()){double[]p=position();int x=(int)p[0],y=(int)p[1];graphics.renderOutline(x-4,y-4,190,35,0xFF55AAFF);graphics.drawString(font,selected,x,y+6,0xFFFFFFFF);}super.render(graphics,mouseX,mouseY,delta);}
    @Override public void onClose(){save();minecraft.setScreen(parent);}
}
