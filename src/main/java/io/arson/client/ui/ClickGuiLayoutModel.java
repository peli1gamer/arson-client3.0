package io.arson.client.ui;

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
        int categoryRowHeight=Math.max(18,Math.min(28,Math.max(18,(rail.height()-4)/categoryRows)));
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

    public static boolean pairwiseNonIntersecting(List<Rect> rects){
        for(int i=0;i<rects.size();i++)for(int j=i+1;j<rects.size();j++)if(rects.get(i).intersects(rects.get(j)))return false;
        return true;
    }
    public static int moveFocus(Focus focus,int current,int delta,int categoryCount,int moduleCount){int count=switch(focus){case CATEGORY->categoryCount;case MODULE->moduleCount;default->1;};if(count<=0)return -1;return Math.floorMod(current+delta,count);}
    public static int moveModule(int current,int delta,int moduleCount,int columns){if(moduleCount<=0)return -1;int cols=Math.max(1,columns);if(delta==1||delta==-1)return Math.floorMod(current+delta,moduleCount);return Math.max(0,Math.min(moduleCount-1,current+delta*cols));}
    public static <T>T safeGet(List<T> values,int index){return values==null||index<0||index>=values.size()?null:values.get(index);}
    private static double clamp(double value,double min,double max){return Double.isNaN(value)||Double.isInfinite(value)?min:Math.max(min,Math.min(max,value));}
    private static final class ModuleCategoryCountHolder { private static final int COUNT=6; }
}
