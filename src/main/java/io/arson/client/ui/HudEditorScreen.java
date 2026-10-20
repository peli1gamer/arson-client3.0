package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.CombatInfoModule;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HudModule;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 1.21.11 HUD editor with multi-selection, persistent relative anchors and compatible cross-element clipboard editing. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS={"watermark","coordinates","fps","player-info","world-info","combat-info","array-list"};
    private final Screen parent; private HudModule hud; private HudLayoutModule layout; private ArrayListModule arrayList; private CombatInfoModule combatInfo;
    private final HudSelectionModel selection=new HudSelectionModel("watermark");
    private final Map<String,Snapshot> clipboard=new HashMap<>();
    private boolean dragging; private double offsetX,offsetY;
    public HudEditorScreen(Screen parent){super(Component.literal("HUD Editor"));this.parent=parent;}
    @Override protected void init(){hud=(HudModule)ArsonClient.getInstance().modules().get("hud");layout=(HudLayoutModule)ArsonClient.getInstance().modules().get("hud-layout");arrayList=(ArrayListModule)ArsonClient.getInstance().modules().get("array-list");combatInfo=(CombatInfoModule)ArsonClient.getInstance().modules().get("combat-info");if(hud==null)return;
        repairLayout();
        int tabGap=4;int tabWidth=Math.max(40,Math.min(88,(width-20-tabGap*(ELEMENTS.length-1))/ELEMENTS.length));
        for(int i=0;i<ELEMENTS.length;i++){final String e=ELEMENTS[i];addRenderableWidget(Button.builder(Component.literal((selection.contains(e)?"> ":"")+e),b->{selection.select(e,false);rebuild();}).bounds(10+i*(tabWidth+tabGap),45,tabWidth,22).build());}
        int y=78;
        addRenderableWidget(Button.builder(Component.literal("Visible: "+(allVisible()?"ON":"MIXED")),b->{toggleVisible();save();rebuild();}).bounds(10,y,120,22).build());
        addRenderableWidget(Button.builder(Component.literal("Scale -"),b->{adjustScale(-.05);save();rebuild();}).bounds(135,y,70,22).build());
        addRenderableWidget(Button.builder(Component.literal("Scale +"),b->{adjustScale(.05);save();rebuild();}).bounds(210,y,70,22).build());
        if(selection.elements().stream().anyMatch(this::supportsAlignment))addRenderableWidget(Button.builder(Component.literal("Align"),b->{cycleAlignment();save();rebuild();}).bounds(285,y,70,22).build());
        if(selection.elements().stream().anyMatch(this::supportsAnchor))addRenderableWidget(Button.builder(Component.literal("Anchor"),b->{anchorSelected();save();rebuild();}).bounds(360,y,75,22).build());
        addRenderableWidget(Button.builder(Component.literal("Copy"),b->{copySelected();rebuild();}).bounds(440,y,60,22).build());
        addRenderableWidget(Button.builder(Component.literal("Paste"),b->{pasteSelected();save();rebuild();}).bounds(505,y,60,22).build());
        addRenderableWidget(Button.builder(Component.literal("Duplicate"),b->{duplicateSelected();save();rebuild();}).bounds(570,y,78,22).build());
        addRenderableWidget(Button.builder(Component.literal("Background"),b->{toggleBackground();save();rebuild();}).bounds(653,y,90,22).build());
        addRenderableWidget(Button.builder(Component.literal("Reset Selected"),b->{resetSelected();save();rebuild();}).bounds(748,y,105,22).build());
        addRenderableWidget(Button.builder(Component.literal("Select All"),b->{selection.replace(Set.of(ELEMENTS));rebuild();}).bounds(10,106,85,22).build());
        addRenderableWidget(Button.builder(Component.literal("Clear Selection"),b->{selection.clear();rebuild();}).bounds(100,106,100,22).build());
        if(selection.elements().stream().anyMatch(this::supportsRowFormat))addRenderableWidget(Button.builder(Component.literal(formatLabel()),b->{cycleRowFormat();save();rebuild();}).bounds(205,106,145,22).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(width-85,height-30,75,22).build());
    }
    private void rebuild(){clearWidgets();init();}
    private void repairLayout(){
        if(layout==null)return;
        if(arrayList!=null) layout.setArrayListPreferredPosition(arrayList.x(),arrayList.y());
        if(layout.repairNoOverlap(width,height,hud.scale(),Math.max(90,Math.min(width/2,220)),Math.max(24,Math.min(height/2,320)),arrayList)) save();
    }
    private boolean visible(String e){if("array-list".equals(e))return arrayList!=null&&arrayList.enabled();if("combat-info".equals(e))return combatInfo!=null&&combatInfo.enabled();return hud.elementVisible(e);}
    private boolean allVisible(){if(selection.size()==0)return false;for(String e:selection.elements())if(!visible(e))return false;return true;}
    private void toggleVisible(){boolean target=!allVisible();for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null&&arrayList.enabled()!=target)arrayList.toggle();}else if("combat-info".equals(e)){if(combatInfo!=null)combatInfo.setEnabled(target);}else hud.setElementVisible(e,target);}
    private void adjustScale(double delta){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.adjustScale(delta);continue;}if("combat-info".equals(e)){if(combatInfo!=null)for(Setting<?> setting:combatInfo.settings())if(setting instanceof DoubleSetting d&&d.id().equals("info-scale"))d.set(d.get()+delta);continue;}for(Setting<?> setting:hud.settings())if(setting instanceof DoubleSetting d&&d.id().equals(e+"-scale"))d.set(d.get()+delta);}}
    private void toggleBackground(){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.toggleBackground();continue;}if("combat-info".equals(e)){if(combatInfo!=null)for(Setting<?> setting:combatInfo.settings())if(setting instanceof BooleanSetting b&&b.id().equals("background"))b.set(!b.enabled());continue;}for(Setting<?> setting:hud.settings())if(setting instanceof BooleanSetting b&&b.id().equals(e+"-background"))b.set(!b.enabled());}}
    private void cycleAlignment(){for(String e:selection.elements())if(supportsAlignment(e))hud.cycleAlignment(e);}
    private boolean supportsAlignment(String e){return !"array-list".equals(e)&&!"combat-info".equals(e);}
    private boolean supportsAnchor(String e){return !"array-list".equals(e)&&!"combat-info".equals(e);}
    private void resetSelected(){
        for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null)arrayList.resetSettings();}
        else if("combat-info".equals(e)){if(combatInfo!=null)for(Setting<?> setting:combatInfo.settings())if(Set.of("x","y","info-scale","background","text-color").contains(setting.id()))setting.reset();}
        else{hud.resetElement(e);if(layout!=null)layout.resetElement(e);if(supportsRowFormat(e))hud.setElementRowFormat(e,HudModule.ElementRowFormat.INHERIT);}
    }
    private boolean supportsRowFormat(String element){return "player-info".equals(element)||"world-info".equals(element);}
    private HudModule.ElementRowFormat rowFormatOverride(String element){return switch(element){case "player-info"->hud.playerInfoRowFormat();case "world-info"->hud.worldInfoRowFormat();default->null;};}
    private String formatLabel(){
        HudModule.ElementRowFormat common=null;
        for(String e:selection.elements())if(supportsRowFormat(e)){
            HudModule.ElementRowFormat current=rowFormatOverride(e);
            if(common==null)common=current;else if(common!=current)return "Format: Mixed";
        }
        if(common==null)return "Format: —";
        return "Format: "+switch(common){case INHERIT->"Inherit";case STACKED->"Stacked";case COMPACT->"Compact";case DENSE->"Dense";case TWO_COLUMN->"Two Col";};
    }
    private void cycleRowFormat(){
        HudModule.ElementRowFormat[] values=HudModule.ElementRowFormat.values();
        for(String e:selection.elements())if(supportsRowFormat(e)){
            HudModule.ElementRowFormat current=rowFormatOverride(e);
            hud.setElementRowFormat(e,values[(current.ordinal()+1)%values.length]);
        }
    }
    private void save(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private double[] position(String e){if("array-list".equals(e))return new double[]{arrayList.x(),arrayList.y()};if("combat-info".equals(e))return combatInfo==null?new double[]{0,0}:new double[]{combatInfo.x(),combatInfo.y()};if(layout==null)return switch(e){case "coordinates"->new double[]{hud.coordinatesX(),hud.coordinatesY()};case "fps"->new double[]{hud.fpsX(),hud.fpsY()};case "player-info"->new double[]{hud.playerInfoX(),hud.playerInfoY()};case "world-info"->new double[]{hud.worldInfoX(),hud.worldInfoY()};default->new double[]{hud.x(),hud.y()};};return layout.resolve(e,width,height,190,hud.scale()*hud.elementScale(e)*35);}
    private void setPosition(String e,double x,double y){if("array-list".equals(e)){arrayList.setEditorPosition(x,y);return;}double step=hud.gridSnap()?hud.gridSize():1.0;x=Math.round(Math.max(0.0,x)/step)*step;y=Math.round(Math.max(0.0,y)/step)*step;if("combat-info".equals(e)){if(combatInfo!=null)combatInfo.setEditorPosition(x,y);return;}if(layout!=null)layout.setPositionPreservingAnchor(e,x,y,width,height,190,hud.scale()*hud.elementScale(e)*35);else hud.setEditorPosition(e,x,y);}
    private double widgetScale(String e){if("array-list".equals(e))return arrayList.scale();if("combat-info".equals(e))return combatInfo==null?1.0:combatInfo.infoScale();return hud.scale()*hud.elementScale(e);}
    private int widgetWidth(String e){return "combat-info".equals(e)?280:190;}
    private int widgetHeight(String e){return "combat-info".equals(e)?180:35;}
    private boolean hit(String e,double x,double y){if(!visible(e))return false;double[]p=position(e);double s=widgetScale(e);return x>=p[0]-8&&x<=p[0]+widgetWidth(e)*s&&y>=p[1]-8&&y<=p[1]+widgetHeight(e)*s;}
    private Snapshot snapshot(String e){if("array-list".equals(e)||hud==null)return null;double[]p=position(e);if("combat-info".equals(e)){return combatInfo==null?null:new Snapshot(e,visible(e),p[0],p[1],combatInfo.infoScale(),combatInfo.background(),"left",combatInfo.textColor(),HudLayoutModule.Anchor.TOP_LEFT,null);}HudLayoutModule.Anchor anchor=layout==null?HudLayoutModule.Anchor.TOP_LEFT:layout.anchor(e);HudModule.ElementRowFormat format=supportsRowFormat(e)?rowFormatOverride(e):null;return new Snapshot(e,visible(e),p[0],p[1],hud.elementScale(e),hud.elementBackground(e),hud.elementAlignment(e),hud.elementColor(e),anchor,format);}
    private void copySelected(){clipboard.clear();for(String e:selection.elements()){Snapshot s=snapshot(e);if(s!=null)clipboard.put(e,s);}}
    private void pasteSelected(){if(clipboard.isEmpty()||hud==null)return;List<Snapshot> values=new ArrayList<>(clipboard.values());for(String target:selection.elements()){if("array-list".equals(target))continue;Snapshot source=clipboard.get(target);if(source==null)source=values.get(0);if(source!=null&&HudClipboardCompatible.isCompatible(source.source,target))applySnapshot(target,source);}}
    private void duplicateSelected(){if(selection.size()!=1)return;String source=selection.elements().iterator().next();Snapshot s=snapshot(source);if(s==null)return;for(String target:ELEMENTS){if(target.equals(source)||selection.contains(target)||"array-list".equals(target))continue;if(HudClipboardCompatible.isCompatible(source,target)){applySnapshot(target,new Snapshot(source,s.visible,s.x+12,s.y+12,s.scale,s.background,s.alignment,s.color,s.anchor,s.rowFormat));selection.select(target,false);break;}}}
    private void applySnapshot(String e,Snapshot s){if(s==null||"array-list".equals(e))return;if("combat-info".equals(e)){if(combatInfo==null)return;combatInfo.setEnabled(s.visible);combatInfo.setEditorPosition(s.x,s.y);for(Setting<?> setting:combatInfo.settings()){if(setting instanceof DoubleSetting d&&d.id().equals("info-scale"))d.set(s.scale);else if(setting instanceof BooleanSetting b&&b.id().equals("background"))b.set(s.background);else if(setting instanceof ColorSetting c&&c.id().equals("text-color"))c.set(s.color);}return;}hud.setElementVisible(e,s.visible);if(supportsRowFormat(e)&&s.rowFormat!=null)hud.setElementRowFormat(e,s.rowFormat);if(layout!=null)layout.setAnchor(e,s.anchor);setPosition(e,s.x,s.y);for(Setting<?> setting:hud.settings()){
            if(setting instanceof DoubleSetting d&&d.id().equals(e+"-scale"))d.set(s.scale);
            else if(setting instanceof BooleanSetting b&&b.id().equals(e+"-background"))b.set(s.background);
            else if(setting instanceof ColorSetting c&&c.id().equals(e+"-color"))c.set(s.color);
            else if(setting instanceof io.arson.client.settings.StringSetting str&&str.id().equals(e+"-align"))str.set(s.alignment);
        }}
    private void anchorSelected(){if(layout==null)return;HudLayoutModule.Anchor[] anchors=HudLayoutModule.Anchor.values();for(String e:selection.elements()){if(!supportsAnchor(e))continue;Snapshot current=snapshot(e);HudLayoutModule.Anchor next=anchors[(layout.anchor(e).ordinal()+1)%anchors.length];layout.setAnchorPreservingPosition(e,next,current.x,current.y,width,height,190,hud.scale()*hud.elementScale(e)*35);}}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(event.button()==0){for(String e:ELEMENTS)if(hit(e,event.x(),event.y())){selection.select(e,event.hasShiftDown());double[]p=position(e);offsetX=event.x()-p[0];offsetY=event.y()-p[1];dragging=true;return true;}}return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dragX,double dragY){if(dragging&&event.button()==0){double x=event.x()-offsetX,y=event.y()-offsetY;if(selection.size()>0){String anchor=selection.elements().iterator().next();double[] a=position(anchor);double dx=x-a[0],dy=y-a[1];for(String e:selection.elements()){double[]p=position(e);setPosition(e,p[0]+dx,p[1]+dy);}}return true;}return super.mouseDragged(event,dragX,dragY);}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(event.button()==0&&dragging){dragging=false;repairLayout();save();return true;}return super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}if(event.key()==GLFW.GLFW_KEY_C&&event.hasControlDown()){copySelected();return true;}if(event.key()==GLFW.GLFW_KEY_V&&event.hasControlDown()){pasteSelected();save();rebuild();return true;}if(event.key()==GLFW.GLFW_KEY_D&&event.hasControlDown()){duplicateSelected();save();rebuild();return true;}return super.keyPressed(event);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){graphics.fill(0,0,width,height,0x66000000);graphics.drawString(font,"HUD Editor — "+selection.size()+" selected",10,12,0xFFFFFFFF);graphics.drawString(font,"Shift-click group | drag | Ctrl+C/V/D copy, paste, duplicate",10,27,0xFFAAAAAA);for(String e:selection.elements())if(visible(e)){double[]p=position(e);int x=(int)p[0],y=(int)p[1];double scale=widgetScale(e);int boxWidth=(int)Math.ceil(widgetWidth(e)*scale),boxHeight=(int)Math.ceil(widgetHeight(e)*scale);graphics.renderOutline(x-4,y-4,boxWidth+8,boxHeight+8,0xFF55AAFF);if("combat-info".equals(e)){graphics.pose().pushMatrix();graphics.pose().translate(x,y);graphics.pose().scale((float)combatInfo.infoScale(),(float)combatInfo.infoScale());if(combatInfo.background())graphics.fill(-combatInfo.padding(),-combatInfo.padding(),280+combatInfo.padding(),180+combatInfo.padding(),combatInfo.backgroundColor());graphics.drawString(font,"Combat Info",0,0,combatInfo.textColor(),true);graphics.drawString(font,"Target state: Sprinting",0,12,combatInfo.textColor(),true);graphics.drawString(font,"HP 18.0/20.0",0,24,combatInfo.textColor(),true);graphics.drawString(font,"Absorption 2.0 • Armor 20",0,36,combatInfo.textColor(),true);graphics.drawString(font,"Armor durability H 100 C 86 L 70 F 55",0,48,combatInfo.textColor(),true);graphics.drawString(font,"Speed 1 0:20",0,60,combatInfo.textColor(),true);graphics.drawString(font,"Distance 4.2m",0,72,combatInfo.textColor(),true);graphics.drawString(font,"Look offset yaw +3.0° pitch -1.2°",0,84,combatInfo.textColor(),true);graphics.drawString(font,"Held Diamond Sword",0,96,combatInfo.textColor(),true);graphics.drawString(font,"Target held Shield",0,108,combatInfo.textColor(),true);graphics.drawString(font,"Durability 120/156",0,120,combatInfo.textColor(),true);graphics.drawString(font,"Cooldown 90% • Ready",0,132,combatInfo.textColor(),true);graphics.pose().popMatrix();}else graphics.drawString(font,e,x,y+6,0xFFFFFFFF);}super.render(graphics,mouseX,mouseY,delta);}
    @Override public void onClose(){save();minecraft.setScreen(parent);}
    private record Snapshot(String source,boolean visible,double x,double y,double scale,boolean background,String alignment,int color,HudLayoutModule.Anchor anchor,HudModule.ElementRowFormat rowFormat){}
    static final class HudClipboardCompatible {
        private HudClipboardCompatible() {}
        static boolean isCompatible(String source,String target){return source!=null&&target!=null&&!source.equals("array-list")&&!target.equals("array-list")&&STANDARD.contains(source)&&STANDARD.contains(target);}
        private static final Set<String> STANDARD=Set.of("watermark","coordinates","fps","player-info","world-info","combat-info");
    }
}
