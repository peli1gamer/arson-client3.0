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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ArsonScreen extends Screen {
    private static final int[] COLOR_PRESETS = {0xD6FFFFFF, 0xD6FF5555, 0xD6FFAA00, 0xD6FFFF55, 0xD655FF55, 0xD655FFFF, 0xD655AAFF, 0xD6AA55FF, 0xD6FF55FF, 0xD6AAAAAA};
    private static final int CONTENT_TOP = 58;
    private static final int CONTENT_BOTTOM = 320;
    private static final int SCROLL_STEP = 24;
    private final Screen parent;
    private Module.Category selectedCategory = Module.Category.RENDER;
    private final List<Button> contentButtons = new ArrayList<>();
    private int scrollOffset;
    private int maxScroll;
    private Module bindingModule;
    private EditBox searchBox;
    private EditBox profileBox;
    private EditBox stringBox;
    private StringSetting editingString;

    public ArsonScreen(Screen parent) { super(Component.literal("Arson Client V3")); this.parent = parent; }

    @Override protected void init() { rebuild(); }

    private void rebuild() {
        clearWidgets(); contentButtons.clear();
        int panelX = Math.max(20, width / 2 - 280), panelY = Math.max(20, height / 2 - 180);
        searchBox = new EditBox(font, panelX + 145, panelY + 14, 265, 20, Component.literal("Search modules"));
        searchBox.setHint(Component.literal("Search modules...")); addRenderableWidget(searchBox);
        int categoryX = panelX + 18, categoryY = panelY + 58;
        for (Module.Category category : Module.Category.values()) {
            Module.Category current = category;
            addRenderableWidget(Button.builder(Component.literal(category.displayName()), b -> { selectedCategory = current; bindingModule = null; scrollOffset = 0; rebuild(); }).bounds(categoryX, categoryY, 105, 20).build());
            categoryY += 24;
        }
        addContent(panelX, panelY); addFooter(panelX, panelY);
        if (editingString != null) {
            stringBox = new EditBox(font, panelX + 145, panelY + 282, 265, 20, Component.literal(editingString.name()));
            stringBox.setValue(editingString.get());
            stringBox.setMaxLength(128);
            addRenderableWidget(stringBox);
            stringBox.setFocused(true);
            stringBox.moveCursorToEnd(false);
        }
    }

    private boolean matches(Module module) {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return query.isEmpty() || module.name().toLowerCase(Locale.ROOT).contains(query) || module.id().toLowerCase(Locale.ROOT).contains(query);
    }

    private void addContent(int panelX, int panelY) {
        int x = panelX + 145, y = panelY + CONTENT_TOP - scrollOffset, contentHeight = 0;
        for (Module module : ArsonClient.getInstance().modules().all()) {
            if (module.category() != selectedCategory || !matches(module)) continue;
            Module current = module;
            Button moduleButton = Button.builder(moduleLabel(current), button -> { current.toggle(); button.setMessage(moduleLabel(current)); saveConfig(); }).bounds(x, y, 265, 21).build();
            addContentWidget(moduleButton, y, panelY);
            Button bindButton = Button.builder(bindLabel(current), button -> { bindingModule = current; button.setMessage(Component.literal("Press a key...")); }).bounds(x + 273, y, 92, 21).build();
            addContentWidget(bindButton, y, panelY);
            y += 25; contentHeight += 25;
            for (Setting<?> setting : current.settings()) {
                Button settingButton = null;
                if (setting instanceof BooleanSetting bool) settingButton = Button.builder(settingLabel(bool), button -> { bool.set(!bool.enabled()); button.setMessage(settingLabel(bool)); saveConfig(); }).bounds(x + 14, y, 351, 18).build();
                else if (setting instanceof DoubleSetting number) settingButton = Button.builder(settingLabel(number), button -> { double next = number.get() + number.step(); if (next > number.max()) next = number.min(); number.set(next); button.setMessage(settingLabel(number)); saveConfig(); }).bounds(x + 14, y, 351, 18).build();
                else if (setting instanceof ColorSetting color) settingButton = Button.builder(settingLabel(color), button -> { color.set(nextColor(color.get())); button.setMessage(settingLabel(color)); saveConfig(); }).bounds(x + 14, y, 351, 18).build();
                else if (setting instanceof StringSetting text) settingButton = Button.builder(settingLabel(text), button -> beginStringEdit(text)).bounds(x + 14, y, 351, 18).build();
                if (settingButton != null) { addContentWidget(settingButton, y, panelY); y += 21; contentHeight += 21; }
            }
            y += 6; contentHeight += 6;
        }
        maxScroll = Math.max(0, contentHeight - (CONTENT_BOTTOM - CONTENT_TOP));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private void beginStringEdit(StringSetting setting) {
        editingString = setting;
        rebuild();
    }

    private void finishStringEdit(boolean save) {
        if (editingString != null && stringBox != null && save) {
            editingString.set(stringBox.getValue());
            saveConfig();
        }
        editingString = null;
        stringBox = null;
        rebuild();
    }

    private void addFooter(int panelX, int panelY) {
        int footerY = panelY + 330;
        addRenderableWidget(Button.builder(Component.literal("Reset Category"), b -> { for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == selectedCategory) module.resetToDefaults(); saveConfig(); rebuild(); }).bounds(panelX + 145, footerY, 120, 20).build());
        Module hudModule = ArsonClient.getInstance().modules().get("hud");
        if (hudModule instanceof HudModule hud) {
            addRenderableWidget(Button.builder(Component.literal("HUD Preset"), b -> { applyNextHudPreset(hud); saveConfig(); rebuild(); }).bounds(panelX + 271, footerY, 100, 20).build());
            addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> this.minecraft.gui.setScreen(new HudEditorScreen(this))).bounds(panelX + 377, footerY, 100, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveConfig()).bounds(panelX + 455, footerY, 50, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(panelX + 510, footerY, 50, 20).build());
        profileBox = new EditBox(font, panelX + 145, panelY + 305, 150, 20, Component.literal("Profile name"));
        profileBox.setHint(Component.literal("Profile name..."));
        profileBox.setMaxLength(32);
        addRenderableWidget(profileBox);
        addRenderableWidget(Button.builder(Component.literal("Save Profile"), b -> saveProfile()).bounds(panelX + 301, panelY + 305, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Load Profile"), b -> loadProfile()).bounds(panelX + 401, panelY + 305, 95, 20).build());
    }

    private void saveProfile() {
        if (minecraft == null || profileBox == null) return;
        ConfigManager.saveProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue());
        saveConfig();
    }

    private void loadProfile() {
        if (minecraft == null || profileBox == null) return;
        ConfigManager.loadProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue());
        rebuild();
    }

    private static void applyNextHudPreset(HudModule hud) {
        int signature = hud.showCoordinates() ? 1 : 0; signature += hud.showBackground() ? 2 : 0; signature += hud.scale() > 1.04 ? 4 : 0;
        if (signature == 0 || signature == 1) hud.applyPreset("compact"); else if (signature == 2 || signature == 3) hud.applyPreset("full"); else hud.applyPreset("minimal");
    }

    private void addContentWidget(Button button, int y, int panelY) { if (y >= panelY + CONTENT_TOP && y <= panelY + CONTENT_BOTTOM - 18) { addRenderableWidget(button); contentButtons.add(button); } }
    private void saveConfig() { if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules()); }
    private static int nextColor(int current) { for (int i = 0; i < COLOR_PRESETS.length; i++) if (COLOR_PRESETS[i] == current) return COLOR_PRESETS[(i + 1) % COLOR_PRESETS.length]; return COLOR_PRESETS[0]; }
    private static Component moduleLabel(Module module) { return Component.literal((module.enabled() ? "[ON] " : "[OFF] ") + module.name()); }
    private static Component bindLabel(Module module) { return Component.literal(module.hasKeybind() ? "Key: " + module.keyCode() : "Bind"); }
    private static Component settingLabel(BooleanSetting setting) { return Component.literal("  " + setting.name() + ": " + (setting.enabled() ? "ON" : "OFF")); }
    private static Component settingLabel(DoubleSetting setting) { return Component.literal("  " + setting.name() + ": " + String.format(Locale.ROOT, "%.2f", setting.get())); }
    private static Component settingLabel(ColorSetting setting) { return Component.literal("  " + setting.name() + ": #" + String.format(Locale.ROOT, "%08X", setting.get())); }
    private static Component settingLabel(StringSetting setting) { String value = setting.get().isEmpty() ? "<empty>" : setting.get(); return Component.literal("  " + setting.name() + ": " + value); }

    @Override public boolean charTyped(char codePoint, int modifiers) { if (bindingModule == null && (profileBox != null && profileBox.isFocused() || stringBox != null && stringBox.isFocused())) return super.charTyped(codePoint, modifiers); if (bindingModule == null && searchBox != null && searchBox.isFocused()) { boolean handled = super.charTyped(codePoint, modifiers); if (handled) { rebuildContentFromSearch(); return true; } } return super.charTyped(codePoint, modifiers); }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) { if (keyCode == GLFW.GLFW_KEY_ESCAPE) { bindingModule = null; rebuild(); return true; } if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) bindingModule.setKeyCode(0); else bindingModule.setKeyCode(keyCode); bindingModule = null; saveConfig(); rebuild(); return true; }
        if (stringBox != null && stringBox.isFocused()) { if (keyCode == GLFW.GLFW_KEY_ENTER) { finishStringEdit(true); return true; } if (keyCode == GLFW.GLFW_KEY_ESCAPE) { finishStringEdit(false); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
        if (profileBox != null && profileBox.isFocused()) { if (keyCode == GLFW.GLFW_KEY_ENTER) { if (!profileBox.getValue().isBlank()) saveProfile(); return true; } if (keyCode == GLFW.GLFW_KEY_ESCAPE) { profileBox.setFocused(false); return true; } }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rebuildContentFromSearch() { String value = searchBox == null ? "" : searchBox.getValue(); rebuild(); if (searchBox != null) { searchBox.setValue(value); searchBox.setFocused(true); searchBox.moveCursorToEnd(false); } }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) { int panelX = Math.max(20, width / 2 - 280), panelY = Math.max(20, height / 2 - 180); if (mouseX >= panelX + 140 && mouseX <= panelX + 535 && mouseY >= panelY + CONTENT_TOP && mouseY <= panelY + CONTENT_BOTTOM) { int direction = verticalAmount > 0 ? -1 : 1; scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + direction * SCROLL_STEP)); rebuild(); return true; } return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = Math.max(20, width / 2 - 280), panelY = Math.max(20, height / 2 - 180);
        graphics.fill(panelX, panelY, panelX + 560, panelY + 360, 0xE0101014); graphics.outline(panelX, panelY, 560, 360, 0xFF4C4C56);
        graphics.text(font, "Arson Client V3", panelX + 18, panelY + 18, 0xFFFFFFFF, true); graphics.text(font, "Modules / Settings", panelX + 18, panelY + 34, 0xFFAAAAAA, false); graphics.text(font, selectedCategory.displayName(), panelX + 145, panelY + 40, 0xFFFFFFFF, true);
        graphics.text(font, "Profile", panelX + 18, panelY + 311, 0xFFAAAAAA, false);
        if (editingString != null) graphics.text(font, "Editing: " + editingString.name(), panelX + 145, panelY + 267, 0xFFFFAA55, false);
        if (bindingModule != null) graphics.text(font, "Binding: " + bindingModule.name() + " — press a key", panelX + 145, panelY + 48, 0xFFFFAA55, false); else if (maxScroll > 0) graphics.text(font, "Scroll", panelX + 485, panelY + 40, 0xFFAAAAAA, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
    @Override public void onClose() { bindingModule = null; saveConfig(); minecraft.gui.setScreen(parent); }
}
