package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import net.minecraft.client.Minecraft;
import io.arson.client.ui.NoOverlapLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persistent, resize-aware HUD anchor constraints. Positions are stored as anchor-relative offsets. */
public final class HudLayoutModule extends Module {
    public enum Anchor { TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }

    private final BooleanSetting clamp = setting(new BooleanSetting("clamp", "Clamp To Viewport", true));
    private final EnumSetting<Anchor> watermarkAnchor = setting(new EnumSetting<>("watermark-anchor", "Watermark Anchor", Anchor.TOP_LEFT));
    private final EnumSetting<Anchor> coordinatesAnchor = setting(new EnumSetting<>("coordinates-anchor", "Coordinates Anchor", Anchor.TOP_LEFT));
    private final EnumSetting<Anchor> fpsAnchor = setting(new EnumSetting<>("fps-anchor", "FPS Anchor", Anchor.TOP_LEFT));
    private final EnumSetting<Anchor> playerInfoAnchor = setting(new EnumSetting<>("player-info-anchor", "Player Info Anchor", Anchor.TOP_LEFT));
    private final EnumSetting<Anchor> worldInfoAnchor = setting(new EnumSetting<>("world-info-anchor", "World Info Anchor", Anchor.TOP_LEFT));

    private final DoubleSetting watermarkX = offset("watermark-x", "Watermark X", 6);
    private final DoubleSetting watermarkY = offset("watermark-y", "Watermark Y", 6);
    private final DoubleSetting coordinatesX = offset("coordinates-x", "Coordinates X", 6);
    private final DoubleSetting coordinatesY = offset("coordinates-y", "Coordinates Y", 28);
    private final DoubleSetting fpsX = offset("fps-x", "FPS X", 6);
    private final DoubleSetting fpsY = offset("fps-y", "FPS Y", 39);
    private final DoubleSetting playerInfoX = offset("player-info-x", "Player Info X", 6);
    private final DoubleSetting playerInfoY = offset("player-info-y", "Player Info Y", 50);
    private final DoubleSetting worldInfoX = offset("world-info-x", "World Info X", 6);
    private final DoubleSetting worldInfoY = offset("world-info-y", "World Info Y", 94);

    public HudLayoutModule() {
        super("hud-layout", "HUD Layout", Category.RENDER, "Keeps HUD element anchors and offsets persistent across viewport resizes.");
        clamp.description("Keep anchored HUD elements inside the visible scaled viewport.");
    }

    private DoubleSetting offset(String id, String name, double value) {
        return setting(new DoubleSetting(id, name, value, -1000.0, 2000.0, 1.0));
    }

    public boolean clamp() { return clamp.enabled(); }
    public Anchor anchor(String element) { return switch (element) {
        case "watermark" -> watermarkAnchor.get(); case "coordinates" -> coordinatesAnchor.get(); case "fps" -> fpsAnchor.get();
        case "player-info" -> playerInfoAnchor.get(); case "world-info" -> worldInfoAnchor.get(); default -> Anchor.TOP_LEFT;
    }; }

    public double offsetX(String element) { return switch (element) {
        case "watermark" -> watermarkX.get(); case "coordinates" -> coordinatesX.get(); case "fps" -> fpsX.get();
        case "player-info" -> playerInfoX.get(); case "world-info" -> worldInfoX.get(); default -> 0;
    }; }
    public double offsetY(String element) { return switch (element) {
        case "watermark" -> watermarkY.get(); case "coordinates" -> coordinatesY.get(); case "fps" -> fpsY.get();
        case "player-info" -> playerInfoY.get(); case "world-info" -> worldInfoY.get(); default -> 0;
    }; }

    public void setAnchor(String element, Anchor value) { switch (element) {
        case "watermark" -> watermarkAnchor.set(value); case "coordinates" -> coordinatesAnchor.set(value); case "fps" -> fpsAnchor.set(value);
        case "player-info" -> playerInfoAnchor.set(value); case "world-info" -> worldInfoAnchor.set(value); default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
    } }
    public void setOffset(String element, double x, double y) { setOffsetX(element, x); setOffsetY(element, y); }
    private void setOffsetX(String element, double value) { switch (element) {
        case "watermark" -> watermarkX.set(value); case "coordinates" -> coordinatesX.set(value); case "fps" -> fpsX.set(value);
        case "player-info" -> playerInfoX.set(value); case "world-info" -> worldInfoX.set(value); default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
    } }
    private void setOffsetY(String element, double value) { switch (element) {
        case "watermark" -> watermarkY.set(value); case "coordinates" -> coordinatesY.set(value); case "fps" -> fpsY.set(value);
        case "player-info" -> playerInfoY.set(value); case "world-info" -> worldInfoY.set(value); default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
    } }

    public double[] resolve(String element, double viewportWidth, double viewportHeight, double boxWidth, double boxHeight) {
        Anchor a = anchor(element);
        double baseX = switch (a) { case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0; case TOP_CENTER, CENTER, BOTTOM_CENTER -> (viewportWidth - boxWidth) / 2.0; case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> viewportWidth - boxWidth; };
        double baseY = switch (a) { case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0; case CENTER_LEFT, CENTER, CENTER_RIGHT -> (viewportHeight - boxHeight) / 2.0; case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> viewportHeight - boxHeight; };
        double x = baseX + offsetX(element), y = baseY + offsetY(element);
        if (clamp()) { x = Math.max(0, Math.min(viewportWidth - Math.max(0, boxWidth), x)); y = Math.max(0, Math.min(viewportHeight - Math.max(0, boxHeight), y)); }
        return new double[]{x, y};
    }

    public void setPositionPreservingAnchor(String element, double x, double y, double viewportWidth, double viewportHeight, double boxWidth, double boxHeight) {
        Anchor a = anchor(element);
        double baseX = switch (a) { case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0; case TOP_CENTER, CENTER, BOTTOM_CENTER -> (viewportWidth - boxWidth) / 2.0; case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> viewportWidth - boxWidth; };
        double baseY = switch (a) { case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0; case CENTER_LEFT, CENTER, CENTER_RIGHT -> (viewportHeight - boxHeight) / 2.0; case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> viewportHeight - boxHeight; };
        setOffset(element, x - baseX, y - baseY);
    }

    public void setAnchorPreservingPosition(String element, Anchor value, double currentX, double currentY, double viewportWidth, double viewportHeight, double boxWidth, double boxHeight) {
        setAnchor(element, value);
        setPositionPreservingAnchor(element, currentX, currentY, viewportWidth, viewportHeight, boxWidth, boxHeight);
    }

    public void resetElement(String element) {
        switch (element) {
            case "watermark" -> { watermarkAnchor.reset(); watermarkX.reset(); watermarkY.reset(); }
            case "coordinates" -> { coordinatesAnchor.reset(); coordinatesX.reset(); coordinatesY.reset(); }
            case "fps" -> { fpsAnchor.reset(); fpsX.reset(); fpsY.reset(); }
            case "player-info" -> { playerInfoAnchor.reset(); playerInfoX.reset(); playerInfoY.reset(); }
            case "world-info" -> { worldInfoAnchor.reset(); worldInfoX.reset(); worldInfoY.reset(); }
            default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
        }
    }


    /** Repairs legacy/off-screen/overlapping HUD positions while preserving each element's anchor. */
    public boolean repairNoOverlap(int viewportWidth,int viewportHeight,double globalScale,int arrayListWidth,int arrayListHeight,ArrayListModule arrayList) {
        double scale=Math.max(0.5,globalScale);
        int logicalW=Math.max(1,(int)Math.floor(viewportWidth/scale));
        int logicalH=Math.max(1,(int)Math.floor(viewportHeight/scale));
        List<String> order=List.of("watermark","coordinates","fps","player-info","world-info","array-list");
        Map<String,NoOverlapLayout.Rect> preferred=new LinkedHashMap<>();
        Map<String,NoOverlapLayout.Size> sizes=new LinkedHashMap<>();
        for(String element:order){
            if("array-list".equals(element)){
                if(arrayList == null || !arrayList.enabled()) continue;
                preferred.put(element,new NoOverlapLayout.Rect(0,0,arrayListWidth,arrayListHeight));
                // Array-list coordinates are already in the raw viewport space.
                preferred.put(element,new NoOverlapLayout.Rect(Math.max(0,arrayListPreferredX),Math.max(0,arrayListPreferredY),arrayListWidth,arrayListHeight));
                sizes.put(element,new NoOverlapLayout.Size(arrayListWidth,arrayListHeight));
                continue;
            }
            double elementScale=elementScaleHint(element,viewportWidth,viewportHeight);
            double[] p=resolve(element,logicalW,logicalH,190*elementScale,35*elementScale);
            int x=(int)Math.round(p[0]*scale),y=(int)Math.round(p[1]*scale);
            int w=Math.max(1,(int)Math.ceil(190*elementScale*scale)),h=Math.max(1,(int)Math.ceil(35*elementScale*scale));
            preferred.put(element,new NoOverlapLayout.Rect(x,y,w,h));
            sizes.put(element,new NoOverlapLayout.Size(w,h));
        }
        Map<String,NoOverlapLayout.Placement> placements=NoOverlapLayout.solve(viewportWidth,viewportHeight,new NoOverlapLayout.Insets(6,6,6,6),6,preferred,sizes,order.stream().filter(preferred::containsKey).toList());
        boolean changed=false;
        for(String element:placements.keySet()){
            NoOverlapLayout.Rect rect=placements.get(element).rect();
            if("array-list".equals(element)){
                int targetX=rect.x(),targetY=rect.y();
                if(arrayList != null && (arrayList.x()!=targetX || arrayList.y()!=targetY)){ arrayList.setEditorPosition(targetX,targetY); changed=true; }
                continue;
            }
            double x=rect.x()/scale,y=rect.y()/scale;
            if(Math.abs(resolve(element,logicalW,logicalH,190*elementScaleHint(element,viewportWidth,viewportHeight),35*elementScaleHint(element,viewportWidth,viewportHeight))[0]-x)>0.5 || Math.abs(resolve(element,logicalW,logicalH,190*elementScaleHint(element,viewportWidth,viewportHeight),35*elementScaleHint(element,viewportWidth,viewportHeight))[1]-y)>0.5){
                setPositionPreservingAnchor(element,x,y,logicalW,logicalH,190*elementScaleHint(element),35*elementScaleHint(element));
                changed=true;
            }
        }
        return changed;
    }

    private int arrayListPreferredX=6,arrayListPreferredY=6;
    public void setArrayListPreferredPosition(int x,int y){arrayListPreferredX=x;arrayListPreferredY=y;}
    private double elementScaleHint(String element,int viewportWidth,int viewportHeight){return viewportWidth<520||viewportHeight<360?1.0:2.0;}

    @Override protected void onTick(Minecraft client) { /* layout is declarative; no per-tick work is required */ }
}
