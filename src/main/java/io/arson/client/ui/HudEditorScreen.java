package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ArrayListModule;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 1.21.11 HUD editor with multi-selection, persistent relative anchors and compatible cross-element clipboard editing. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS={"watermark","coordinates","fps","player-info","world-info","array-list"};
    private final Screen parent; private HudModule hud; private HudLayoutModule layout; private ArrayListModule arrayList;
    private final HudSelectionModel selection=new HudSelectionModel("watermark");
    private final Map<String,Snapshot> clipboard=new LinkedHashMap<>();
    private boolean dragging; private String draggedElement; private double offsetX,offsetY;
    private int toolbarBottom = 136;
    public HudEditorScreen(Screen parent){super(Component.literal("HUD Editor"));this.parent=parent;}
    @Override protected void init(){hud=(HudModule)ArsonClient.getInstance().modules().get("hud");layout=(HudLayoutModule)ArsonClient.getInstance().modules().get("hud-layout");arrayList=(ArrayListModule)ArsonClient.getInstance().modules().get("array-list");if(hud==null)return;
        repairLayout();
        int selectorGap=5;
        int selectorWidth=Math.max(34,(width-20-selectorGap*(ELEMENTS.length-1))/ELEMENTS.length);
        String[] shortNames={"Watermark","Coords","FPS","Player","World","Array"};
        for(int i=0;i<ELEMENTS.length;i++){
            final String e=ELEMENTS[i];
            String label=selectorWidth<72?shortNames[i]:e;
            int x=10+i*(selectorWidth+selectorGap);
            addRenderableWidget(Button.builder(Component.literal((selection.contains(e)?"> ":"")+label),b->{selection.select(e,false);rebuild();}).bounds(x,45,selectorWidth,22).build());
        }
        int[] cursor={10,78};
        addHudButton("Visible: "+(allVisible()?"ON":"MIXED"),112,cursor,()->{toggleVisible();save();rebuild();});
        addHudButton("Scale -",64,cursor,()->{adjustScale(-.05);save();rebuild();});
        addHudButton("Scale +",64,cursor,()->{adjustScale(.05);save();rebuild();});
        addHudButton("Align",58,cursor,()->{cycleAlignment();save();rebuild();});
        addHudButton("Anchor",64,cursor,()->{anchorSelected();save();rebuild();});
        addHudButton("Copy",52,cursor,()->{copySelected();rebuild();});
        addHudButton("Paste",52,cursor,()->{pasteSelected();save();rebuild();});
        addHudButton("Duplicate",76,cursor,()->{duplicateSelected();save();rebuild();});
        addHudButton("Background",84,cursor,()->{toggleBackground();save();rebuild();});
        addHudButton("Reset Selected",104,cursor,()->{resetSelected();save();rebuild();});
        addHudButton("Select All",78,cursor,()->{selection.replace(java.util.Arrays.asList(ELEMENTS));rebuild();});
        addHudButton("Clear Selection",98,cursor,()->{selection.clear();rebuild();});
        toolbarBottom=cursor[1]+27;
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(Math.max(10,width-85),Math.max(toolbarBottom+8,height-30),75,22).build());
    }
    private void addHudButton(String label,int preferredWidth,int[] cursor,Runnable action){
        int available=Math.max(40,width-20);
        int buttonWidth=Math.min(preferredWidth,available);
        if(cursor[0]+buttonWidth>width-10){cursor[0]=10;cursor[1]+=27;}
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(cursor[0],cursor[1],buttonWidth,22).build());
        cursor[0]+=buttonWidth+5;
    }
    private void rebuild(){clearWidgets();init();}
    private void repairLayout(){
        if(layout==null)return;
        if(arrayList!=null) layout.setArrayListPreferredPosition(arrayList.x(),arrayList.y());
        if(layout.repairNoOverlap(width,height,hud.scale(),Math.max(90,Math.min(width/2,220)),Math.max(24,Math.min(height/2,320)),arrayList)) save();
    }
    private boolean visible(String e){return "array-list".equals(e)?arrayList!=null&&arrayList.enabled():hud.elementVisible(e);}
    private boolean allVisible(){if(selection.size()==0)return false;for(String e:selection.elements())if(!visible(e))return false;return true;}
    private void toggleVisible(){boolean target=!allVisible();for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null&&arrayList.enabled()!=target)arrayList.toggle();}else hud.setElementVisible(e,target);}
    private void adjustScale(double delta){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.adjustScale(delta);continue;}for(Setting<?> setting:hud.settings())if(setting instanceof DoubleSetting d&&d.id().equals(e+"-scale"))d.set(d.get()+delta);}}
    private void toggleBackground(){for(String e:selection.elements()){if("array-list".equals(e)){if(arrayList!=null)arrayList.toggleBackground();continue;}for(Setting<?> setting:hud.settings())if(setting instanceof BooleanSetting b&&b.id().equals(e+"-background"))b.set(!b.enabled());}}
    private void cycleAlignment(){for(String e:selection.elements())if(!"array-list".equals(e))hud.cycleAlignment(e);}
    private void resetSelected(){for(String e:selection.elements())if("array-list".equals(e)){if(arrayList!=null)arrayList.resetSettings();}else{hud.resetElement(e);if(layout!=null)layout.resetElement(e);}}
    private void save(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private double[] position(String e){if("array-list".equals(e))return new double[]{arrayList.x(),arrayList.y()};if(layout==null)return switch(e){case "coordinates"->new double[]{hud.coordinatesX(),hud.coordinatesY()};case "fps"->new double[]{hud.fpsX(),hud.fpsY()};case "player-info"->new double[]{hud.playerInfoX(),hud.playerInfoY()};case "world-info"->new double[]{hud.worldInfoX(),hud.worldInfoY()};default->new double[]{hud.x(),hud.y()};};return layout.resolve(e,width,height,190,hud.scale()*hud.elementScale(e)*35);}
    private void setPosition(String e,double x,double y){if("array-list".equals(e)){arrayList.setEditorPosition(x,y);return;}if(layout!=null)layout.setPositionPreservingAnchor(e,x,y,width,height,190,hud.scale()*hud.elementScale(e)*35);else hud.setEditorPosition(e,x,y);}
    private boolean hit(String e,double x,double y){if(!visible(e))return false;double[]p=position(e);double s="array-list".equals(e)?arrayList.scale():hud.scale()*hud.elementScale(e);return x>=p[0]-8&&x<=p[0]+190*s&&y>=p[1]-8&&y<=p[1]+35*s;}
    private Snapshot snapshot(String e){if("array-list".equals(e)||hud==null)return null;double[]p=position(e);HudLayoutModule.Anchor anchor=layout==null?HudLayoutModule.Anchor.TOP_LEFT:layout.anchor(e);return new Snapshot(e,visible(e),p[0],p[1],hud.elementScale(e),hud.elementBackground(e),hud.elementAlignment(e),hud.elementColor(e),anchor);}
    private void copySelected(){clipboard.clear();for(String e:selection.elements()){Snapshot s=snapshot(e);if(s!=null)clipboard.put(e,s);}}
    private void pasteSelected(){if(clipboard.isEmpty()||hud==null)return;List<Snapshot> values=new ArrayList<>(clipboard.values());for(String target:selection.elements()){if("array-list".equals(target))continue;Snapshot source=clipboard.get(target);if(source==null)source=values.get(0);if(source!=null&&HudClipboardCompatible.isCompatible(source.source,target))applySnapshot(target,source);}}
    private void duplicateSelected(){if(selection.size()!=1)return;String source=selection.elements().iterator().next();Snapshot s=snapshot(source);if(s==null)return;for(String target:ELEMENTS){if(target.equals(source)||selection.contains(target)||"array-list".equals(target))continue;if(HudClipboardCompatible.isCompatible(source,target)){applySnapshot(target,new Snapshot(source,s.visible,s.x+12,s.y+12,s.scale,s.background,s.alignment,s.color,s.anchor));selection.select(target,false);break;}}}
    private void applySnapshot(String e,Snapshot s){if(s==null||"array-list".equals(e))return;hud.setElementVisible(e,s.visible);if(layout!=null){layout.setAnchor(e,s.anchor);layout.setPositionPreservingAnchor(e,s.x,s.y,width,height,190,hud.scale()*s.scale*35);}else hud.setEditorPosition(e,s.x,s.y);for(Setting<?> setting:hud.settings()){
            if(setting instanceof DoubleSetting d&&d.id().equals(e+"-scale"))d.set(s.scale);
            else if(setting instanceof BooleanSetting b&&b.id().equals(e+"-background"))b.set(s.background);
            else if(setting instanceof ColorSetting c&&c.id().equals(e+"-color"))c.set(s.color);
            else if(setting instanceof io.arson.client.settings.StringSetting str&&str.id().equals(e+"-align"))str.set(s.alignment);
        }}
    private void anchorSelected(){if(layout==null)return;HudLayoutModule.Anchor[] anchors=HudLayoutModule.Anchor.values();for(String e:selection.elements()){if("array-list".equals(e))continue;Snapshot current=snapshot(e);HudLayoutModule.Anchor next=anchors[(layout.anchor(e).ordinal()+1)%anchors.length];layout.setAnchorPreservingPosition(e,next,current.x,current.y,width,height,190,hud.scale()*hud.elementScale(e)*35);}}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(event.button()==0){for(String e:ELEMENTS)if(hit(e,event.x(),event.y())){boolean additive=event.hasShiftDown();if(additive&&selection.contains(e)){selection.select(e,true);dragging=false;draggedElement=null;rebuild();return true;}selection.select(e,additive);draggedElement=e;double[]p=position(e);offsetX=event.x()-p[0];offsetY=event.y()-p[1];dragging=true;return true;}}return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dragX,double dragY){if(dragging&&event.button()==0&&draggedElement!=null&&selection.contains(draggedElement)){double x=event.x()-offsetX,y=event.y()-offsetY;double[] anchor=position(draggedElement);double dx=x-anchor[0],dy=y-anchor[1];for(String e:selection.elements()){double[]p=position(e);setPosition(e,p[0]+dx,p[1]+dy);}return true;}return super.mouseDragged(event,dragX,dragY);}
    @Override public boolean mouseReleased(MouseButtonEvent event){if(event.button()==0&&dragging){dragging=false;draggedElement=null;repairLayout();save();return true;}return super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}if(event.key()==GLFW.GLFW_KEY_C&&event.hasControlDown()){copySelected();return true;}if(event.key()==GLFW.GLFW_KEY_V&&event.hasControlDown()){pasteSelected();save();rebuild();return true;}if(event.key()==GLFW.GLFW_KEY_D&&event.hasControlDown()){duplicateSelected();save();rebuild();return true;}return super.keyPressed(event);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){graphics.fill(0,0,width,height,0x66000000);graphics.drawString(font,"HUD Editor — "+selection.size()+" selected",10,12,0xFFFFFFFF);graphics.drawString(font,"Shift-click group | drag | Ctrl+C/V/D copy, paste, duplicate",10,27,0xFFAAAAAA);for(String e:selection.elements())if(visible(e)){double[]p=position(e);int x=(int)p[0],y=(int)p[1];graphics.renderOutline(x-4,y-4,190,35,0xFF55AAFF);graphics.drawString(font,e,x,y+6,0xFFFFFFFF);}super.render(graphics,mouseX,mouseY,delta);}
    @Override public void onClose(){save();minecraft.setScreen(parent);}
    private record Snapshot(String source,boolean visible,double x,double y,double scale,boolean background,String alignment,int color,HudLayoutModule.Anchor anchor){}
    static final class HudClipboardCompatible {
        private HudClipboardCompatible() {}
        static boolean isCompatible(String source,String target){return source!=null&&target!=null&&!source.equals("array-list")&&!target.equals("array-list")&&STANDARD.contains(source)&&STANDARD.contains(target);}
        private static final Set<String> STANDARD=Set.of("watermark","coordinates","fps","player-info","world-info");
    }
}
