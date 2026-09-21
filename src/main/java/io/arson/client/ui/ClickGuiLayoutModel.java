package io.arson.client.ui;
import java.util.List;
/** Pure geometry/navigation model for the responsive Arson ClickGUI. */
public final class ClickGuiLayoutModel {
 public enum Mode { FULL, COMPACT, NARROW }
 public enum Focus { SEARCH, CATEGORY, MODULE, ACTION }
 public record Rect(int x,int y,int width,int height){public int right(){return x+width;}public int bottom(){return y+height;}public boolean contains(double px,double py){return px>=x&&px<right()&&py>=y&&py<bottom();}}
 public record Geometry(Mode mode,int panelX,int panelY,int panelWidth,int panelHeight,Rect rail,Rect search,Rect content,Rect moduleList,Rect detail,Rect footer,int columns,int cardWidth,int cardHeight,int cardGap,int safeMargin){
  public Rect moduleCard(int index,int visibleCount){if(columns<=0)return new Rect(moduleList.x(),moduleList.y(),0,0);int row=index/columns,col=index%columns;int h=Math.max(0,Math.min(cardHeight,moduleList.height()));return new Rect(moduleList.x()+col*(cardWidth+cardGap),moduleList.y()+row*(h+cardGap),cardWidth,h);}
  public int visibleRows(int visibleCount){return Math.max(1,(visibleCount+columns-1)/columns);}
 }
 private ClickGuiLayoutModel(){}
 public static Geometry compute(int sw,int sh,double requestedScale){
  int safe=sw<520||sh<360?6:12;Mode mode=sw<760||sh<520?Mode.NARROW:sw<1100||sh<700?Mode.COMPACT:Mode.FULL;
  double scale=clamp(requestedScale,.72,mode==Mode.NARROW?1.0:mode==Mode.COMPACT?1.10:1.25);
  int targetW=(int)Math.round(900*scale),targetH=(int)Math.round(540*scale),availableW=Math.max(260,sw-safe*2),availableH=Math.max(220,sh-safe*2);
  int pw=Math.min(targetW,availableW),ph=Math.min(targetH,availableH),x=Math.max(safe,(sw-pw)/2),y=Math.max(safe,(sh-ph)/2);
  int railW=mode==Mode.NARROW?76:mode==Mode.COMPACT?142:158,pad=mode==Mode.NARROW?8:12,gap=mode==Mode.NARROW?7:10,header=mode==Mode.NARROW?38:46,footerH=mode==Mode.NARROW?34:42;
  railW=Math.min(railW,Math.max(58,pw/3));
  Rect rail=new Rect(x+pad,y+header,Math.max(50,railW-pad),Math.max(40,ph-header-footerH-pad));
  int cx=rail.right()+gap,cw=Math.max(100,x+pw-pad-cx),cy=y+header,ch=Math.max(40,ph-header-footerH);Rect content=new Rect(cx,cy,cw,ch);
  int cols=mode==Mode.NARROW?1:cw>=540?2:1,cardGap=mode==Mode.NARROW?7:9,listW=cols==2?Math.max(90,(cw-cardGap)*5/10):cw;
  int dx=cols==2?cx+listW+cardGap:cx,dw=cols==2?Math.max(80,cw-listW-cardGap):cw,dy=cols==2?cy:cy+Math.min(120,ch/3),dh=cols==2?ch:Math.max(40,ch-(dy-cy));
  Rect list=new Rect(cx,cy+34,listW,Math.max(40,ch-34)),detail=new Rect(dx,dy,dw,dh),search=new Rect(cx,y+9,Math.max(80,cw),mode==Mode.NARROW?20:24),foot=new Rect(x+pad,y+ph-footerH-pad/2,Math.max(80,pw-pad*2),footerH);
  int cardW=Math.max(70,(listW-cardGap*(cols-1))/cols),cardH=mode==Mode.NARROW?42:56;
  return new Geometry(mode,x,y,pw,ph,rail,search,content,list,detail,foot,cols,cardW,cardH,cardGap,safe);
 }
 public static int moveFocus(Focus focus,int current,int delta,int categoryCount,int moduleCount){int count=switch(focus){case CATEGORY->categoryCount;case MODULE->moduleCount;default->1;};if(count<=0)return -1;return Math.floorMod(current+delta,count);}
 public static int moveModule(int current,int delta,int moduleCount,int columns){if(moduleCount<=0)return -1;int cols=Math.max(1,columns);if(delta==1||delta==-1)return Math.floorMod(current+delta,moduleCount);return Math.max(0,Math.min(moduleCount-1,current+delta*cols));}
 public static <T>T safeGet(List<T> values,int index){return values==null||index<0||index>=values.size()?null:values.get(index);}
 private static double clamp(double value,double min,double max){return Double.isNaN(value)||Double.isInfinite(value)?min:Math.max(min,Math.min(max,value));}
}
