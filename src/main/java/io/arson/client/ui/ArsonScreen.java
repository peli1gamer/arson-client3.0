package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ClickGuiPreferencesModule;
import io.arson.client.module.HudModule;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Responsive, keyboard-navigable ClickGUI. Geometry is delegated to the pure layout model. */
public final class ArsonScreen extends Screen {
    private enum Theme { MIDNIGHT, GRAPHITE, CONTRAST }
    private final Screen parent;
    private Module.Category category = Module.Category.RENDER;
    private Module selected;
    private Module bindingModule;
    private EditBox search, profile, editBox;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private final List<Button> moduleButtons = new ArrayList<>();
    private ClickGuiLayoutModel.Geometry geometry;
    private int scroll, detailScroll;
    private String settingGroupId = "all";
    private boolean favoritesOnly, enabledOnly, alphabetical, compactMode;
    private Theme theme = Theme.MIDNIGHT;
    private ClickGuiLayoutModel.Focus focus = ClickGuiLayoutModel.Focus.MODULE;
    private int categoryFocus, moduleFocus;

    public ArsonScreen(Screen parent) { super(Component.literal("Arson Client V3")); this.parent = parent; }

    @Override protected void init() {
        syncPreferences();
        geometry = ClickGuiLayoutModel.compute(width, height, compactMode ? 0.86 : preferencesScale());
        rebuild();
    }

    private double preferencesScale() {
        ClickGuiPreferencesModule p = preferences();
        return p == null ? 1.0 : p.panelScale();
    }

    private ClickGuiPreferencesModule preferences() {
        Module m = ArsonClient.getInstance().modules().get("clickgui-preferences");
        return m instanceof ClickGuiPreferencesModule p ? p : null;
    }

    private void syncPreferences() {
        ClickGuiPreferencesModule p = preferences();
        if (p == null) return;
        theme = Theme.valueOf(p.theme().name());
        alphabetical = p.alphabetical();
        favoritesOnly = p.favoritesOnly();
        enabledOnly = p.enabledOnly();
        compactMode = p.compactMode();
    }

    private void savePreferences() {
        ClickGuiPreferencesModule p = preferences();
        if (p == null) return;
        p.setAlphabetical(alphabetical);
        p.setFavoritesOnly(favoritesOnly);
        p.setEnabledOnly(enabledOnly);
        p.setCompactMode(compactMode);
        while (!p.theme().name().equals(theme.name())) p.cycleTheme();
        ConfigManager.save(minecraft, ArsonClient.getInstance().modules());
    }

    private int accentColor() { return switch (theme) {
        case MIDNIGHT -> 0xFF4C8DFF; case GRAPHITE -> 0xFF9BAEC4; case CONTRAST -> 0xFFFFFFFF; }; }
    private int panelColor() { return switch (theme) {
        case MIDNIGHT -> 0xE50C1017; case GRAPHITE -> 0xE5161A20; case CONTRAST -> 0xEE050505; }; }
    private int headerColor() { return switch (theme) {
        case MIDNIGHT -> 0xD919202B; case GRAPHITE -> 0xD921252C; case CONTRAST -> 0xEE101010; }; }
    private int cardColor(boolean enabled, boolean selectedCard) {
        if (selectedCard) return 0xE52A3A52;
        if (enabled) return 0xD51D2A38;
        return 0xC8141921;
    }

    private void rebuild() {
        String searchValue = search == null ? "" : search.getValue();
        String profileValue = profile == null ? "" : profile.getValue();
        clearWidgets();
        moduleButtons.clear();
        profile = null;
        editBox = null;

        List<Module> visible = visibleModules();
        if (selected == null || !visible.contains(selected)) {
            selected = visible.isEmpty() ? null : visible.get(Math.max(0, Math.min(moduleFocus, visible.size() - 1)));
        }
        moduleFocus = selected == null ? 0 : Math.max(0, visible.indexOf(selected));

        Rects r = new Rects(geometry);
        search = new EditBox(font, r.search().x(), r.search().y(), r.search().width(), r.search().height(), Component.literal("Search"));
        search.setHint(Component.literal("Search modules...   Ctrl+K"));
        search.setValue(searchValue);
        addRenderableWidget(search);

        addRenderableWidget(Button.builder(Component.literal("Clear"), b -> {
            favoritesOnly = false; enabledOnly = false; alphabetical = false;
            search.setValue(""); scroll = 0; detailScroll = 0; savePreferences(); rebuild();
        }).bounds(r.toolbar().x()+toolbarButtonX(0),r.toolbar().y(),toolbarButtonWidth(0),22).build());

        addRenderableWidget(Button.builder(Component.literal(favoritesOnly ? "★" : "☆"), b -> {
            favoritesOnly = !favoritesOnly; scroll = 0; savePreferences(); rebuild();
        }).bounds(r.toolbar().x()+toolbarButtonX(1),r.toolbar().y(),toolbarButtonWidth(1),22).build());

        addRenderableWidget(Button.builder(Component.literal(enabledOnly ? "●" : "○"), b -> {
            enabledOnly = !enabledOnly; scroll = 0; savePreferences(); rebuild();
        }).bounds(r.toolbar().x()+toolbarButtonX(2),r.toolbar().y(),toolbarButtonWidth(2),22).build());

        addRenderableWidget(Button.builder(Component.literal(compactMode ? "Compact" : "Comfort"), b -> {
            compactMode = !compactMode; savePreferences(); init();
        }).bounds(r.toolbar().x()+toolbarButtonX(3),r.toolbar().y(),toolbarButtonWidth(3),22).build());

        addRenderableWidget(Button.builder(Component.literal("Theme"), b -> {
            theme = Theme.values()[(theme.ordinal()+1)%Theme.values().length]; savePreferences(); rebuild();
        }).bounds(r.toolbar().x()+toolbarButtonX(4),r.toolbar().y(),toolbarButtonWidth(4),22).build());

        addRenderableWidget(Button.builder(Component.literal(alphabetical ? "A-Z" : "Smart"), b -> {
            alphabetical = !alphabetical; savePreferences(); rebuild();
        }).bounds(r.toolbar().x()+toolbarButtonX(5),r.toolbar().y(),toolbarButtonWidth(5),22).build());

        addCategoryButtons(r);
        addModuleCards(visible);
        if (editingString != null || editingColor != null) addEditBox(r); else addDetailWidgets(r);
        addFooter(r, profileValue);
    }

    private List<Module> visibleModules() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<Module> result = new ArrayList<>();
        for (Module m : ArsonClient.getInstance().modules().organized(category)) {
            if (favoritesOnly && !m.favorite()) continue;
            if (enabledOnly && !m.enabled()) continue;
            String hay = (m.name()+" "+m.id()+" "+m.description()).toLowerCase(Locale.ROOT);
            if (!q.isEmpty() && !hay.contains(q)) continue;
            result.add(m);
        }
        if (alphabetical) result.sort(Comparator.comparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }


    private int toolbarButtonWidth(int index){
        if(geometry.mode()==ClickGuiLayoutModel.Mode.NARROW){ int gap=2; int fitted=Math.max(15,(geometry.toolbar().width()-gap*5)/6); int desired=switch(index){case 0->32;case 1,2->22;case 3->42;case 4->30;default->30;}; return Math.min(desired,fitted); }
        return switch(index){case 0->48;case 1,2->26;case 3->72;case 4->58;default->54;};
    }
    private int toolbarButtonX(int index){int x=0;int gap=geometry.mode()==ClickGuiLayoutModel.Mode.NARROW?2:4;for(int i=0;i<index;i++)x+=toolbarButtonWidth(i)+gap;return x;}
    private void addCategoryButtons(Rects r) {
        Module.Category[] categories = Module.Category.values();
        for (int i=0;i<categories.length;i++) {
            Module.Category c=categories[i];
            int rowY=r.rail().y()+i*geometry.categoryRowHeight();
            int rowH=Math.min(24, Math.max(1, geometry.categoryRowHeight()));
            String label = geometry.mode()==ClickGuiLayoutModel.Mode.NARROW ? c.displayName().substring(0,1) : c.displayName();
            Button b=Button.builder(Component.literal((i==categoryFocus?"> ":"  ")+label), x -> {
                categoryFocus=categoriesIndex(c); category=c; moduleFocus=0; selected=null; settingGroupId="all"; scroll=0; detailScroll=0; focus=ClickGuiLayoutModel.Focus.MODULE; rebuild();
            }).bounds(r.rail().x(),rowY,r.rail().width(),rowH).build();
            addRenderableWidget(b);
        }
    }

    private int categoriesIndex(Module.Category c) {
        Module.Category[] cs=Module.Category.values();
        for(int i=0;i<cs.length;i++) if(cs[i]==c) return i;
        return 0;
    }

    private void addModuleCards(List<Module> visible) {
        int maxCards = Math.min(4, Math.max(1, Math.min(geometry.visibleRows(visible.size()), Math.max(1, geometry.moduleList().height() / (geometry.cardHeight()+geometry.cardGap()))) * geometry.columns()));
        int start = Math.min(scroll, Math.max(0, visible.size()-1));
        int shown=0;
        for(int i=start;i<visible.size() && shown<maxCards;i++,shown++){
            Module m=visible.get(i);
            int logical=i-start;
            ClickGuiLayoutModel.Rect card=geometry.moduleCard(logical,Math.min(visible.size()-start,maxCards));
            if(card.bottom()>geometry.detail().bottom() && geometry.columns()==1) continue;
            Module chosen=m;
            Button b=Button.builder(Component.literal((m.favorite()?"★ ":"  ")+(m.enabled()?"● ":"○ ")+m.name()), x -> {
                if(selected!=chosen) settingGroupId="all"; selected=chosen; moduleFocus=visibleModules().indexOf(chosen); detailScroll=0; focus=ClickGuiLayoutModel.Focus.MODULE; rebuild();
            }).bounds(card.x(),card.y(),card.width(),card.height()).build();
            moduleButtons.add(b); addRenderableWidget(b);
        }
    }

    private void addDetailWidgets(Rects r) {
        if(selected==null) return;
        int x=r.detail().x()+8, w=Math.max(60,r.detail().width()-16);
        int top=r.detail().y()+6, bottom=r.detail().bottom()-6, y=top-detailScroll;
        int controlH=22,gapY=26;
        if(y+controlH>top-1&&y<bottom) addRenderableWidget(Button.builder(Component.literal(selected.enabled()?"Disable":"Enable"),b->toggleSelected()).bounds(x,y,w,controlH).build());
        y+=gapY;
        int half=Math.max(45,(w-6)/2);
        if(y+controlH>top-1&&y<bottom){
            addRenderableWidget(Button.builder(Component.literal(selected.favorite()?"★ Favorite":"☆ Favorite"),b->{selected.setFavorite(!selected.favorite());saveConfig();rebuild();}).bounds(x,y,half,controlH).build());
            if(x+half+6+half<=r.detail().right()-6) addRenderableWidget(Button.builder(Component.literal(bindingModule==selected?"Press key":"Key: "+keyName(selected.keyCode())),b->{bindingModule=selected;focus=ClickGuiLayoutModel.Focus.ACTION;rebuild();}).bounds(x+half+6,y,half,controlH).build());
        }
        y+=gapY;
        if(y+controlH>top-1&&y<bottom){
            addRenderableWidget(Button.builder(Component.literal("Reset"),b->{selected.resetToDefaults();NotificationCenter.push(selected.name(),"Module reset");saveConfig();rebuild();}).bounds(x,y,half,controlH).build());
            if(x+half+6+half<=r.detail().right()-6) addRenderableWidget(Button.builder(Component.literal("Reset settings"),b->{selected.resetSettings();NotificationCenter.push(selected.name(),"Settings reset");saveConfig();rebuild();}).bounds(x+half+6,y,half,controlH).build());
        }
        y+=gapY+4;
        if(!selected.description().isBlank()){
            if(y+30>top-1&&y<bottom) addRenderableWidget(Button.builder(Component.literal(selected.description()),b->{}).bounds(x,y,w,30).build());
            y+=34;
        }
        List<SettingCategory> categories=settingCategories();
        if(!settingGroupId.equals("all")&&categories.stream().noneMatch(category->category.id().equals(settingGroupId))) settingGroupId="all";
        String activeName=settingGroupId.equals("all")?"All settings":categoryName(categories,settingGroupId);
        if(y+22>top-1&&y<bottom) addRenderableWidget(Button.builder(Component.literal("Category: "+activeName+"  ›"),b->{cycleSettingCategory(categories);detailScroll=0;rebuild();}).bounds(x,y,w,22).build());
        y+=26;
        String groupId=null;
        for(Setting<?> setting:selected.settings()){
            if(!setting.visible()||!settingMatchesCategory(setting)) continue;
            SettingGroup group=setting.group();
            if(group!=null&&!group.id().equals(groupId)){groupId=group.id();y+=4;}
            int controlW=Math.max(45,w-32);
            if(y+22>top-1&&y<bottom){
                if(setting instanceof BooleanSetting bs)
                    addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+(bs.enabled()?"ON":"OFF")),b->{bs.set(!bs.enabled());saveConfig();rebuild();}).bounds(x,y,controlW,22).build());
                else if(setting instanceof DoubleSetting ds)
                    addRenderableWidget(new DoubleSettingSlider(x,y,controlW,22,ds,this::saveConfig));
                else if(setting instanceof EnumSetting<?> es)
                    addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+prettyEnum(es.get())),b->{es.cycle(1);saveConfig();rebuild();}).bounds(x,y,controlW,22).build());
                else if(setting instanceof ColorSetting cs)
                    addRenderableWidget(Button.builder(Component.literal(setting.name()+": #"+String.format(Locale.ROOT,"%08X",cs.get())),b->{editingColor=cs;editingString=null;rebuild();}).bounds(x,y,controlW,22).build());
                else if(setting instanceof StringSetting ss)
                    addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+ss.get()),b->{editingString=ss;editingColor=null;rebuild();}).bounds(x,y,controlW,22).build());
                else addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+String.valueOf(setting.get())),b->{}).bounds(x,y,controlW,22).build());
                if(x+controlW+4+28<=r.detail().right()-6) addRenderableWidget(Button.builder(Component.literal("↺"),b->{setting.reset();saveConfig();rebuild();}).bounds(x+controlW+4,y,28,22).build());
            }
            y+=26;
        }
    }
    private int maxDetailScroll(){
        if(selected==null)return 0;
        int contentHeight=3*26+4+(selected.description().isBlank()?0:34)+26;
        String lastGroup=null;
        for(Setting<?> setting:selected.settings()) if(setting.visible()&&settingMatchesCategory(setting)){
            SettingGroup group=setting.group();
            if(group!=null&&!group.id().equals(lastGroup)){contentHeight+=4;lastGroup=group.id();}
            contentHeight+=26;
        }
        return Math.max(0,contentHeight-Math.max(24,geometry.detail().height()-12));
    }

    private List<SettingCategory> settingCategories(){
        List<SettingCategory> result=new ArrayList<>();
        if(selected==null)return result;
        for(Setting<?> setting:selected.settings()) if(setting.visible()){
            SettingCategory category=categoryFor(setting);
            if(result.stream().noneMatch(existing->existing.id().equals(category.id()))) result.add(category);
        }
        return result;
    }
    private SettingCategory categoryFor(Setting<?> setting){
        SettingGroup group=setting.group();
        if(group!=null)return new SettingCategory("group:"+group.id(),group.name());
        String key=(setting.id()+" "+setting.name()).toLowerCase(Locale.ROOT);
        if(containsAny(key,"color","colour","render","visual","outline","box","esp","label","background","opacity","glow","tracer","font","scale","size","align","position","anchor"))
            return new SettingCategory("visuals","Visuals");
        if(containsAny(key,"target","range","radius","fov","aim","entity","player","mob","team","friend","enemy","hitbox","part"))
            return new SettingCategory("targeting","Targeting");
        if(containsAny(key,"cache","interval","scan","delay","tick","update","limit","performance","optimiz"))
            return new SettingCategory("performance","Performance");
        if(containsAny(key,"auto","mode","sound","alert","notify","packet","movement","knockback","behavior","behaviour"))
            return new SettingCategory("behavior","Behavior");
        if(containsAny(key,"layout","spacing","padding","width","height","gap","offset","sort"))
            return new SettingCategory("layout","Layout");
        return new SettingCategory("general","General");
    }
    private boolean containsAny(String value,String... tokens){
        for(String token:tokens)if(value.contains(token))return true;
        return false;
    }
    private String categoryName(List<SettingCategory> categories,String id){
        for(SettingCategory category:categories)if(category.id().equals(id))return category.name();
        return "All settings";
    }
    private boolean settingMatchesCategory(Setting<?> setting){
        return "all".equals(settingGroupId)||categoryFor(setting).id().equals(settingGroupId);
    }
    private void cycleSettingCategory(List<SettingCategory> categories){
        List<String> ids=new ArrayList<>();ids.add("all");for(SettingCategory category:categories)ids.add(category.id());
        int index=ids.indexOf(settingGroupId);settingGroupId=ids.get((index+1+ids.size())%ids.size());
    }
    private record SettingCategory(String id,String name){}

    private void addFooter(Rects r,String profileValue) {
        if(geometry.mode()==ClickGuiLayoutModel.Mode.NARROW){
            int y=r.footer().y()+3;
            addRenderableWidget(Button.builder(Component.literal("HUD"),b->{if(ArsonClient.getInstance().modules().get("hud") instanceof HudModule) minecraft.setScreen(new HudEditorScreen(this));}).bounds(r.footer().x(),y,42,20).build());
            addRenderableWidget(Button.builder(Component.literal("Save"),b->saveConfig()).bounds(r.footer().x()+46,y,42,20).build());
            addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(r.footer().right()-46,y,46,20).build());
            int row2=y+22, pw=Math.max(70,r.footer().width()-80);
            profile=new EditBox(font,r.footer().x(),row2,pw,20,Component.literal("Profile"));profile.setHint(Component.literal("profile"));profile.setValue(profileValue);addRenderableWidget(profile);
            addRenderableWidget(Button.builder(Component.literal("L"),b->loadProfile()).bounds(r.footer().right()-74,row2,34,20).build());
            addRenderableWidget(Button.builder(Component.literal("S"),b->saveProfile()).bounds(r.footer().right()-38,row2,34,20).build());
            return;
        }
        int y=r.footer().y()+6;
        addRenderableWidget(Button.builder(Component.literal("HUD"),b->{if(ArsonClient.getInstance().modules().get("hud") instanceof HudModule) minecraft.setScreen(new HudEditorScreen(this));}).bounds(r.footer().x(),y,52,22).build());
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveConfig()).bounds(r.footer().x()+58,y,52,22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(r.footer().right()-58,y,58,22).build());
        int pw=Math.min(110,Math.max(60,r.footer().width()-240));
        profile=new EditBox(font,r.footer().right()-190,y,pw,22,Component.literal("Profile"));profile.setHint(Component.literal("profile"));profile.setValue(profileValue);addRenderableWidget(profile);
        addRenderableWidget(Button.builder(Component.literal("Load"),b->loadProfile()).bounds(r.footer().right()-74,y,36,22).build());
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveProfile()).bounds(r.footer().right()-38,y,36,22).build());
    }

    private void addEditBox(Rects r) {
        int y=r.detail().y()+6;
        int w=Math.max(70,r.detail().width()-50);
        editBox=new EditBox(font,r.detail().x()+8,y,w,22,Component.literal(editingString!=null?editingString.name():editingColor.name()));
        editBox.setValue(editingString!=null?editingString.get():String.format(Locale.ROOT,"%08X",editingColor.get()));
        editBox.setMaxLength(128);
        addRenderableWidget(editBox);
        addRenderableWidget(Button.builder(Component.literal("Apply"),b->applyEdit()).bounds(r.detail().right()-40,y,34,22).build());
        addRenderableWidget(Button.builder(Component.literal("×"),b->{editingString=null;editingColor=null;rebuild();}).bounds(r.detail().right()-40,y+26,34,22).build());
    }

    private void toggleSelected(){if(selected==null)return;selected.toggle();NotificationCenter.push(selected.name(),selected.enabled()?"Enabled":"Disabled");saveConfig();rebuild();}
    private static String keyName(int key){if(key<=0)return"None";String n=GLFW.glfwGetKeyName(key,0);return n==null?"KEY "+key:n.toUpperCase(Locale.ROOT);}
    private static String prettyEnum(Object v){if(v==null)return"None";String raw=v.toString().toLowerCase(Locale.ROOT),out="";for(String p:raw.split("_"))if(!p.isEmpty())out+=(out.isEmpty()?"":" ")+Character.toUpperCase(p.charAt(0))+p.substring(1);return out;}
    private void applyEdit(){if(editBox!=null){if(editingString!=null)editingString.set(editBox.getValue());else if(editingColor!=null){try{String raw=editBox.getValue().trim().replace("#","");if(raw.length()==6)raw="FF"+raw;if(raw.length()!=8)throw new NumberFormatException();editingColor.set((int)Long.parseLong(raw,16));}catch(NumberFormatException ignored){NotificationCenter.push("Invalid color","Use RRGGBB or AARRGGBB");return;}}saveConfig();}editingString=null;editingColor=null;editBox=null;rebuild();}
    private void saveConfig(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private void saveProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.saveProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue()))NotificationCenter.push("Profile","Saved "+profile.getValue());else NotificationCenter.push("Profile","Invalid profile name");}
    private void loadProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.loadProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue())){NotificationCenter.push("Profile","Loaded "+profile.getValue());rebuild();}else NotificationCenter.push("Profile","Profile not found or invalid");}

    @Override public boolean keyPressed(KeyEvent event){
        int key=event.key();
        if(bindingModule!=null){if(key==GLFW.GLFW_KEY_ESCAPE){bindingModule=null;rebuild();return true;}bindingModule.setKeyCode(key);NotificationCenter.push(bindingModule.name(),"Keybind set to "+keyName(key));bindingModule=null;saveConfig();rebuild();return true;}
        if(key==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}
        if(event.hasControlDown()&&key==GLFW.GLFW_KEY_K){if(search!=null){search.setFocused(true);focus=ClickGuiLayoutModel.Focus.SEARCH;}return true;}
        if(key==GLFW.GLFW_KEY_TAB){focus=nextFocus(event.hasShiftDown()?-1:1);applyFocus();return true;}
        if(focus==ClickGuiLayoutModel.Focus.CATEGORY&&(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){categoryFocus=ClickGuiLayoutModel.moveFocus(focus,categoryFocus,key==GLFW.GLFW_KEY_UP?-1:1,Module.Category.values().length,visibleModules().size());category=Module.Category.values()[categoryFocus];selected=null;settingGroupId="all";moduleFocus=0;detailScroll=0;rebuild();return true;}
        if(focus==ClickGuiLayoutModel.Focus.MODULE&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){
            int delta=(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT)?(key==GLFW.GLFW_KEY_LEFT?-1:1):(key==GLFW.GLFW_KEY_UP?-1:1);
            if(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN) delta*=geometry.columns();
            List<Module> visible=visibleModules();moduleFocus=ClickGuiLayoutModel.moveModule(moduleFocus,delta,visible.size(),geometry.columns());Module m=ClickGuiLayoutModel.safeGet(visible,moduleFocus);if(m!=null){if(selected!=m)settingGroupId="all";selected=m;scroll=0;detailScroll=0;rebuild();}return true;
        }
        if(key==GLFW.GLFW_KEY_ENTER&&focus==ClickGuiLayoutModel.Focus.MODULE){toggleSelected();return true;}
        return super.keyPressed(event);
    }

    private ClickGuiLayoutModel.Focus nextFocus(int delta){
        ClickGuiLayoutModel.Focus[] order={ClickGuiLayoutModel.Focus.SEARCH,ClickGuiLayoutModel.Focus.CATEGORY,ClickGuiLayoutModel.Focus.MODULE,ClickGuiLayoutModel.Focus.ACTION};
        int current=0;for(int i=0;i<order.length;i++)if(order[i]==focus){current=i;break;}
        return order[Math.floorMod(current+delta,order.length)];
    }
    private void applyFocus(){if(search!=null)search.setFocused(focus==ClickGuiLayoutModel.Focus.SEARCH);}

    @Override public boolean charTyped(CharacterEvent event){boolean handled=super.charTyped(event);if(search!=null&&search.isFocused()){scroll=0;rebuild();}return handled;}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontal,double vertical){
        if(vertical==0)return super.mouseScrolled(mouseX,mouseY,horizontal,vertical);
        if(geometry.moduleList().contains(mouseX,mouseY)||geometry.detail().contains(mouseX,mouseY)){
            int direction=vertical>0?-1:1;
            if(geometry.detail().contains(mouseX,mouseY)) detailScroll=Math.max(0,Math.min(maxDetailScroll(),detailScroll+direction*3*26));
            else {List<Module> visible=visibleModules();int step=geometry.columns()*2;int max=Math.max(0,((visible.size()+geometry.columns()-1)/geometry.columns())*geometry.columns()-4);scroll=Math.max(0,Math.min(max,scroll+direction*step));}
            rebuild();return true;
        }
        return super.mouseScrolled(mouseX,mouseY,horizontal,vertical);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        Rects r=new Rects(geometry);
        g.fill(0,0,width,height,0x42000000);
        rounded(g,geometry.panelX(),geometry.panelY(),geometry.panelWidth(),geometry.panelHeight(),panelColor());
        g.fill(geometry.panelX(),geometry.panelY(),geometry.panelX()+geometry.panelWidth(),geometry.panelY()+34,headerColor());
        g.drawString(font,"ARSON",geometry.panelX()+14,geometry.panelY()+10,0xFFFFFFFF);
        g.drawString(font,"CLIENT V3",geometry.panelX()+14,geometry.panelY()+21,0xFF7E8998);
        g.drawString(font,""+ArsonClient.getInstance().modules().enabledCount()+" enabled",geometry.panelX()+geometry.panelWidth()-92,geometry.panelY()+12,0xFFB8C1CC);
        g.renderOutline(geometry.panelX(),geometry.panelY(),geometry.panelWidth(),geometry.panelHeight(),accentColor());
        g.fill(geometry.rail().x(),geometry.rail().y(),geometry.rail().right(),geometry.rail().bottom(),0x65151A22);
        rounded(g,r.moduleList().x(),r.moduleList().y(),r.moduleList().width(),r.moduleList().height(),0x501A202A);
        rounded(g,r.detail().x(),r.detail().y(),r.detail().width(),r.detail().height(),0x501A202A);
        g.fill(r.moduleList().right(),r.moduleList().y()+4,r.moduleList().right()+1,r.moduleList().bottom()-4,0x553A4657);
        if(selected!=null){
            g.fill(r.detail().x()+7,r.detail().y()+5,r.detail().right()-7,r.detail().y()+6,accentColor());
            g.drawString(font,selected.name(),r.detail().x()+8,r.detail().y()-12,accentColor());
        }
        int selectedCategoryY=r.rail().y()+categoryFocus*geometry.categoryRowHeight();
        g.fill(r.rail().x(),selectedCategoryY,r.rail().x()+2,Math.min(r.rail().bottom(),selectedCategoryY+Math.min(24,geometry.categoryRowHeight())),accentColor());
        g.fill(geometry.panelX()+1,geometry.panelY()+33,geometry.panelX()+geometry.panelWidth()-1,geometry.panelY()+34,accentColor());
        super.render(g,mouseX,mouseY,delta);
        for(int i=0;i<renderedModules.size();i++){
            Module module=renderedModules.get(i);
            ClickGuiLayoutModel.Rect card=geometry.moduleCard(i,renderedModules.size());
            if(module==selected) g.renderOutline(card.x(),card.y(),card.width(),card.height(),accentColor());
            if(module.enabled()) g.fill(card.x()+1,card.y()+3,card.x()+3,card.bottom()-3,accentColor());
        }
        g.drawString(font,"MODULES",r.moduleList().x()+2,r.moduleList().y()-10,0xFF8D99AA);
        g.drawString(font,"SETTINGS",r.detail().x()+2,r.detail().y()-10,0xFF8D99AA);
        g.fill(r.footer().x(),r.footer().y(),r.footer().right(),r.footer().y()+1,0x553A4657);
    }

    private static void rounded(GuiGraphics g,int x,int y,int w,int h,int color){
        if(w<4||h<4){g.fill(x,y,x+w,y+h,color);return;}
        g.fill(x+2,y,x+w-2,y+h,color);g.fill(x,y+2,x+w,y+h-2,color);
        g.fill(x+1,y+1,x+w-1,y+2,color);g.fill(x+1,y+h-2,x+w-1,y+h-1,color);
    }

    private record Rects(ClickGuiLayoutModel.Geometry g){
        ClickGuiLayoutModel.Rect rail(){return g.rail();}
        ClickGuiLayoutModel.Rect detail(){return g.detail();}
        ClickGuiLayoutModel.Rect search(){return g.search();}
        ClickGuiLayoutModel.Rect toolbar(){return g.toolbar();}
        ClickGuiLayoutModel.Rect footer(){return g.footer();}
        int headerY(){return g.panelY()+9;}
        int headerLeftX(){return g.panelX()+8;}
        int headerButtonW(){return g.mode()==ClickGuiLayoutModel.Mode.NARROW?34:54;}
    }

    @Override public void onClose(){bindingModule=null;saveConfig();minecraft.setScreen(parent);}
}
