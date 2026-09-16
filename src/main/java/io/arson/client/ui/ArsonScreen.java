package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.HudModule;
import io.arson.client.module.Module;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

/** Functional 1.21.11 ClickGUI: categories, modules, settings, profiles and HUD access. */
public final class ArsonScreen extends Screen {
    private final Screen parent;
    private Module.Category category = Module.Category.RENDER;
    private Module selected;
    private EditBox search;
    private EditBox profile;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private EditBox editBox;
    private int panelX, panelY, panelW = 900, panelH = 540;
    private int scroll;

    public ArsonScreen(Screen parent) { super(Component.literal("Arson Client V3")); this.parent = parent; }
    @Override protected void init() { panelX = Math.max(8, (width - panelW) / 2); panelY = Math.max(8, (height - panelH) / 2); rebuild(); }

    private void rebuild() {
        clearWidgets();
        if (selected == null || selected.category() != category) selected = ArsonClient.getInstance().modules().organized(category).stream().findFirst().orElse(null);
        search = new EditBox(font, panelX + 145, panelY + 8, 260, 20, Component.literal("Search modules"));
        search.setHint(Component.literal("Search modules...")); addRenderableWidget(search);
        int y = panelY + 42;
        for (Module.Category c : Module.Category.values()) { Module.Category chosen = c; addRenderableWidget(Button.builder(Component.literal(c.displayName()), b -> { category = chosen; selected = null; scroll = 0; rebuild(); }).bounds(panelX + 8, y, 122, 22).build()); y += 26; }
        y = panelY + 48; String q = search.getValue().toLowerCase(Locale.ROOT);
        for (Module module : ArsonClient.getInstance().modules().organized(category)) {
            if (!q.isEmpty() && !module.name().toLowerCase(Locale.ROOT).contains(q) && !module.id().contains(q)) continue;
            Module chosen = module; addRenderableWidget(Button.builder(Component.literal((module.enabled() ? "● " : "○ ") + module.name()), b -> { selected = chosen; scroll = 0; rebuild(); }).bounds(panelX + 145, y, 225, 22).build()); y += 25; if (y > panelY + panelH - 65) break;
        }
        if (selected != null) addSettingWidgets();
        int footer = panelY + panelH - 34;
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveConfig()).bounds(panelX + panelW - 150, footer, 65, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(panelX + panelW - 78, footer, 65, 22).build());
        Module hud = ArsonClient.getInstance().modules().get("hud");
        if (hud instanceof HudModule) addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> minecraft.setScreen(new HudEditorScreen(this))).bounds(panelX + 375, footer, 100, 22).build());
        if (editingString != null || editingColor != null) addEditBox();
        if (profile == null && editingString == null && editingColor == null) {
            profile = new EditBox(font, panelX + 485, footer, 120, 22, Component.literal("Profile")); profile.setHint(Component.literal("profile")); addRenderableWidget(profile);
            addRenderableWidget(Button.builder(Component.literal("Load"), b -> loadProfile()).bounds(panelX + 610, footer, 55, 22).build());
            addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveProfile()).bounds(panelX + 670, footer, 55, 22).build());
        }
    }

    private void addSettingWidgets() {
        int x = panelX + 385, y = panelY + 50 - scroll, bottom = panelY + panelH - 55;
        for (Setting<?> setting : selected.settings()) {
            if (y < panelY + 45) { y += 27; continue; } if (y > bottom) break;
            if (setting instanceof BooleanSetting b) addRenderableWidget(Button.builder(Component.literal(setting.name() + ": " + (b.enabled() ? "ON" : "OFF")), x1 -> { b.set(!b.enabled()); saveConfig(); rebuild(); }).bounds(x, y, 300, 22).build());
            else if (setting instanceof DoubleSetting d) addRenderableWidget(Button.builder(Component.literal(setting.name() + ": " + String.format(Locale.ROOT, "%.2f", d.get())), x1 -> { d.set(Math.min(d.max(), d.get() + d.step())); saveConfig(); rebuild(); }).bounds(x, y, 300, 22).build());
            else if (setting instanceof ColorSetting c) addRenderableWidget(Button.builder(Component.literal(setting.name() + ": #" + String.format(Locale.ROOT, "%08X", c.get())), x1 -> { editingColor = c; editingString = null; rebuild(); }).bounds(x, y, 300, 22).build());
            else if (setting instanceof StringSetting s) addRenderableWidget(Button.builder(Component.literal(setting.name() + ": " + s.get()), x1 -> { editingString = s; editingColor = null; rebuild(); }).bounds(x, y, 300, 22).build());
            y += 27;
        }
    }

    private void addEditBox() {
        editBox = new EditBox(font, panelX + 385, panelY + panelH - 62, 210, 22, Component.literal(editingString != null ? editingString.name() : editingColor.name()));
        editBox.setValue(editingString != null ? editingString.get() : String.format(Locale.ROOT, "%08X", editingColor.get())); editBox.setMaxLength(128); addRenderableWidget(editBox);
        addRenderableWidget(Button.builder(Component.literal("Apply"), b -> applyEdit()).bounds(panelX + 600, panelY + panelH - 62, 60, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> { editingString = null; editingColor = null; editBox = null; rebuild(); }).bounds(panelX + 665, panelY + panelH - 62, 65, 22).build());
    }

    private void applyEdit() {
        if (editBox != null) { if (editingString != null) editingString.set(editBox.getValue()); else if (editingColor != null) { try { editingColor.set((int) Long.parseLong(editBox.getValue().replace("#", ""), 16)); } catch (NumberFormatException ignored) { } } saveConfig(); }
        editingString = null; editingColor = null; editBox = null; rebuild();
    }
    private void saveConfig() { if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules()); }
    private void saveProfile() { if (minecraft != null && profile != null && !profile.getValue().isBlank()) ConfigManager.saveProfile(minecraft, ArsonClient.getInstance().modules(), profile.getValue()); }
    private void loadProfile() { if (minecraft != null && profile != null && !profile.getValue().isBlank()) { ConfigManager.loadProfile(minecraft, ArsonClient.getInstance().modules(), profile.getValue()); rebuild(); } }

    @Override public boolean keyPressed(KeyEvent event) { if (event.key() == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; } return super.keyPressed(event); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { return super.mouseClicked(event, doubleClick); }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { if (selected != null) { scroll = Math.max(0, scroll + (verticalAmount > 0 ? -27 : 27)); rebuild(); return true; } return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xE00F1015); graphics.fill(panelX, panelY, panelX + panelW, panelY + 34, 0xFF181820);
        graphics.drawString(font, "Arson Client V3", panelX + 12, panelY + 11, 0xFFFFFFFF); graphics.drawString(font, "Enabled: " + ArsonClient.getInstance().modules().enabledCount(), panelX + panelW - 105, panelY + 11, 0xFFAAAAAA);
        graphics.renderOutline(panelX, panelY, panelW, panelH, 0xFF4C4C56); if (selected != null) graphics.drawString(font, selected.name(), panelX + 385, panelY + 35, 0xFFAAAAAA); super.render(graphics, mouseX, mouseY, delta);
    }
    @Override public void onClose() { saveConfig(); minecraft.setScreen(parent); }
}
