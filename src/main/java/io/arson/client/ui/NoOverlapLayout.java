package io.arson.client.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
                chosen=findCandidate(chosen,w,h,safeLeft,safeTop,safeRight,safeBottom,step,occupied);
            }
            boolean moved=chosen.x()!=pref.x()||chosen.y()!=pref.y();
            result.put(id,new Placement(id,chosen,moved,compacted));
            occupied.add(chosen);
        }
        return result;
    }

    private static Rect findCandidate(Rect preferred,int w,int h,int left,int top,int right,int bottom,int gap,List<Rect> occupied){
        List<Rect> candidates=new ArrayList<>();
        candidates.add(new Rect(preferred.x(),clamp(preferred.bottom()+gap,top,bottom-h),w,h));
        candidates.add(new Rect(clamp(preferred.right()+gap,left,right-w),preferred.y(),w,h));
        candidates.add(new Rect(left,top,w,h));
        int maxY=Math.max(top,bottom-h),maxX=Math.max(left,right-w);
        for(int y=top;y<=maxY;y+=gap){
            for(int x=left;x<=maxX;x+=gap){
                candidates.add(new Rect(x,y,w,h));
            }
        }
        Rect best=null; long bestDistance=Long.MAX_VALUE;
        for(Rect candidate:candidates){
            if(conflicts(candidate,occupied)) continue;
            long distance=Math.abs((long)candidate.x()-preferred.x())+Math.abs((long)candidate.y()-preferred.y());
            if(distance<bestDistance || (distance==bestDistance && (best==null || candidate.y()<best.y() || candidate.y()==best.y()&&candidate.x()<best.x()))){
                best=candidate;bestDistance=distance;
            }
        }
        return best==null ? new Rect(left,top,w,h) : best;
    }

    private static boolean conflicts(Rect candidate,List<Rect> occupied){
        for(Rect rect:occupied) if(candidate.intersects(rect)) return true;
        return false;
    }
    private static int clamp(int value,int min,int max){ return Math.max(min,Math.min(max,value)); }
}
