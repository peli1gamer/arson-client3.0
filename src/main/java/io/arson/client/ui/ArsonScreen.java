package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.HudModule;
import io.arson.client.module.ClickGuiPreferencesModule;
import io.arson.client.module.Module;
import io.arson.client.notification.NotificationCenter;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.SettingGroup;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;

public final class ArsonScreen extends Screen {
    private enum Theme { MIDNIGHT, GRAPHITE, CONTRAST }
    private final Screen parent;
    private Module.Category category=Module.Category.RENDER;
    private Module selected;
    private Module bindingModule;
    private EditBox search;
    private EditBox profile;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private EditBox editBox;
    private final List<Button> moduleButtons=new ArrayList<>();
    private int panelX,panelY,panelW=900,panelH=540;
    private int scroll;
    private boolean favoritesOnly;
    private boolean enabledOnly;
    private Theme theme=Theme.MIDNIGHT;
    private boolean alphabetical;
    private int sidebarW;
    private int contentX;
    private int contentW;
    private int contentTop;
    private boolean compact;
    public ArsonScreen(Screen parent){super(Component.literal("Arson Client V3"));this.parent=parent;}
    @Override protected void init(){
        syncPreferences();
        ClickGuiPreferencesModule p=preferences();
        double preferredScale=p==null?1.0:p.panelScale();
        ResponsiveUiLayout.Rect panel=ResponsiveUiLayout.fitPanel(width,height,(int)Math.round(900*preferredScale),(int)Math.round(540*preferredScale),8);
        panelX=panel.x(); panelY=panel.y(); panelW=panel.width(); panelH=panel.height();
        compact=panelW<620 || panelH<420;
        sidebarW=ResponsiveUiLayout.clamp(compact ? 92 : 132,92,150);
        contentX=panelX+sidebarW+12;
        contentW=Math.max(120,panelW-sidebarW-20);
        contentTop=panelY+(compact?88:48);
        rebuild();
    }
    @Override protected void setInitialFocus(){ if(search!=null) setInitialFocus(search); else super.setInitialFocus(); }
    private ClickGuiPreferencesModule preferences(){Module m=ArsonClient.getInstance().modules().get("clickgui-preferences");return m instanceof ClickGuiPreferencesModule p?p:null;}
    private void syncPreferences(){ClickGuiPreferencesModule p=preferences();if(p==null)return;theme=Theme.valueOf(p.theme().name());alphabetical=p.alphabetical();favoritesOnly=p.favoritesOnly();enabledOnly=p.enabledOnly();}
    private void savePreferences(){ClickGuiPreferencesModule p=preferences();if(p==null)return;p.setAlphabetical(alphabetical);p.setFavoritesOnly(favoritesOnly);p.setEnabledOnly(enabledOnly);if(!p.theme().name().equals(theme.name()))p.cycleTheme();ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private int headerColor(){return switch(theme){case MIDNIGHT->0xFF181820;case GRAPHITE->0xFF202428;case CONTRAST->0xFF101010;};}
    private int accentColor(){return switch(theme){case MIDNIGHT->0xFF6C63FF;case GRAPHITE->0xFF8AA0B8;case CONTRAST->0xFFFFFFFF;};}
    private int panelColor(){return switch(theme){case MIDNIGHT->0xE00F1015;case GRAPHITE->0xE016191C;case CONTRAST->0xEE050505;};}
    private void rebuild(){
        String profileValue=profile==null?"":profile.getValue();
        String searchValue=search==null?"":search.getValue();
        clearWidgets(); moduleButtons.clear(); profile=null; editBox=null;
        if(selected==null || selected.category()!=category || (favoritesOnly&&!selected.favorite()) || (enabledOnly&&!selected.enabled()))
            selected=visibleModules().stream().findFirst().orElse(null);

        int headerY=panelY+8;
        int searchY=compact?panelY+34:headerY;
        int searchX=compact?panelX+8:panelX+145;
        int searchW=compact?panelW-16:Math.min(245,panelW-155);
        search=new EditBox(font,searchX,searchY,Math.max(90,searchW),20,Component.literal("Search modules"));
        search.setHint(Component.literal("Search modules...")); search.setValue(searchValue); addRenderableWidget(search);

        if(!compact){
            addRenderableWidget(Button.builder(Component.literal((favoritesOnly?"★ Favorites":"☆ Favorites")+" ("+ArsonClient.getInstance().modules().favoriteCount()+")"),b->{favoritesOnly=!favoritesOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+395,headerY,120,20).build());
            addRenderableWidget(Button.builder(Component.literal((enabledOnly?"● Enabled":"○ Enabled")+" ("+ArsonClient.getInstance().modules().enabledCount()+")"),b->{enabledOnly=!enabledOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+520,headerY,110,20).build());
            addRenderableWidget(Button.builder(Component.literal("Clear Filters"),b->{clearFilters();}).bounds(panelX+8,headerY,125,20).build());
            addRenderableWidget(Button.builder(Component.literal("Theme: "+theme.name()),b->{cycleTheme();}).bounds(panelX+635,headerY,105,20).build());
            addRenderableWidget(Button.builder(Component.literal(alphabetical?"Sort: A-Z":"Sort: Smart"),b->{alphabetical=!alphabetical;savePreferences();scroll=0;rebuild();}).bounds(panelX+745,headerY,95,20).build());
        } else {
            int fy=panelY+60, fw=Math.max(58,(panelW-24)/4);
            addRenderableWidget(Button.builder(Component.literal(favoritesOnly?"★ Fav":"☆ Fav"),b->{favoritesOnly=!favoritesOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+8,fy,fw,20).build());
            addRenderableWidget(Button.builder(Component.literal(enabledOnly?"● On":"○ On"),b->{enabledOnly=!enabledOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+12+fw,fy,fw,20).build());
            addRenderableWidget(Button.builder(Component.literal("Clear"),b->clearFilters()).bounds(panelX+16+fw*2,fy,fw,20).build());
            addRenderableWidget(Button.builder(Component.literal(alphabetical?"A-Z":"Smart"),b->{alphabetical=!alphabetical;savePreferences();scroll=0;rebuild();}).bounds(panelX+20+fw*3,fy,fw,20).build());
        }

        int categoryY=contentTop;
        for(Module.Category c:Module.Category.values()){
            Module.Category chosen=c;
            Button button=Button.builder(Component.literal(chosen.displayName()+" ("+ArsonClient.getInstance().modules().categoryCount(chosen)+")"),b->{category=chosen;selected=null;scroll=0;rebuild();})
                .bounds(panelX+8,categoryY,sidebarW,22).tooltip(Tooltip.create(Component.literal("Show "+chosen.displayName()+" modules"))).build();
            addRenderableWidget(button); categoryY+=25;
        }
        refreshModuleButtons();

        if(selected!=null){
            int x=contentX, w=contentW, y=contentTop-2;
            int buttonW=Math.max(100,w);
            addRenderableWidget(Button.builder(Component.literal(selected.enabled()?"Disable Module":"Enable Module"),b->toggleSelected())
                .bounds(x,y,buttonW,22).tooltip(Tooltip.create(Component.literal(selected.help()))).build());
            y+=26;
            int half=Math.max(48,(buttonW-5)/2);
            addRenderableWidget(Button.builder(Component.literal(selected.favorite()?"★ Favorited":"☆ Favorites"),b->{selected.setFavorite(!selected.favorite());saveConfig();rebuild();}).bounds(x,y,half,22).build());
            addRenderableWidget(Button.builder(Component.literal(bindingModule==selected?"Press a key...":"Keybind: "+keyName(selected.keyCode())),b->{bindingModule=selected;rebuild();}).bounds(x+half+5,y,buttonW-half-5,22).build());
            y+=26;
            int third=Math.max(42,(buttonW-10)/3);
            addRenderableWidget(Button.builder(Component.literal("Reset"),b->{selected.resetToDefaults();NotificationCenter.push(selected.name(),"Module reset to defaults");saveConfig();rebuild();}).bounds(x,y,third,22).build());
            addRenderableWidget(Button.builder(Component.literal("Settings"),b->{selected.resetSettings();NotificationCenter.push(selected.name(),"Settings reset to defaults");saveConfig();rebuild();}).bounds(x+third+5,y,third,22).build());
            addRenderableWidget(Button.builder(Component.literal("Help"),b->NotificationCenter.push(selected.name(),selected.help())).bounds(x+(third+5)*2,y,buttonW-(third+5)*2,22).build());
            addSettingWidgets(y+27);
        }
        int footer=panelY+panelH-28;
        int fw=Math.max(52,(panelW-32)/5);
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveConfig()).bounds(panelX+8,footer,fw,22).build());
        if(!compact) addRenderableWidget(Button.builder(Component.literal("HUD Editor"),b->minecraft.setScreen(new HudEditorScreen(this))).bounds(panelX+16+fw,footer,fw+20,22).build());
        addRenderableWidget(Button.builder(Component.literal("Load"),b->loadProfile()).bounds(panelX+24+fw*2,footer,fw,22).build());
        addRenderableWidget(Button.builder(Component.literal("Save Profile"),b->saveProfile()).bounds(panelX+32+fw*3,footer,fw+8,22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(panelX+panelW-Math.max(58,fw),footer,Math.max(58,fw),22).build());
        if(editingString!=null||editingColor!=null)addEditBox();
        if(editingString==null&&editingColor==null){
            int pw=Math.min(150,Math.max(80,panelW/4));
            profile=new EditBox(font,panelX+panelW-pw-8,footer-25,pw,22,Component.literal("Profile"));
            profile.setHint(Component.literal("profile"));profile.setValue(profileValue);addRenderableWidget(profile);
        }
    }
    private void clearFilters(){favoritesOnly=false;enabledOnly=false;alphabetical=false;if(search!=null)search.setValue("");savePreferences();scroll=0;rebuild();}
    private void cycleTheme(){theme=Theme.values()[(theme.ordinal()+1)%Theme.values().length];savePreferences();rebuild();}
    private List<Module> visibleModules(){
        String q=search==null?"":search.getValue().trim().toLowerCase(Locale.ROOT);
        java.util.ArrayList<Module> list=new java.util.ArrayList<>(ArsonClient.getInstance().modules().organized(category));
        if(alphabetical) list.sort(Comparator.comparing(Module::name,String.CASE_INSENSITIVE_ORDER));
        list.removeIf(m->(favoritesOnly&&!m.favorite())||(enabledOnly&&!m.enabled())||(!q.isEmpty()&&!((m.name()+" "+m.id()+" "+m.description()).toLowerCase(Locale.ROOT).contains(q))));
        return list;
    }
    private void refreshModuleButtons(){
        for(Button button:moduleButtons)removeWidget(button); moduleButtons.clear();
        int y=contentTop+2; int maxY=panelY+panelH-62;
        List<Module> visible=visibleModules();
        int rowH=24; int max=Math.max(1,(maxY-y)/rowH);
        int start=Math.min(Math.max(0,scroll/rowH),Math.max(0,visible.size()-max));
        for(int i=start;i<visible.size()&&i<start+max;i++){
            Module module=visible.get(i); Module chosen=module;
            Button button=Button.builder(Component.literal((module==selected?"▸ ":"  ")+(module.favorite()?"★ ":"☆ ")+(module.enabled()?"● ":"○ ")+module.name()),
                b->{selected=chosen;scroll=0;rebuild();})
                .bounds(contentX,y,contentW,rowH-2).tooltip(Tooltip.create(Component.literal(module.help()))).build();
            moduleButtons.add(button);addRenderableWidget(button);y+=rowH;
        }
    }
    private void toggleSelected(){if(selected==null)return;selected.toggle();NotificationCenter.push(selected.name(),selected.enabled()?"Enabled":"Disabled");saveConfig();rebuild();}
    private static String keyName(int keyCode){if(keyCode<=0)return"None";String name=GLFW.glfwGetKeyName(keyCode,0);return name!=null?name.toUpperCase(Locale.ROOT):"KEY "+keyCode;}
    private void addSettingWidgets(int startY){
        if(selected==null)return;
        int x=contentX, y=startY-scroll, bottom=panelY+panelH-58, width=Math.max(100,contentW);
        String groupId=null;
        for(Setting<?> setting:selected.settings()){
            if(!setting.visible())continue;
            SettingGroup group=setting.group();
            if(group!=null&&!group.id().equals(groupId)){groupId=group.id();if(y>=startY&&y<=bottom){addRenderableWidget(Button.builder(Component.literal("▸ "+group.name()),b->{}).bounds(x,y,width,20).tooltip(Tooltip.create(Component.literal(group.description()))).build());y+=22;}}
            if(y<startY){y+=27;continue;} if(y>bottom)break;
            int resetW=Math.min(55,Math.max(40,width/5)); int controlW=Math.max(55,width-resetW-5);
            if(setting instanceof BooleanSetting b)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+(b.enabled()?"ON":"OFF")),x1->{b.set(!b.enabled());saveConfig();rebuild();}).bounds(x,y,controlW,22).tooltip(Tooltip.create(Component.literal(setting.description()))).build());
            else if(setting instanceof DoubleSetting d)addRenderableWidget(new DoubleSettingSlider(x,y,controlW,22,d,this::saveConfig));
            else if(setting instanceof EnumSetting<?> e)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+prettyEnum(e.get())),b->{e.cycle(1);saveConfig();rebuild();}).bounds(x,y,controlW,22).tooltip(Tooltip.create(Component.literal(setting.description()))).build());
            else if(setting instanceof ColorSetting col)addRenderableWidget(Button.builder(Component.literal(setting.name()+": #"+String.format(Locale.ROOT,"%08X",col.get())),b->{editingColor=col;editingString=null;rebuild();}).bounds(x,y,controlW,22).build());
            else if(setting instanceof StringSetting s)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+s.get()),b->{editingString=s;editingColor=null;rebuild();}).bounds(x,y,controlW,22).build());
            else addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+String.valueOf(setting.get())),x1->{}).bounds(x,y,controlW,22).build());
            addRenderableWidget(Button.builder(Component.literal("Reset"),b->{setting.reset();NotificationCenter.push(setting.name(),"Reset to default");saveConfig();rebuild();}).bounds(x+controlW+5,y,resetW,22).build());
            y+=27;
        }
    }
    private static String prettyEnum(Object value){if(value==null)return"None";String raw=value.toString().toLowerCase(Locale.ROOT);StringBuilder out=new StringBuilder();for(String part:raw.split("_")){if(part.isEmpty())continue;if(out.length()>0)out.append(' ');out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));}return out.toString();}
    private void addEditBox(){editBox=new EditBox(font,panelX+385,panelY+panelH-62,210,22,Component.literal(editingString!=null?editingString.name():editingColor.name()));editBox.setValue(editingString!=null?editingString.get():String.format(Locale.ROOT,"%08X",editingColor.get()));editBox.setMaxLength(128);addRenderableWidget(editBox);addRenderableWidget(Button.builder(Component.literal("Apply"),b->applyEdit()).bounds(panelX+600,panelY+panelH-62,60,22).build());addRenderableWidget(Button.builder(Component.literal("Cancel"),b->{editingString=null;editingColor=null;editBox=null;rebuild();}).bounds(panelX+665,panelY+panelH-62,65,22).build());}
    private void applyEdit(){if(editBox!=null){if(editingString!=null)editingString.set(editBox.getValue());else if(editingColor!=null){try{String raw=editBox.getValue().trim().replace("#","");if(raw.length()==6)raw="FF"+raw;if(raw.length()!=8)throw new NumberFormatException();editingColor.set((int)Long.parseLong(raw,16));}catch(NumberFormatException ignored){NotificationCenter.push("Invalid color","Use RRGGBB or AARRGGBB");return;}}saveConfig();}editingString=null;editingColor=null;editBox=null;rebuild();}
    private void saveConfig(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private void saveProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.saveProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue()))NotificationCenter.push("Profile","Saved "+profile.getValue());else NotificationCenter.push("Profile","Invalid profile name");}
    private void loadProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.loadProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue())){NotificationCenter.push("Profile","Loaded "+profile.getValue());rebuild();}else NotificationCenter.push("Profile","Profile not found or invalid");}
    @Override public boolean keyPressed(KeyEvent event){
        if(bindingModule!=null){if(event.key()==GLFW.GLFW_KEY_ESCAPE){bindingModule=null;rebuild();return true;}bindingModule.setKeyCode(event.key());NotificationCenter.push(bindingModule.name(),"Keybind set to "+keyName(event.key()));bindingModule=null;saveConfig();rebuild();return true;}
        if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}
        if(search!=null&&search.isFocused()) return super.keyPressed(event);
        List<Module> visible=visibleModules();
        if(!visible.isEmpty()&&event.key()==GLFW.GLFW_KEY_UP){selectRelative(-1,visible);return true;}
        if(!visible.isEmpty()&&event.key()==GLFW.GLFW_KEY_DOWN){selectRelative(1,visible);return true;}
        if(event.key()==GLFW.GLFW_KEY_LEFT){changeCategory(-1);return true;}
        if(event.key()==GLFW.GLFW_KEY_RIGHT){changeCategory(1);return true;}
        if(event.key()==GLFW.GLFW_KEY_ENTER&&selected!=null){toggleSelected();return true;}
        return super.keyPressed(event);
    }
    private void selectRelative(int delta,List<Module> visible){
        int index=selected==null?-1:visible.indexOf(selected); if(index<0)index=delta>0?-1:0;
        selected=visible.get(Math.floorMod(index+delta,visible.size())); scroll=Math.max(0,(visible.indexOf(selected)-2)*24); rebuild();
    }
    private void changeCategory(int delta){
        Module.Category[] all=Module.Category.values(); int index=category.ordinal(); category=all[Math.floorMod(index+delta,all.length)]; selected=null;scroll=0;rebuild();
    }
    @Override public boolean charTyped(CharacterEvent event){boolean handled=super.charTyped(event);if(search!=null&&search.isFocused())refreshModuleButtons();return handled;}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){
        if(selected!=null){int step=verticalAmount>0?-24:24;scroll=Math.max(0,scroll+step);rebuild();return true;}
        return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
    }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){
        graphics.fill(panelX,panelY,panelX+panelW,panelY+panelH,panelColor());
        graphics.fill(panelX,panelY,panelX+panelW,panelY+(compact?30:34),headerColor());
        graphics.drawString(font,"Arson Client V3",panelX+12,panelY+10,0xFFFFFFFF);
        graphics.drawString(font,category.displayName()+" • "+visibleModules().size()+" shown",contentX,contentTop-16,accentColor());
        if(compact) graphics.drawString(font,"↑↓ select  ←→ category  Enter toggle  Esc close",panelX+8,panelY+panelH-45,0xFFAAAAAA);
        if(selected!=null&&!selected.description().isBlank()){
            String help=selected.description();
            int max=Math.max(1,contentW/6);
            if(help.length()>max)help=help.substring(0,Math.max(1,max-1))+"…";
            graphics.drawString(font,help,contentX,contentTop-16,0xFFAAAAAA);
        }
        graphics.renderOutline(panelX,panelY,panelW,panelH,accentColor());
        super.render(graphics,mouseX,mouseY,delta);
    }
    @Override public void onClose(){bindingModule=null;saveConfig();minecraft.setScreen(parent);}
}
