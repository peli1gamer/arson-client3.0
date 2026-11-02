package io.arson.client.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;

/** Deterministic rectangle placer used by HUD and GUI layout code. */
public final class NoOverlapLayout {
    public record Insets(int left, int top, int right, int bottom) {
        public Insets { left=Math.max(0,left); top=Math.max(0,top); right=Math.max(0,right); bottom=Math.max(0,bottom); }
    }
    public record Rect(int x, int y, int width, int height) {
        public int right(){ return x+Math.max(0,width); }
        public int bottom(){ return y+Math.max(0,height); }
        public boolean intersects(Rect other){
            return other != null && x < other.right() && right() > other.x && y < other.bottom() && bottom() > other.y;
        }
        public boolean within(int viewportWidth,int viewportHeight,Insets insets){
            return x >= insets.left() && y >= insets.top()
                    && right() <= Math.max(insets.left(), viewportWidth-insets.right())
                    && bottom() <= Math.max(insets.top(), viewportHeight-insets.bottom());
        }
    }
    public record Size(int width,int height) {
        public Size { width=Math.max(1,width); height=Math.max(1,height); }
    }
    public record Placement(String id,Rect rect,boolean moved,boolean compacted) {}
    private NoOverlapLayout(){}

    public static Map<String,Placement> solve(int viewportWidth,int viewportHeight,Insets insets,int gap,
                                               Map<String,Rect> preferred,Map<String,Size> sizes,List<String> order){
        int safeLeft=Math.min(Math.max(0,insets.left()),Math.max(0,viewportWidth));
        int safeTop=Math.min(Math.max(0,insets.top()),Math.max(0,viewportHeight));
        int safeRight=Math.max(safeLeft,viewportWidth-Math.max(0,insets.right()));
        int safeBottom=Math.max(safeTop,viewportHeight-Math.max(0,insets.bottom()));
        int regionWidth=Math.max(1,safeRight-safeLeft),regionHeight=Math.max(1,safeBottom-safeTop);
        int step=Math.max(1,gap);
        Map<String,Placement> result=new LinkedHashMap<>();
        List<Rect> occupied=new ArrayList<>();
        for(String id:order){
            Size requested=sizes.getOrDefault(id,new Size(1,1));
            int w=Math.min(requested.width(),regionWidth),h=Math.min(requested.height(),regionHeight);
            Rect pref=preferred.getOrDefault(id,new Rect(safeLeft,safeTop,w,h));
            int px=clamp(pref.x(),safeLeft,safeRight-w),py=clamp(pref.y(),safeTop,safeBottom-h);
            Rect chosen=new Rect(px,py,w,h);
            boolean compacted=w!=requested.width()||h!=requested.height();
            if(conflicts(chosen,occupied)){
                Rect candidate=findCandidate(chosen,w,h,safeLeft,safeTop,safeRight,safeBottom,step,occupied);
                if(candidate!=null) chosen=candidate;
                else {
                    Rect compactedCandidate=findCompactedCandidate(preferred.getOrDefault(id,chosen),w,h,safeLeft,safeTop,safeRight,safeBottom,step,occupied);
                    if(compactedCandidate!=null){ chosen=compactedCandidate; compacted=true; }
                    else {
                        reflowIntoRows(result, occupied, id, pref, safeLeft, safeTop,
                                safeRight, safeBottom, preferred);
                        chosen = result.get(id).rect();
                        compacted = true;
                    }
                }
            }
            boolean moved=chosen.x()!=pref.x()||chosen.y()!=pref.y();
            result.put(id,new Placement(id,chosen,moved,compacted));
            occupied.add(chosen);
        }
        return result;
    }

    private static void reflowIntoRows(Map<String,Placement> result, List<Rect> occupied,
                                        String currentId, Rect currentPreferred,
                                        int left, int top, int right, int bottom,
                                        Map<String,Rect> preferred) {
        List<String> ids = new ArrayList<>(result.keySet());
        ids.add(currentId);
        int count = ids.size();
        int regionHeight = Math.max(0, bottom - top);
        int rowHeight = count == 0 ? 0 : regionHeight / count;
        result.clear();
        occupied.clear();
        for (int i = 0; i < count; i++) {
            String id = ids.get(i);
            Rect original = id.equals(currentId) ? currentPreferred
                    : preferred.getOrDefault(id, new Rect(left, top, right-left, rowHeight));
            int y = top + i * rowHeight;
            int height = i == count - 1 ? Math.max(0, bottom - y) : rowHeight;
            Rect placed = new Rect(left, y, Math.max(0, right-left), height);
            result.put(id, new Placement(id, placed,
                    placed.x() != original.x() || placed.y() != original.y(), true));
            occupied.add(placed);
        }
    }

    private static Rect findCandidate(Rect preferred,int w,int h,int left,int top,int right,int bottom,int gap,List<Rect> occupied){
        // A nearest non-overlapping placement must touch a viewport or occupied-rectangle
        // boundary on at least one axis. Testing those boundaries avoids allocating and
        // scanning every grid cell on large (4K+) viewports.
        Set<Integer> xs = new LinkedHashSet<>();
        Set<Integer> ys = new LinkedHashSet<>();
        addCandidate(xs, preferred.x(), left, right - w);
        addCandidate(xs, preferred.right() + gap, left, right - w);
        addCandidate(xs, left, left, right - w);
        addCandidate(xs, right - w, left, right - w);
        addCandidate(ys, preferred.y(), top, bottom - h);
        addCandidate(ys, preferred.bottom() + gap, top, bottom - h);
        addCandidate(ys, top, top, bottom - h);
        addCandidate(ys, bottom - h, top, bottom - h);
        for (Rect obstacle : occupied) {
            addCandidate(xs, obstacle.right() + gap, left, right - w);
            addCandidate(xs, obstacle.x() - w - gap, left, right - w);
            addCandidate(ys, obstacle.bottom() + gap, top, bottom - h);
            addCandidate(ys, obstacle.y() - h - gap, top, bottom - h);
        }

        Rect best = null;
        long bestDistance = Long.MAX_VALUE;
        for (int y : ys) {
            for (int x : xs) {
                Rect candidate = new Rect(x, y, w, h);
                if (conflicts(candidate, occupied)) continue;
                long distance = Math.abs((long) x - preferred.x()) + Math.abs((long) y - preferred.y());
                if (distance < bestDistance || (distance == bestDistance
                        && (best == null || y < best.y() || y == best.y() && x < best.x()))) {
                    best = candidate;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    private static void addCandidate(Set<Integer> candidates, int value, int min, int max) {
        candidates.add(clamp(value, min, Math.max(min, max)));
    }

    private static Rect findCompactedCandidate(Rect preferred,int width,int height,int left,int top,int right,int bottom,int gap,List<Rect> occupied){
        double[] factors={0.85,0.70,0.55,0.40,0.25,0.15,0.08};
        for(double factor:factors){
            int w=Math.max(1,Math.min(right-left,(int)Math.floor(width*factor)));
            int h=Math.max(1,Math.min(bottom-top,(int)Math.floor(height*factor)));
            Rect candidate=findCandidate(new Rect(preferred.x(),preferred.y(),w,h),w,h,left,top,right,bottom,gap,occupied);
            if(candidate!=null)return candidate;
        }
        for(int h=Math.max(1,Math.min(height,bottom-top));h>=1;h=Math.max(1,h/2)){
            Rect candidate=findCandidate(new Rect(preferred.x(),preferred.y(),1,h),1,h,left,top,right,bottom,gap,occupied);
            if(candidate!=null)return candidate;
            if(h==1)break;
        }
        return null;
    }
    public static boolean pairwiseNonIntersecting(List<Rect> rects){
        for(int i=0;i<rects.size();i++) for(int j=i+1;j<rects.size();j++) if(rects.get(i).intersects(rects.get(j))) return false;
        return true;
    }
    private static boolean conflicts(Rect candidate,List<Rect> occupied){
        for(Rect rect:occupied) if(candidate.intersects(rect)) return true;
        return false;
    }
    private static int clamp(int value,int min,int max){ return Math.max(min,Math.min(max,value)); }
}
