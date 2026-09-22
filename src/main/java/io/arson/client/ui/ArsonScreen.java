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
    private String pendingDeleteProfile;
    private ColorSetting editingColor;
    private final List<Button> moduleButtons = new ArrayList<>();
    private final List<SectionLabel> detailGroupLabels = new ArrayList<>();
    private ClickGuiLayoutModel.Geometry geometry;
    private int scroll, detailScroll;
    private boolean favoritesOnly, enabledOnly, alphabetical, compactMode, detailsPage;
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

    private int accentColor() { return 0xFFE27632; }
    private int panelColor() { return switch (theme) {
        case MIDNIGHT -> 0xF00B0D11;
        case GRAPHITE -> 0xF0121418;
        case CONTRAST -> 0xF0060709;
    }; }
    private int headerColor() { return switch (theme) {
        case MIDNIGHT -> 0xE012151B;
        case GRAPHITE -> 0xE01B1E23;
        case CONTRAST -> 0xE00D0E10;
    }; }
    private int cardColor(boolean enabled, boolean selectedCard) {
        if (selectedCard) return 0xE52B1D13;
        if (enabled) return 0xD51D1915;
        return 0xC814171C;
    }

    private void rebuild() {
        String searchValue = search == null ? "" : search.getValue();
        String profileValue = profile == null ? "" : profile.getValue();
        clearWidgets();
        moduleButtons.clear();
        detailGroupLabels.clear();
        profile = null;
        editBox = null;

        List<Module> visible = visibleModules();
        if (selected == null || !visible.contains(selected)) {
            selected = visible.isEmpty() ? null : visible.get(Math.max(0, Math.min(moduleFocus, visible.size() - 1)));
        }
        moduleFocus = selected == null ? 0 : Math.max(0, visible.indexOf(selected));

        Rects r = new Rects(geometry, detailsPage);
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
        if (editingString != null || editingColor != null) addEditBox(r); else if (detailsPage) addDetailWidgets(r);
        addFooter(r, profileValue);
    }

    private List<Module> visibleModules() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<Module> result = new ArrayList<>();
        var modules = ArsonClient.getInstance().modules();
        var candidates = q.isEmpty() ? modules.organized(category) : modules.search(q);
        for (Module m : candidates) {
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
                categoryFocus=categoriesIndex(c); category=c; moduleFocus=0; selected=null; scroll=0; detailScroll=0; detailsPage=false; focus=ClickGuiLayoutModel.Focus.MODULE; rebuild();
            }).bounds(r.rail().x(),rowY,r.rail().width(),rowH).build();
            addRenderableWidget(b);
        }
    }

    private int categoriesIndex(Module.Category c) {
        Module.Category[] cs=Module.Category.values();
        for(int i=0;i<cs.length;i++) if(cs[i]==c) return i;
        return 0;
    }

    private int visibleModuleColumns() {
        if (geometry.mode() == ClickGuiLayoutModel.Mode.NARROW) return 1;
        return Math.max(1, Math.min(3, (geometry.content().width() + 8) / 220));
    }

    private void addModuleCards(List<Module> visible) {
        if (detailsPage) return;
        ClickGuiLayoutModel.Rect area = geometry.content();
        int columns = visibleModuleColumns();
        int gap = 8;
        int cardHeight = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 40 : 48;
        int start = Math.min(scroll, Math.max(0, visible.size() - 1));
        List<ClickGuiLayoutModel.Rect> cards = ClickGuiLayoutModel.gridCards(
                area, visible.size() - start, columns, cardHeight, gap);
        for (int logical = 0; logical < cards.size(); logical++) {
            Module module = visible.get(start + logical);
            ClickGuiLayoutModel.Rect card = cards.get(logical);
            Button button = Button.builder(Component.literal((module.favorite() ? "★  " : "") + module.name() + (module.enabled() ? "  ●" : "  ○")), ignored -> {
                selected = module;
                moduleFocus = visibleModules().indexOf(module);
                detailsPage = true;
                detailScroll = 0;
                focus = ClickGuiLayoutModel.Focus.MODULE;
                rebuild();
            }).bounds(card.x(), card.y(), card.width(), card.height()).build();
            moduleButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void addDetailWidgets(Rects r) {
        if(selected==null) return;
        int x=r.detail().x()+8, w=Math.max(60,r.detail().width()-16);
        int top=r.detail().y()+6, bottom=r.detail().bottom()-6, y=top-detailScroll;
        int controlH=22,gapY=26;
        int actionWidth=Math.max(1,Math.min(100,(w-6)/2));
        if(y+controlH>top-1&&y<bottom) {
            addRenderableWidget(Button.builder(Component.literal("← Back"),b->{detailsPage=false;detailScroll=0;rebuild();}).bounds(x,y,actionWidth,controlH).build());
            addRenderableWidget(Button.builder(Component.literal(selected.enabled()?"● Enabled":"○ Disabled"),b->toggleSelected()).bounds(x+w-actionWidth,y,actionWidth,controlH).build());
        }
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
            if(y+34>top-1&&y<bottom) addRenderableWidget(Button.builder(Component.literal(selected.description()),b->{}).bounds(x,y,w,34).build());
            y+=40;
        }
        String groupId=null;
        for(Setting<?> setting:selected.settings()){
            if(!setting.visible()) continue;
            SettingGroup group=setting.group();
            if(group!=null&&!group.id().equals(groupId)){
                groupId=group.id();
                if(y+12>top-1&&y<bottom) detailGroupLabels.add(new SectionLabel(group.name(),x,y));
                y+=16;
            }
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
        int lines=3+(selected.description().isBlank()?0:2), groups=0;
        String groupId=null;
        for(Setting<?> setting:selected.settings()) if(setting.visible()) {
            lines++;
            SettingGroup group=setting.group();
            if(group!=null&&!group.id().equals(groupId)){groupId=group.id();groups++;}
        }
        return Math.max(0,lines*26+groups*16-Math.max(24,geometry.content().height()-12));
    }

    private void addFooter(Rects r,String profileValue) {
        int y = r.footer().y() + (geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 3 : 6);
        int controlHeight = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 20 : 22;
        int buttonY = y;
        addRenderableWidget(Button.builder(Component.literal("HUD"),b->{if(ArsonClient.getInstance().modules().get("hud") instanceof HudModule) minecraft.setScreen(new HudEditorScreen(this));}).bounds(r.footer().x(),buttonY,52,controlHeight).build());
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveConfig()).bounds(r.footer().x()+58,buttonY,52,controlHeight).build());

        int loadWidth = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 38 : 42;
        int saveWidth = loadWidth;
        int deleteWidth = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 48 : 52;
        int closeWidth = geometry.mode() == ClickGuiLayoutModel.Mode.NARROW ? 46 : 58;
        int gap = 4;
        int right = r.footer().right();
        int closeX = right - closeWidth;
        int deleteX = closeX - gap - deleteWidth;
        int saveX = deleteX - gap - saveWidth;
        int loadX = saveX - gap - loadWidth;
        int profileX;
        int profileWidth;
        if (geometry.mode() == ClickGuiLayoutModel.Mode.NARROW) {
            buttonY = y + 22;
            profileX = r.footer().x();
            profileWidth = Math.max(40, loadX - gap - profileX);
        } else {
            profileWidth = Math.min(120, Math.max(52, loadX - gap - r.footer().x() - 116));
            profileX = loadX - gap - profileWidth;
        }

        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(closeX,y,closeWidth,controlHeight).build());
        profile=new EditBox(font,profileX,geometry.mode()==ClickGuiLayoutModel.Mode.NARROW?buttonY:y,profileWidth,controlHeight,Component.literal("Profile"));
        profile.setHint(Component.literal("profile"));
        profile.setValue(profileValue);
        addRenderableWidget(profile);
        int actionY = geometry.mode()==ClickGuiLayoutModel.Mode.NARROW ? buttonY : y;
        addRenderableWidget(Button.builder(Component.literal("Load"),b->loadProfile()).bounds(loadX,actionY,loadWidth,controlHeight).build());
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveProfile()).bounds(saveX,actionY,saveWidth,controlHeight).build());
        String profileName = profile.getValue();
        String deleteLabel = profileName.equals(pendingDeleteProfile) ? "Confirm" : "Delete";
        addRenderableWidget(Button.builder(Component.literal(deleteLabel),b->deleteProfile()).bounds(deleteX,actionY,deleteWidth,controlHeight).build());
    }

    private void deleteProfile() {
        if (minecraft == null || profile == null || profile.getValue().isBlank()) {
            NotificationCenter.push("Profile", "Enter a profile name");
            return;
        }
        String name = profile.getValue();
        if (!name.equals(pendingDeleteProfile)) {
            pendingDeleteProfile = name;
            NotificationCenter.push("Profile", "Click Confirm to delete " + name);
            rebuild();
            return;
        }
        pendingDeleteProfile = null;
        if (ConfigManager.deleteProfile(minecraft, name)) NotificationCenter.push("Profile", "Deleted " + name);
        else NotificationCenter.push("Profile", "Profile not found or could not be deleted");
        rebuild();
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
    private void saveProfile(){pendingDeleteProfile=null;if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.saveProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue()))NotificationCenter.push("Profile","Saved "+profile.getValue());else NotificationCenter.push("Profile","Invalid profile name");}
    private void loadProfile(){pendingDeleteProfile=null;if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.loadProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue())){NotificationCenter.push("Profile","Loaded "+profile.getValue());rebuild();}else NotificationCenter.push("Profile","Profile not found or invalid");}

    @Override public boolean keyPressed(KeyEvent event){
        int key=event.key();
        if(bindingModule!=null){if(key==GLFW.GLFW_KEY_ESCAPE){bindingModule=null;rebuild();return true;}bindingModule.setKeyCode(key);NotificationCenter.push(bindingModule.name(),"Keybind set to "+keyName(key));bindingModule=null;saveConfig();rebuild();return true;}
        if(key==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}
        if(event.hasControlDown()&&key==GLFW.GLFW_KEY_K){if(search!=null){search.setFocused(true);focus=ClickGuiLayoutModel.Focus.SEARCH;}return true;}
        if(key==GLFW.GLFW_KEY_TAB){focus=nextFocus(event.hasShiftDown()?-1:1);applyFocus();return true;}
        if(focus==ClickGuiLayoutModel.Focus.CATEGORY&&(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){categoryFocus=ClickGuiLayoutModel.moveFocus(focus,categoryFocus,key==GLFW.GLFW_KEY_UP?-1:1,Module.Category.values().length,visibleModules().size());category=Module.Category.values()[categoryFocus];selected=null;moduleFocus=0;detailScroll=0;detailsPage=false;rebuild();return true;}
        if(focus==ClickGuiLayoutModel.Focus.MODULE&&!detailsPage&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN)){
            int delta=(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT)?(key==GLFW.GLFW_KEY_LEFT?-1:1):(key==GLFW.GLFW_KEY_UP?-1:1);
            if(key==GLFW.GLFW_KEY_UP||key==GLFW.GLFW_KEY_DOWN) delta*=visibleModuleColumns();
            List<Module> visible=visibleModules();moduleFocus=ClickGuiLayoutModel.moveModule(moduleFocus,delta,visible.size(),visibleModuleColumns());Module m=ClickGuiLayoutModel.safeGet(visible,moduleFocus);if(m!=null){selected=m;scroll=0;rebuild();}return true;
        }
        if(key==GLFW.GLFW_KEY_ENTER&&focus==ClickGuiLayoutModel.Focus.MODULE&&!detailsPage){detailsPage=selected!=null;detailScroll=0;rebuild();return true;}
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
        if (geometry.content().contains(mouseX,mouseY)) {
            if (detailsPage) detailScroll=Math.max(0,Math.min(maxDetailScroll(),detailScroll+(vertical>0?-1:1)));
            else scroll=Math.max(0,scroll+(vertical>0?-visibleModuleColumns():visibleModuleColumns()));
            rebuild();return true;
        }
        return super.mouseScrolled(mouseX,mouseY,horizontal,vertical);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
        Rects r=new Rects(geometry, detailsPage);
        g.fill(0,0,width,height,0x42000000);
        rounded(g,geometry.panelX(),geometry.panelY(),geometry.panelWidth(),geometry.panelHeight(),panelColor());
        g.fill(geometry.panelX(),geometry.panelY(),geometry.panelX()+geometry.panelWidth(),geometry.panelY()+34,headerColor());
        g.drawString(font,"ARSON",geometry.panelX()+14,geometry.panelY()+10,0xFFFFFFFF);
        g.drawString(font,"CLIENT V3",geometry.panelX()+14,geometry.panelY()+21,0xFF7E8998);
        g.drawString(font,""+ArsonClient.getInstance().modules().enabledCount()+" enabled",geometry.panelX()+geometry.panelWidth()-92,geometry.panelY()+12,0xFFB8C1CC);
        g.renderOutline(geometry.panelX(),geometry.panelY(),geometry.panelWidth(),geometry.panelHeight(),accentColor());
        g.fill(geometry.rail().x(),geometry.rail().y(),geometry.rail().right(),geometry.rail().bottom(),0x65151A22);
        for (SectionLabel label : detailGroupLabels) {
            if (label.y() >= geometry.content().y() && label.y() + 9 <= geometry.content().bottom())
                g.drawString(font, label.name().toUpperCase(Locale.ROOT), label.x(), label.y(), accentColor());
        }
        if (selected != null) {
            String breadcrumb = detailsPage ? selected.category().displayName() + "  /  " + selected.name() : category.displayName();
            g.drawString(font, breadcrumb, geometry.content().x() + 4, geometry.content().y() - 12, accentColor());
        }
        List<Module> visible = visibleModules();
        if (visible.isEmpty()) {
            String message = !search.getValue().isBlank() ? "No modules found" : favoritesOnly ? "No favorites saved" : enabledOnly ? "No enabled modules" : "No modules";
            int messageX = geometry.content().x() + Math.max(0, (geometry.content().width() - font.width(message)) / 2);
            g.drawString(font, message, messageX, geometry.content().y() + 10, 0xFFB8C1CC);
            if (!search.getValue().isBlank()) {
                String hint = "Try a different search.";
                int hintX = geometry.content().x() + Math.max(0, (geometry.content().width() - font.width(hint)) / 2);
                g.drawString(font, hint, hintX, geometry.content().y() + 24, 0xFF7E8998);
            }
        }
        super.render(g,mouseX,mouseY,delta);
    }

    private static void rounded(GuiGraphics g,int x,int y,int w,int h,int color){
        if(w<4||h<4){g.fill(x,y,x+w,y+h,color);return;}
        g.fill(x+2,y,x+w-2,y+h,color);g.fill(x,y+2,x+w,y+h-2,color);
        g.fill(x+1,y+1,x+w-1,y+2,color);g.fill(x+1,y+h-2,x+w-1,y+h-1,color);
    }

    private record SectionLabel(String name, int x, int y) {}

    private record Rects(ClickGuiLayoutModel.Geometry g, boolean detailsPage){
        ClickGuiLayoutModel.Rect rail(){return g.rail();}
        ClickGuiLayoutModel.Rect detail(){return detailsPage ? g.content() : g.detail();}
        ClickGuiLayoutModel.Rect search(){return g.search();}
        ClickGuiLayoutModel.Rect toolbar(){return g.toolbar();}
        ClickGuiLayoutModel.Rect footer(){return g.footer();}
        int headerY(){return g.panelY()+9;}
        int headerLeftX(){return g.panelX()+8;}
        int headerButtonW(){return g.mode()==ClickGuiLayoutModel.Mode.NARROW?34:54;}
    }

    @Override public void onClose(){bindingModule=null;pendingDeleteProfile=null;saveConfig();minecraft.setScreen(parent);}
}
