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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public ArsonScreen(Screen parent){super(Component.literal("Arson Client V3"));this.parent=parent;}
    @Override protected void init(){syncPreferences();panelX=Math.max(8,(width-panelW)/2);panelY=Math.max(8,(height-panelH)/2);rebuild();}
    private ClickGuiPreferencesModule preferences(){Module m=ArsonClient.getInstance().modules().get("clickgui-preferences");return m instanceof ClickGuiPreferencesModule p?p:null;}
    private void syncPreferences(){ClickGuiPreferencesModule p=preferences();if(p==null)return;theme=Theme.valueOf(p.theme().name());alphabetical=p.alphabetical();favoritesOnly=p.favoritesOnly();enabledOnly=p.enabledOnly();}
    private void savePreferences(){ClickGuiPreferencesModule p=preferences();if(p==null)return;p.setAlphabetical(alphabetical);p.setFavoritesOnly(favoritesOnly);p.setEnabledOnly(enabledOnly);if(!p.theme().name().equals(theme.name()))p.cycleTheme();ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private int headerColor(){return switch(theme){case MIDNIGHT->0xFF181820;case GRAPHITE->0xFF202428;case CONTRAST->0xFF101010;};}
    private int accentColor(){return switch(theme){case MIDNIGHT->0xFF6C63FF;case GRAPHITE->0xFF8AA0B8;case CONTRAST->0xFFFFFFFF;};}
    private int panelColor(){return switch(theme){case MIDNIGHT->0xE00F1015;case GRAPHITE->0xE016191C;case CONTRAST->0xEE050505;};}
    private void rebuild(){
        String profileValue=profile==null?"":profile.getValue();String searchValue=search==null?"":search.getValue();
        clearWidgets();moduleButtons.clear();profile=null;editBox=null;
        if(selected==null||selected.category()!=category||(favoritesOnly&&!selected.favorite())||(enabledOnly&&!selected.enabled()))selected=ArsonClient.getInstance().modules().organized(category).stream().filter(m->(!favoritesOnly||m.favorite())&&(!enabledOnly||m.enabled())).findFirst().orElse(null);
        search=new EditBox(font,panelX+145,panelY+8,245,20,Component.literal("Search modules"));search.setHint(Component.literal("Search modules..."));search.setValue(searchValue);addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal((favoritesOnly?"★ Favorites":"☆ Favorites")+" ("+ArsonClient.getInstance().modules().favoriteCount()+")"),b->{favoritesOnly=!favoritesOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+395,panelY+8,120,20).build());
        addRenderableWidget(Button.builder(Component.literal((enabledOnly?"● Enabled":"○ Enabled")+" ("+ArsonClient.getInstance().modules().enabledCount()+")"),b->{enabledOnly=!enabledOnly;savePreferences();scroll=0;rebuild();}).bounds(panelX+520,panelY+8,110,20).build());
        addRenderableWidget(Button.builder(Component.literal("Clear Filters"),b->{favoritesOnly=false;enabledOnly=false;alphabetical=false;if(search!=null)search.setValue("");savePreferences();scroll=0;rebuild();}).bounds(panelX+8,panelY+8,125,20).build());
        addRenderableWidget(Button.builder(Component.literal("Theme: "+theme.name()),b->{theme=Theme.values()[(theme.ordinal()+1)%Theme.values().length];savePreferences();rebuild();}).bounds(panelX+635,panelY+8,105,20).build());
        addRenderableWidget(Button.builder(Component.literal(alphabetical?"Sort: A-Z":"Sort: Smart"),b->{alphabetical=!alphabetical;savePreferences();rebuild();}).bounds(panelX+745,panelY+8,95,20).build());
        int y=panelY+42;for(Module.Category c:Module.Category.values()){Module.Category chosen=c;addRenderableWidget(Button.builder(Component.literal(chosen.displayName()+" ("+ArsonClient.getInstance().modules().categoryCount(chosen)+")"),b->{category=chosen;selected=null;scroll=0;rebuild();}).bounds(panelX+8,y,122,22).build());y+=26;}
        refreshModuleButtons();
        if(selected!=null){
            addRenderableWidget(Button.builder(Component.literal(selected.enabled()?"Disable Module":"Enable Module"),b->toggleSelected()).bounds(panelX+385,panelY+50,300,22).build());
            addRenderableWidget(Button.builder(Component.literal(selected.favorite()?"★ Favorited":"☆ Add to Favorites"),b->{selected.setFavorite(!selected.favorite());saveConfig();rebuild();}).bounds(panelX+385,panelY+76,145,22).build());
            addRenderableWidget(Button.builder(Component.literal(bindingModule==selected?"Press a key...":"Keybind: "+keyName(selected.keyCode())),b->{bindingModule=selected;rebuild();}).bounds(panelX+535,panelY+76,150,22).build());
            addRenderableWidget(Button.builder(Component.literal("Reset Module"),b->{selected.resetToDefaults();NotificationCenter.push(selected.name(),"Module reset to defaults");saveConfig();rebuild();}).bounds(panelX+385,panelY+102,100,22).build());
            addRenderableWidget(Button.builder(Component.literal("Reset Settings"),b->{selected.resetSettings();NotificationCenter.push(selected.name(),"Settings reset to defaults");saveConfig();rebuild();}).bounds(panelX+490,panelY+102,110,22).build());
            addSettingWidgets();
        }
        int footer=panelY+panelH-34;
        addRenderableWidget(Button.builder(Component.literal("Save"),b->saveConfig()).bounds(panelX+panelW-150,footer,65,22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"),b->onClose()).bounds(panelX+panelW-78,footer,65,22).build());
        Module hud=ArsonClient.getInstance().modules().get("hud");if(hud instanceof HudModule)addRenderableWidget(Button.builder(Component.literal("HUD Editor"),b->minecraft.setScreen(new HudEditorScreen(this))).bounds(panelX+375,footer,100,22).build());
        if(editingString!=null||editingColor!=null)addEditBox();
        if(editingString==null&&editingColor==null){profile=new EditBox(font,panelX+485,footer,120,22,Component.literal("Profile"));profile.setHint(Component.literal("profile"));profile.setValue(profileValue);addRenderableWidget(profile);addRenderableWidget(Button.builder(Component.literal("Load"),b->loadProfile()).bounds(panelX+610,footer,55,22).build());addRenderableWidget(Button.builder(Component.literal("Save"),b->saveProfile()).bounds(panelX+670,footer,55,22).build());}
    }
    private void refreshModuleButtons(){for(Button button:moduleButtons)removeWidget(button);moduleButtons.clear();int y=panelY+48;String q=search==null?"":search.getValue().trim().toLowerCase(Locale.ROOT);java.util.ArrayList<Module> visibleModules=new java.util.ArrayList<>(ArsonClient.getInstance().modules().organized(category));if(alphabetical)visibleModules.sort(java.util.Comparator.comparing(Module::name,String.CASE_INSENSITIVE_ORDER));for(Module module:visibleModules){if(favoritesOnly&&!module.favorite())continue;if(enabledOnly&&!module.enabled())continue;String haystack=(module.name()+" "+module.id()+" "+module.description()).toLowerCase(Locale.ROOT);if(!q.isEmpty()&&!haystack.contains(q))continue;Module chosen=module;Button button=Button.builder(Component.literal((module.favorite()?"★ ":"  ")+(module.enabled()?"● ":"○ ")+module.name()),b->{selected=chosen;scroll=0;rebuild();}).bounds(panelX+145,y,225,22).build();moduleButtons.add(button);addRenderableWidget(button);y+=25;if(y>panelY+panelH-65)break;}}
    private void toggleSelected(){if(selected==null)return;selected.toggle();NotificationCenter.push(selected.name(),selected.enabled()?"Enabled":"Disabled");saveConfig();rebuild();}
    private static String keyName(int keyCode){if(keyCode<=0)return"None";String name=GLFW.glfwGetKeyName(keyCode,0);return name!=null?name.toUpperCase(Locale.ROOT):"KEY "+keyCode;}
    private void addSettingWidgets(){int x=panelX+385,y=panelY+130-scroll,bottom=panelY+panelH-55;String groupId=null;for(Setting<?> setting:selected.settings()){if(!setting.visible())continue;SettingGroup group=setting.group();if(group!=null&&!group.id().equals(groupId)){groupId=group.id();if(y>=panelY+100&&y<=bottom){addRenderableWidget(Button.builder(Component.literal("▸ "+group.name()),b->{}).bounds(x,y,300,20).build());y+=22;if(!group.description().isBlank())y+=8;}}if(y<panelY+100){y+=27;continue;}if(y>bottom)break;if(setting instanceof BooleanSetting b)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+(b.enabled()?"ON":"OFF")),x1->{b.set(!b.enabled());saveConfig();rebuild();}).bounds(x,y,235,22).build());else if(setting instanceof DoubleSetting d)addRenderableWidget(new DoubleSettingSlider(x,y,235,22,d,this::saveConfig));else if(setting instanceof EnumSetting<?> e)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+prettyEnum(e.get())),b->{e.cycle(1);saveConfig();rebuild();}).bounds(x,y,235,22).build());else if(setting instanceof ColorSetting c)addRenderableWidget(Button.builder(Component.literal(setting.name()+": #"+String.format(Locale.ROOT,"%08X",c.get())),b->{editingColor=c;editingString=null;rebuild();}).bounds(x,y,235,22).build());else if(setting instanceof StringSetting s)addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+s.get()),b->{editingString=s;editingColor=null;rebuild();}).bounds(x,y,235,22).build());else addRenderableWidget(Button.builder(Component.literal(setting.name()+": "+String.valueOf(setting.get())),x1->{}).bounds(x,y,235,22).build());addRenderableWidget(Button.builder(Component.literal("Reset"),b->{setting.reset();NotificationCenter.push(setting.name(),"Reset to default");saveConfig();rebuild();}).bounds(x+240,y,60,22).build());y+=27;}}
    private static String prettyEnum(Object value){if(value==null)return"None";String raw=value.toString().toLowerCase(Locale.ROOT);StringBuilder out=new StringBuilder();for(String part:raw.split("_")){if(part.isEmpty())continue;if(out.length()>0)out.append(' ');out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));}return out.toString();}
    private void addEditBox(){editBox=new EditBox(font,panelX+385,panelY+panelH-62,210,22,Component.literal(editingString!=null?editingString.name():editingColor.name()));editBox.setValue(editingString!=null?editingString.get():String.format(Locale.ROOT,"%08X",editingColor.get()));editBox.setMaxLength(128);addRenderableWidget(editBox);addRenderableWidget(Button.builder(Component.literal("Apply"),b->applyEdit()).bounds(panelX+600,panelY+panelH-62,60,22).build());addRenderableWidget(Button.builder(Component.literal("Cancel"),b->{editingString=null;editingColor=null;editBox=null;rebuild();}).bounds(panelX+665,panelY+panelH-62,65,22).build());}
    private void applyEdit(){if(editBox!=null){if(editingString!=null)editingString.set(editBox.getValue());else if(editingColor!=null){try{String raw=editBox.getValue().trim().replace("#","");if(raw.length()==6)raw="FF"+raw;if(raw.length()!=8)throw new NumberFormatException();editingColor.set((int)Long.parseLong(raw,16));}catch(NumberFormatException ignored){NotificationCenter.push("Invalid color","Use RRGGBB or AARRGGBB");return;}}saveConfig();}editingString=null;editingColor=null;editBox=null;rebuild();}
    private void saveConfig(){if(minecraft!=null)ConfigManager.save(minecraft,ArsonClient.getInstance().modules());}
    private void saveProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.saveProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue()))NotificationCenter.push("Profile","Saved "+profile.getValue());else NotificationCenter.push("Profile","Invalid profile name");}
    private void loadProfile(){if(minecraft==null||profile==null||profile.getValue().isBlank()){NotificationCenter.push("Profile","Enter a profile name");return;}if(ConfigManager.loadProfile(minecraft,ArsonClient.getInstance().modules(),profile.getValue())){NotificationCenter.push("Profile","Loaded "+profile.getValue());rebuild();}else NotificationCenter.push("Profile","Profile not found or invalid");}
    @Override public boolean keyPressed(KeyEvent event){if(bindingModule!=null){if(event.key()==GLFW.GLFW_KEY_ESCAPE){bindingModule=null;rebuild();return true;}bindingModule.setKeyCode(event.key());NotificationCenter.push(bindingModule.name(),"Keybind set to "+keyName(event.key()));bindingModule=null;saveConfig();rebuild();return true;}if(event.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}boolean handled=super.keyPressed(event);if(search!=null&&search.isFocused())refreshModuleButtons();return handled;}
    @Override public boolean charTyped(CharacterEvent event){boolean handled=super.charTyped(event);if(search!=null&&search.isFocused())refreshModuleButtons();return handled;}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double horizontalAmount,double verticalAmount){if(selected!=null){scroll=Math.max(0,scroll+(verticalAmount>0?-27:27));rebuild();return true;}return super.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);}
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float delta){graphics.fill(panelX,panelY,panelX+panelW,panelY+panelH,panelColor());graphics.fill(panelX,panelY,panelX+panelW,panelY+34,headerColor());graphics.drawString(font,"Arson Client V3",panelX+12,panelY+11,0xFFFFFFFF);graphics.drawString(font,"Enabled: "+ArsonClient.getInstance().modules().enabledCount(),panelX+panelW-175,panelY+11,0xFFAAAAAA);graphics.drawString(font,"Favorites: "+ArsonClient.getInstance().modules().favoriteCount(),panelX+panelW-95,panelY+11,accentColor());graphics.renderOutline(panelX,panelY,panelW,panelH,accentColor());if(selected!=null){graphics.drawString(font,selected.name(),panelX+385,panelY+35,accentColor());String description=selected.description();if(!description.isBlank())graphics.drawString(font,description,panelX+385,panelY+22,0xFFAAAAAA);}super.render(graphics,mouseX,mouseY,delta);}
    @Override public void onClose(){bindingModule=null;saveConfig();minecraft.setScreen(parent);}
}
