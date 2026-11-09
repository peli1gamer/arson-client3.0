package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.ui.NoOverlapLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;

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

    // Last measured logical bounds for text HUD elements; Array List bounds are screen-space.
    private final Map<String, NoOverlapLayout.Size> measuredSizes = new LinkedHashMap<>();

    public HudLayoutModule() {
        super("hud-layout", "HUD Layout", Category.RENDER, "Keeps HUD element anchors and offsets persistent across viewport resizes.");
        clamp.description("Keep anchored HUD elements inside the visible scaled viewport.");
    }

    private DoubleSetting offset(String id, String name, double value) {
        return setting(new DoubleSetting(id, name, value, -1000.0, 2000.0, 1.0));
    }

    public boolean clamp() { return clamp.enabled(); }

    /** Records actual rendered bounds so overlap repair uses content size, not placeholders. */
    public boolean updateElementSize(String element, double width, double height) {
        if (element == null || !List.of("watermark", "coordinates", "fps", "player-info", "world-info", "array-list").contains(element)) return false;
        NoOverlapLayout.Size next = new NoOverlapLayout.Size(safeDimension(width), safeDimension(height));
        NoOverlapLayout.Size previous = measuredSizes.put(element, next);
        return !next.equals(previous);
    }

    /** Returns the last measured bounds, or a safe fallback before the first HUD render. */
    public NoOverlapLayout.Size measuredSize(String element, double fallbackWidth, double fallbackHeight) {
        return measuredSizes.getOrDefault(element,
                new NoOverlapLayout.Size(safeDimension(fallbackWidth), safeDimension(fallbackHeight)));
    }

    /** Stable signature used to schedule overlap repair after measured bounds change. */
    public int boundsSignature() {
        int hash = 1;
        for (String element : List.of("watermark", "coordinates", "fps", "player-info", "world-info", "array-list")) {
            NoOverlapLayout.Size size = measuredSizes.get(element);
            if (size != null) {
                hash = 31 * hash + element.hashCode();
                hash = 31 * hash + size.width();
                hash = 31 * hash + size.height();
            }
        }
        return hash;
    }

    private static int safeDimension(double value) {
        if (!Double.isFinite(value)) return 1;
        return Math.max(1, Math.min(10000, (int) Math.ceil(value)));
    }
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


    /** Compatibility entry point for tests and older integrations. */
    public boolean repairNoOverlap(int viewportWidth,int viewportHeight,double globalScale,int arrayListWidth,int arrayListHeight,ArrayListModule arrayList) {
        return repairNoOverlap(viewportWidth,viewportHeight,globalScale,arrayListWidth,arrayListHeight,arrayList,new HudModule());
    }

    /** Repairs legacy/off-screen/overlapping HUD positions while preserving each element's anchor. */
    public boolean repairNoOverlap(int viewportWidth,int viewportHeight,double globalScale,int arrayListWidth,int arrayListHeight,ArrayListModule arrayList,HudModule hud) {
        return repairNoOverlap(viewportWidth, viewportHeight, globalScale, arrayListWidth, arrayListHeight,
                arrayList, hud, null);
    }

    /**
     * Repairs only the HUD elements that are actually rendered. The HUD module's own
     * visibility toggles are necessary but not sufficient when a child info module is disabled.
     */
    public boolean repairNoOverlap(int viewportWidth, int viewportHeight, double globalScale,
                                   int arrayListWidth, int arrayListHeight, ArrayListModule arrayList,
                                   HudModule hud, Set<String> activeElements) {
        if(hud==null) return false;
        double scale=Math.max(0.5,globalScale);
        int logicalW=Math.max(1,(int)Math.floor(viewportWidth/scale));
        int logicalH=Math.max(1,(int)Math.floor(viewportHeight/scale));
        List<String> order=List.of("watermark","coordinates","fps","player-info","world-info","array-list");
        Map<String,NoOverlapLayout.Rect> preferred=new LinkedHashMap<>();
        Map<String,NoOverlapLayout.Size> sizes=new LinkedHashMap<>();
        for(String element:order){
            if("array-list".equals(element)){
                if(arrayList == null || !arrayList.enabled()) continue;
                // Convert the Array List's edge anchor into its visible bounding rectangle.
                NoOverlapLayout.Size measured = measuredSizes.getOrDefault(element,
                        new NoOverlapLayout.Size(arrayListWidth, arrayListHeight));
                int actualWidth = measured.width(), actualHeight = measured.height();
                int padding=(int)Math.round(arrayList.padding()*arrayList.scale());
                int preferredX=arrayList.rightAlign()
                        ? viewportWidth-arrayList.x()-actualWidth+padding
                        : arrayList.x()-padding;
                int preferredY=arrayList.y()-padding;
                preferred.put(element,new NoOverlapLayout.Rect(Math.max(0,preferredX),Math.max(0,preferredY),actualWidth,actualHeight));
                sizes.put(element,measured);
                continue;
            }
            if (!(activeElements == null ? hud.elementVisible(element) : activeElements.contains(element))) continue;
            double elementScale=hud.elementScale(element);
            NoOverlapLayout.Size measured = measuredSizes.getOrDefault(element,
                    new NoOverlapLayout.Size(safeDimension(190 * elementScale), safeDimension(35 * elementScale)));
            double[] p=resolve(element,logicalW,logicalH,measured.width(),measured.height());
            int x=(int)Math.round(p[0]*scale),y=(int)Math.round(p[1]*scale);
            int w=Math.max(1,(int)Math.ceil(measured.width()*scale)),h=Math.max(1,(int)Math.ceil(measured.height()*scale));
            preferred.put(element,new NoOverlapLayout.Rect(x,y,w,h));
            sizes.put(element,new NoOverlapLayout.Size(w,h));
        }
        Map<String,NoOverlapLayout.Placement> placements=NoOverlapLayout.solve(viewportWidth,viewportHeight,new NoOverlapLayout.Insets(6,6,6,6),6,preferred,sizes,order.stream().filter(preferred::containsKey).toList());
        boolean changed=false;
        for(String element:placements.keySet()){
            NoOverlapLayout.Rect rect=placements.get(element).rect();
            if("array-list".equals(element)){
                if(arrayList != null){
                    NoOverlapLayout.Size measured = measuredSizes.getOrDefault(element,
                            new NoOverlapLayout.Size(arrayListWidth, arrayListHeight));
                    int padding=(int)Math.round(arrayList.padding()*arrayList.scale());
                    int targetX=arrayList.rightAlign()
                            ? viewportWidth-rect.x()-measured.width()+padding
                            : rect.x()+padding;
                    int targetY=rect.y()+padding;
                    if(arrayList.x()!=targetX || arrayList.y()!=targetY){ arrayList.setEditorPosition(targetX,targetY); changed=true; }
                }
                continue;
            }
            double x=rect.x()/scale,y=rect.y()/scale;
            NoOverlapLayout.Size measured = measuredSizes.getOrDefault(element,
                    new NoOverlapLayout.Size(safeDimension(190 * hud.elementScale(element)), safeDimension(35 * hud.elementScale(element))));
            double[] current=resolve(element,logicalW,logicalH,measured.width(),measured.height());
            if(Math.abs(current[0]-x)>0.5 || Math.abs(current[1]-y)>0.5){
                setPositionPreservingAnchor(element,x,y,logicalW,logicalH,measured.width(),measured.height());
                changed=true;
            }
        }
        return changed;
    }

    private int arrayListPreferredX=6,arrayListPreferredY=6;
    public void setArrayListPreferredPosition(int x,int y){arrayListPreferredX=x;arrayListPreferredY=y;}

    @Override protected void onTick(MinecraftClient client) { /* layout is declarative; no per-tick work is required */ }
}
