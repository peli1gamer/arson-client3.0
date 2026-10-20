package io.arson.client.ui;

import java.util.ArrayList;
import java.util.List;

/** Pure geometry/navigation model for the responsive Arson ClickGUI. */
public final class ClickGuiLayoutModel {
    public enum Mode { FULL, COMPACT, NARROW }
    public enum Focus { SEARCH, CATEGORY, MODULE, ACTION }
    public record Rect(int x,int y,int width,int height){
        public int right(){return x+width;} public int bottom(){return y+height;}
        public boolean contains(double px,double py){return px>=x&&px<right()&&py>=y&&py<bottom();}
        public boolean intersects(Rect other){return other!=null&&x<other.right()&&right()>other.x&&y<other.bottom()&&bottom()>other.y;}
    }
    public record CardActionBounds(Rect details, Rect toggle) {}
    public record ModuleCardRegions(Rect details, Rect favorite, Rect toggle) {}
    public record Geometry(Mode mode,int panelX,int panelY,int panelWidth,int panelHeight,Rect rail,Rect search,Rect toolbar,
                           Rect content,Rect moduleList,Rect detail,Rect footer,int columns,int cardWidth,int cardHeight,int cardGap,
                           int safeMargin,int categoryRowHeight){
        public Rect moduleCard(int index,int visibleCount){
            if(columns<=0)return new Rect(moduleList.x(),moduleList.y(),0,0);
            int row=index/columns,col=index%columns;
            int h=Math.max(0,Math.min(cardHeight,moduleList.height()));
            return new Rect(moduleList.x()+col*(cardWidth+cardGap),moduleList.y()+row*(h+cardGap),cardWidth,h);
        }
        public int visibleRows(int visibleCount){return Math.max(1,(visibleCount+columns-1)/columns);}
    }
    private ClickGuiLayoutModel(){}

    public static Geometry compute(int sw,int sh,double requestedScale){
        int safe=sw<520||sh<360?6:12;
        Mode mode=sw<760||sh<520?Mode.NARROW:sw<1100||sh<700?Mode.COMPACT:Mode.FULL;
        double scale=clamp(requestedScale,.72,mode==Mode.NARROW?1.0:mode==Mode.COMPACT?1.10:1.25);
        int targetW=(int)Math.round(900*scale),targetH=(int)Math.round(540*scale);
        int availableW=Math.max(260,sw-safe*2),availableH=Math.max(220,sh-safe*2);
        int pw=Math.min(targetW,availableW),ph=Math.min(targetH,availableH);
        int x=Math.max(safe,(sw-pw)/2),y=Math.max(safe,(sh-ph)/2);
        int pad=mode==Mode.NARROW?7:12,gap=mode==Mode.NARROW?7:10,header=36,footerH=mode==Mode.NARROW?48:42;
        int railW=mode==Mode.NARROW?62:mode==Mode.COMPACT?132:150;
        railW=Math.min(railW,Math.max(52,pw/3));
        int contentX=x+pad+railW+gap;
        int contentW=Math.max(100,x+pw-pad-contentX);
        Rect search=new Rect(contentX,y+7,contentW,21);
        Rect toolbar=new Rect(contentX,search.bottom()+4,contentW,22);
        int contentY=toolbar.bottom()+7;
        int contentH=Math.max(70,y+ph-footerH-pad-contentY);
        Rect content=new Rect(contentX,contentY,contentW,contentH);
        Rect rail=new Rect(x+pad,contentY,Math.max(46,railW-pad),contentH);
        int categoryRows=Math.max(1,ModuleCategoryCountHolder.COUNT);
        int categoryRowHeight=Math.max(1,Math.min(28,Math.max(1,(rail.height()-4)/categoryRows)));
        int cols=mode==Mode.NARROW?1:contentW>=540?2:1,cardGap=mode==Mode.NARROW?6:9;
        Rect list,detail;
        if(cols==2){
            int listW=Math.max(100,(contentW-cardGap)*5/10);
            list=new Rect(content.x(),content.y(),listW,content.height());
            int dx=list.right()+cardGap;
            detail=new Rect(dx,content.y(),Math.max(80,content.right()-dx),content.height());
        }else{
            int listH=Math.max(52,Math.min(content.height()/2,mode==Mode.NARROW?150:190));
            list=new Rect(content.x(),content.y(),content.width(),listH);
            int dy=list.bottom()+cardGap;
            detail=new Rect(content.x(),dy,content.width(),Math.max(40,content.bottom()-dy));
        }
        Rect footer=new Rect(x+pad,y+ph-footerH-pad/2,Math.max(80,pw-pad*2),footerH);
        int cardW=Math.max(70,(list.width()-cardGap*(cols-1))/cols);
        int cardH=mode==Mode.NARROW?40:54;
        return new Geometry(mode,x,y,pw,ph,rail,search,toolbar,content,list,detail,footer,cols,cardW,cardH,cardGap,safe,categoryRowHeight);
    }

    /** Splits a module card into a details target and a non-overlapping direct toggle target. */
    public static CardActionBounds moduleCardActions(Rect card, int requestedToggleWidth, int gap) {
        if (card == null || card.width() <= 0 || card.height() <= 0) {
            Rect empty = new Rect(0, 0, 0, 0);
            return new CardActionBounds(empty, empty);
        }
        int toggleWidth = Math.min(Math.max(1, requestedToggleWidth), Math.max(1, card.width() - 1));
        int spacing = Math.min(Math.max(0, gap), Math.max(0, card.width() - toggleWidth - 1));
        int detailsWidth = card.width() - toggleWidth - spacing;
        return new CardActionBounds(
                new Rect(card.x(), card.y(), detailsWidth, card.height()),
                new Rect(card.x() + detailsWidth + spacing, card.y(), toggleWidth, card.height()));
    }

    /** Splits the details side of a card into separate details and favorite hit regions. */
    public static ModuleCardRegions moduleCardRegions(Rect card, int requestedToggleWidth,
                                                       int requestedFavoriteWidth, int gap) {
        CardActionBounds actions = moduleCardActions(card, requestedToggleWidth, gap);
        Rect detailsSide = actions.details();
        if (detailsSide.width() <= 1 || detailsSide.height() <= 0) {
            Rect empty = new Rect(detailsSide.right(), detailsSide.y(), 0, Math.max(0, detailsSide.height()));
            return new ModuleCardRegions(detailsSide, empty, actions.toggle());
        }

        int favoriteWidth = Math.min(Math.max(1, requestedFavoriteWidth), detailsSide.width() - 1);
        int spacing = Math.min(Math.max(0, gap), detailsSide.width() - favoriteWidth - 1);
        int detailsWidth = detailsSide.width() - favoriteWidth - spacing;
        return new ModuleCardRegions(
                new Rect(detailsSide.x(), detailsSide.y(), detailsWidth, detailsSide.height()),
                new Rect(detailsSide.x() + detailsWidth + spacing, detailsSide.y(), favoriteWidth, detailsSide.height()),
                actions.toggle());
    }

    /** Builds a bounded grid of module cards without allowing overlap or viewport overflow. */
    public static List<Rect> gridCards(Rect area, int itemCount, int requestedColumns, int requestedCardHeight, int gap) {
        if (area == null || itemCount <= 0 || area.width() <= 0 || area.height() <= 0) return List.of();
        int spacing = Math.max(0, gap);
        int columns = Math.max(1, requestedColumns);
        columns = Math.min(columns, Math.max(1, (area.width() + spacing) / (spacing + 1)));
        int cardWidth = Math.max(1, (area.width() - spacing * (columns - 1)) / columns);
        int cardHeight = Math.max(1, Math.min(requestedCardHeight, area.height()));
        int rows = Math.max(1, (area.height() + spacing) / (cardHeight + spacing));
        int count = Math.min(itemCount, rows * columns);
        ArrayList<Rect> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int row = i / columns, column = i % columns;
            cards.add(new Rect(area.x() + column * (cardWidth + spacing),
                    area.y() + row * (cardHeight + spacing), cardWidth, cardHeight));
        }
        return List.copyOf(cards);
    }

    /** Maximum row-aligned item offset for a vertically scrolling module grid. */
    public static int maxGridScroll(int itemCount, int columns, int viewportHeight, int cardHeight, int gap) {
        if (itemCount <= 0 || viewportHeight <= 0) return 0;
        int cols = Math.max(1, columns);
        int spacing = Math.max(0, gap);
        int cardStep = Math.max(1, cardHeight) + spacing;
        int visibleRows = Math.max(1, (viewportHeight + spacing) / cardStep);
        int totalRows = (itemCount + cols - 1) / cols;
        return Math.max(0, (totalRows - visibleRows) * cols);
    }

    /**
     * Returns the thumb for a proportional vertical scrollbar, or null when the viewport has no overflow.
     * The scroll offset is measured in pixels and is safely clamped to the scrollable extent.
     */
    public static Rect scrollbarThumb(Rect viewport, int contentHeight, int scrollOffset,
                                      int minThumbHeight, int width) {
        if (viewport == null || viewport.width() <= 0 || viewport.height() <= 0
                || contentHeight <= viewport.height()) return null;
        int thumbWidth = Math.min(viewport.width(), Math.max(1, width));
        int trackHeight = viewport.height();
        int thumbHeight = (int) Math.max(Math.max(1, minThumbHeight),
                (long) trackHeight * trackHeight / contentHeight);
        thumbHeight = Math.min(trackHeight, thumbHeight);
        int maxOffset = contentHeight - trackHeight;
        int clampedOffset = Math.max(0, Math.min(maxOffset, scrollOffset));
        int travel = trackHeight - thumbHeight;
        int thumbY = viewport.y() + (int) Math.round((double) travel * clampedOffset / maxOffset);
        return new Rect(viewport.right() - thumbWidth, thumbY, thumbWidth, thumbHeight);
    }

    /** Maps a requested thumb top back to a clamped content-pixel offset. */
    public static int scrollOffsetForThumbPosition(Rect viewport, int contentHeight, double desiredThumbTop,
                                                   int minThumbHeight, int width) {
        Rect thumb = scrollbarThumb(viewport, contentHeight, 0, minThumbHeight, width);
        if (thumb == null) return 0;
        int travel = viewport.height() - thumb.height();
        if (travel <= 0) return 0;
        int maxOffset = contentHeight - viewport.height();
        double clampedTop = Math.max(viewport.y(),
                Math.min(viewport.bottom() - thumb.height(), desiredThumbTop));
        return (int) Math.round((clampedTop - viewport.y()) * maxOffset / travel);
    }

    /** Pages through a module grid while keeping the offset row-aligned and within its scroll range. */
    public static int pageGridScroll(int currentOffset, int direction, int itemCount, int columns,
                                     int viewportHeight, int cardHeight, int gap) {
        int cols = Math.max(1, columns);
        int spacing = Math.max(0, gap);
        int visibleRows = Math.max(1, (Math.max(0, viewportHeight) + spacing)
                / (Math.max(1, cardHeight) + spacing));
        int pageItems = Math.max(1, visibleRows - 1) * cols;
        int maximum = maxGridScroll(itemCount, cols, viewportHeight, cardHeight, spacing);
        long next = (long) currentOffset + (long) Integer.signum(direction) * pageItems;
        return (int) Math.max(0, Math.min(maximum, next));
    }

    /** Resolves saved grid density against the current viewport; narrow layouts always remain single-column. */
    public static int effectiveColumns(Mode mode, int contentWidth, int requestedColumns) {
        if (mode == Mode.NARROW) return 1;
        int available = Math.max(1, Math.min(2, (Math.max(0, contentWidth) + 8) / 220));
        return requestedColumns <= 0 ? available : Math.min(available, Math.max(1, requestedColumns));
    }

    public static boolean pairwiseNonIntersecting(List<Rect> rects){
        for(int i=0;i<rects.size();i++)for(int j=i+1;j<rects.size();j++)if(rects.get(i).intersects(rects.get(j)))return false;
        return true;
    }
    public static int moveFocus(Focus focus,int current,int delta,int categoryCount,int moduleCount){int count=switch(focus){case CATEGORY->categoryCount;case MODULE->moduleCount;default->1;};if(count<=0)return -1;return Math.floorMod(current+delta,count);}
    public static int moveModule(int current,int delta,int moduleCount,int columns){if(moduleCount<=0)return -1;int cols=Math.max(1,columns);if(delta==1||delta==-1)return Math.floorMod(current+delta,moduleCount);return Math.max(0,Math.min(moduleCount-1,current+delta*cols));}
    public static <T>T safeGet(List<T> values,int index){return values==null||index<0||index>=values.size()?null:values.get(index);}
    private static double clamp(double value,double min,double max){return Double.isNaN(value)||Double.isInfinite(value)?min:Math.max(min,Math.min(max,value));}
    private static final class ModuleCategoryCountHolder { private static final int COUNT=7; }
}
