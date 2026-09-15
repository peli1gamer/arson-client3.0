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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ArsonScreen extends Screen {
    private static final int[] COLOR_PRESETS = {0xD6FFFFFF, 0xD6FF5555, 0xD6FFAA00, 0xD6FFFF55, 0xD655FF55, 0xD655FFFF, 0xD655AAFF, 0xD6AA55FF, 0xD6FF55FF, 0xD6AAAAAA};
    private static final int MIN_PANEL_WIDTH = 500;
    private static final int MAX_PANEL_WIDTH = 760;
    private static final int MIN_PANEL_HEIGHT = 330;
    private static final int MAX_PANEL_HEIGHT = 560;
    private static final int CONTENT_TOP = 58;
    private static final int CONTENT_BOTTOM = 320;
    private static final int SCROLL_STEP = 24;
    private final Screen parent;
    private Module.Category selectedCategory = Module.Category.RENDER;
    private final List<Button> contentButtons = new ArrayList<>();
    private final Set<String> collapsedModules = new HashSet<>();
    private int scrollOffset;
    private int maxScroll;
    private Module bindingModule;
    private EditBox searchBox;
    private EditBox profileBox;
    private EditBox stringBox;
    private EditBox colorBox;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private int panelX;
    private int panelY;
    private int panelWidth = 560;
    private int panelHeight = 360;
    private boolean draggingPanel;
    private boolean resizingPanel;
    private double dragOffsetX;
    private double dragOffsetY;

    public ArsonScreen(Screen parent) { super(Component.literal("Arson Client V3")); this.parent = parent; }

    @Override protected void init() {
        panelX = clampInt(panelX == 0 ? width / 2 - panelWidth / 2 : panelX, 10, Math.max(10, width - panelWidth - 10));
        panelY = clampInt(panelY == 0 ? height / 2 - panelHeight / 2 : panelY, 10, Math.max(10, height - panelHeight - 10));
        rebuild();
    }

    private void rebuild() {
        clearWidgets(); contentButtons.clear();
        panelX = clampInt(panelX, 10, Math.max(10, width - panelWidth - 10));
        panelY = clampInt(panelY, 10, Math.max(10, height - panelHeight - 10));
        searchBox = new EditBox(font, panelX + 145, panelY + 14, Math.max(180, panelWidth - 295), 20, Component.literal("Search modules"));
        searchBox.setHint(Component.literal("Search modules...")); addRenderableWidget(searchBox);

        int categoryX = panelX + 18, categoryY = panelY + 58;
        for (Module.Category category : Module.Category.values()) {
            Module.Category current = category;
            String label = current.displayName() + " (" + ArsonClient.getInstance().modules().enabledCount(current) + "/" + ArsonClient.getInstance().modules().categoryCount(current) + ")";
            addRenderableWidget(Button.builder(Component.literal(label), b -> { selectedCategory = current; bindingModule = null; scrollOffset = 0; rebuild(); }).bounds(categoryX, categoryY, 105, 20).build());
            categoryY += 24;
        }
        addContent(panelX, panelY); addFooter(panelX, panelY);

        if (editingString != null) {
            stringBox = new EditBox(font, panelX + 145, panelY + panelHeight - 78, Math.max(180, panelWidth - 295), 20, Component.literal(editingString.name()));
            stringBox.setValue(editingString.get()); stringBox.setMaxLength(128);
            addRenderableWidget(stringBox); stringBox.setFocused(true); stringBox.moveCursorToEnd(false);
        }
        if (editingColor != null) {
            colorBox = new EditBox(font, panelX + 145, panelY + panelHeight - 78, 180, 20, Component.literal("ARGB hex"));
            colorBox.setValue(String.format(Locale.ROOT, "%08X", editingColor.get())); colorBox.setMaxLength(8);
            addRenderableWidget(colorBox); colorBox.setFocused(true); colorBox.moveCursorToEnd(false);
        }
    }

    private boolean matches(Module module) {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        return query.isEmpty() || module.name().toLowerCase(Locale.ROOT).contains(query) || module.id().toLowerCase(Locale.ROOT).contains(query);
    }

    private void addContent(int xPanel, int yPanel) {
        int x = xPanel + 145, y = yPanel + CONTENT_TOP - scrollOffset, contentHeight = 0;
        int settingWidth = Math.max(300, panelWidth - 175);
        for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
            if (!matches(module)) continue;
            Module current = module;
            Button moduleButton = Button.builder(moduleLabel(current), button -> { current.toggle(); saveConfig(); rebuild(); }).bounds(x, y, Math.max(220, settingWidth - 75), 21).build();
            addContentWidget(moduleButton, y, yPanel);
            Button bindButton = Button.builder(Component.literal(bindLabel(current)), button -> { bindingModule = current; button.setMessage(Component.literal("Press key")); }).bounds(x + Math.max(220, settingWidth - 70), y, 70, 21).build();
            addContentWidget(bindButton, y, yPanel);
            y += 25; contentHeight += 25;

            if (collapsedModules.contains(current.id())) continue;
            for (Setting<?> setting : current.settings()) {
                if (setting instanceof BooleanSetting bool) {
                    Button settingButton = Button.builder(settingLabel(bool), button -> { bool.set(!bool.enabled()); button.setMessage(settingLabel(bool)); saveConfig(); }).bounds(x + 14, y, settingWidth - 14, 18).build();
                    addContentWidget(settingButton, y, yPanel); y += 21; contentHeight += 21;
                } else if (setting instanceof DoubleSetting number) {
                    int usable = Math.max(250, settingWidth - 14);
                    Button minus = Button.builder(Component.literal("-"), button -> { number.set(clamp(number.get() - number.step(), number.min(), number.max())); saveConfig(); rebuild(); }).bounds(x + 14, y, 24, 18).build();
                    Button value = Button.builder(settingLabel(number), button -> { number.set(clamp(number.get() + number.step(), number.min(), number.max())); saveConfig(); rebuild(); }).bounds(x + 40, y, usable - 50, 18).build();
                    Button plus = Button.builder(Component.literal("+"), button -> { number.set(clamp(number.get() + number.step(), number.min(), number.max())); saveConfig(); rebuild(); }).bounds(x + usable - 10, y, 24, 18).build();
                    addContentWidget(minus, y, yPanel); addContentWidget(value, y, yPanel); addContentWidget(plus, y, yPanel);
                    y += 21; contentHeight += 21;
                } else if (setting instanceof ColorSetting color) {
                    Button settingButton = Button.builder(settingLabel(color), button -> { color.set(nextColor(color.get())); saveConfig(); rebuild(); }).bounds(x + 14, y, settingWidth - 70, 18).build();
                    Button edit = Button.builder(Component.literal("Edit"), button -> beginColorEdit(color)).bounds(x + settingWidth - 52, y, 48, 18).build();
                    addContentWidget(settingButton, y, yPanel); addContentWidget(edit, y, yPanel);
                    y += 21; contentHeight += 21;
                } else if (setting instanceof StringSetting text) {
                    Button settingButton = Button.builder(settingLabel(text), button -> beginStringEdit(text)).bounds(x + 14, y, settingWidth - 14, 18).build();
                    addContentWidget(settingButton, y, yPanel); y += 21; contentHeight += 21;
                }
            }
            y += 6; contentHeight += 6;
        }
        int visibleHeight = Math.max(100, panelHeight - 98);
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private void beginStringEdit(StringSetting setting) { editingColor = null; editingString = setting; rebuild(); }

    private void beginColorEdit(ColorSetting setting) { editingString = null; editingColor = setting; rebuild(); }

    private void finishStringEdit(boolean save) {
        if (editingString != null && stringBox != null && save) { editingString.set(stringBox.getValue()); saveConfig(); }
        editingString = null; stringBox = null; rebuild();
    }

    private void finishColorEdit(boolean save) {
        if (editingColor != null && colorBox != null && save) {
            String value = colorBox.getValue().trim();
            if (value.startsWith("#")) value = value.substring(1);
            try {
                if (value.length() <= 8) editingColor.set((int) Long.parseLong(value, 16));
                saveConfig();
            } catch (NumberFormatException ignored) { }
        }
        editingColor = null; colorBox = null; rebuild();
    }

    private void addFooter(int xPanel, int yPanel) {
        int footerY = yPanel + panelHeight - 30;
        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> { for (Module module : ArsonClient.getInstance().modules().all()) if (module.category() == selectedCategory) module.resetToDefaults(); saveConfig(); rebuild(); }).bounds(xPanel + 145, footerY, 70, 20).build());
        Module hudModule = ArsonClient.getInstance().modules().get("hud");
        if (hudModule instanceof HudModule hud) {
            addRenderableWidget(Button.builder(Component.literal("HUD Preset"), b -> { applyNextHudPreset(hud); saveConfig(); rebuild(); }).bounds(xPanel + 219, footerY, 88, 20).build());
            addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> this.minecraft.gui.setScreen(new HudEditorScreen(this))).bounds(xPanel + 311, footerY, 88, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveConfig()).bounds(xPanel + panelWidth - 105, footerY, 48, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(xPanel + panelWidth - 53, footerY, 48, 20).build());

        int editorY = yPanel + panelHeight - 54;
        if (editingString != null) {
            addRenderableWidget(Button.builder(Component.literal("Apply"), b -> finishStringEdit(true)).bounds(xPanel + panelWidth - 105, editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> finishStringEdit(false)).bounds(xPanel + panelWidth - 53, editorY, 48, 20).build());
        } else if (editingColor != null) {
            addRenderableWidget(Button.builder(Component.literal("Apply"), b -> finishColorEdit(true)).bounds(xPanel + 330, editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> finishColorEdit(false)).bounds(xPanel + 382, editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Preset"), b -> { editingColor.set(nextColor(editingColor.get())); rebuild(); }).bounds(xPanel + 434, editorY, 58, 20).build());
        }

        profileBox = new EditBox(font, xPanel + 145, yPanel + panelHeight - 78, 150, 20, Component.literal("Profile name"));
        profileBox.setHint(Component.literal("Profile name...")); profileBox.setMaxLength(32);
        if (editingString == null && editingColor == null) addRenderableWidget(profileBox);
        if (editingString == null && editingColor == null) {
            addRenderableWidget(Button.builder(Component.literal("Save Profile"), b -> saveProfile()).bounds(xPanel + 301, yPanel + panelHeight - 78, 95, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Load Profile"), b -> loadProfile()).bounds(xPanel + 401, yPanel + panelHeight - 78, 95, 20).build());
        }
    }

    private void saveProfile() { if (minecraft == null || profileBox == null || profileBox.getValue().isBlank()) return; ConfigManager.saveProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue()); saveConfig(); }
    private void loadProfile() { if (minecraft == null || profileBox == null || profileBox.getValue().isBlank()) return; ConfigManager.loadProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue()); rebuild(); }

    private static void applyNextHudPreset(HudModule hud) {
        int signature = hud.showCoordinates() ? 1 : 0; signature += hud.showBackground() ? 2 : 0; signature += hud.scale() > 1.04 ? 4 : 0;
        if (signature == 0 || signature == 1) hud.applyPreset("compact"); else if (signature == 2 || signature == 3) hud.applyPreset("full"); else hud.applyPreset("minimal");
    }

    private void addContentWidget(Button button, int y, int yPanel) { if (y >= yPanel + CONTENT_TOP && y <= yPanel + panelHeight - 40) { addRenderableWidget(button); contentButtons.add(button); } }
    private void saveConfig() { if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules()); }
    private static int nextColor(int current) { for (int i = 0; i < COLOR_PRESETS.length; i++) if (COLOR_PRESETS[i] == current) return COLOR_PRESETS[(i + 1) % COLOR_PRESETS.length]; return COLOR_PRESETS[0]; }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    private static int clampInt(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static String bindLabel(Module module) { return module.keyCode() == 0 ? "Bind" : "Key " + module.keyCode(); }
    private static Component moduleLabel(Module module) { return Component.literal((module.enabled() ? "[ON] " : "[OFF] ") + (module.settings().isEmpty() ? "" : "> ") + module.name()); }
    private static Component settingLabel(BooleanSetting setting) { return Component.literal("  " + setting.name() + ": " + (setting.enabled() ? "ON" : "OFF")); }
    private static Component settingLabel(DoubleSetting setting) { return Component.literal("  " + setting.name() + ": " + String.format(Locale.ROOT, "%.2f", setting.get())); }
    private static Component settingLabel(ColorSetting setting) { return Component.literal("  " + setting.name() + ": #" + String.format(Locale.ROOT, "%08X", setting.get())); }
    private static Component settingLabel(StringSetting setting) { String value = setting.get().isEmpty() ? "<empty>" : setting.get(); return Component.literal("  " + setting.name() + ": " + value); }

    @Override public boolean charTyped(char codePoint, int modifiers) {
        if (bindingModule == null && ((profileBox != null && profileBox.isFocused()) || (stringBox != null && stringBox.isFocused()) || (colorBox != null && colorBox.isFocused()))) return super.charTyped(codePoint, modifiers);
        if (bindingModule == null && searchBox != null && searchBox.isFocused()) { boolean handled = super.charTyped(codePoint, modifiers); if (handled) { rebuildContentFromSearch(); return true; } }
        return super.charTyped(codePoint, modifiers);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) { if (keyCode == GLFW.GLFW_KEY_ESCAPE) { bindingModule = null; rebuild(); return true; } if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) bindingModule.setKeyCode(0); else bindingModule.setKeyCode(keyCode); bindingModule = null; saveConfig(); rebuild(); return true; }
        if (stringBox != null && stringBox.isFocused()) { if (keyCode == GLFW.GLFW_KEY_ENTER) { finishStringEdit(true); return true; } if (keyCode == GLFW.GLFW_KEY_ESCAPE) { finishStringEdit(false); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
        if (colorBox != null && colorBox.isFocused()) { if (keyCode == GLFW.GLFW_KEY_ENTER) { finishColorEdit(true); return true; } if (keyCode == GLFW.GLFW_KEY_ESCAPE) { finishColorEdit(false); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
        if (profileBox != null && profileBox.isFocused()) { if (keyCode == GLFW.GLFW_KEY_ENTER) { saveProfile(); return true; } if (keyCode == GLFW.GLFW_KEY_ESCAPE) { profileBox.setFocused(false); return true; } }
        if (keyCode == GLFW.GLFW_KEY_SPACE && searchBox != null && !searchBox.isFocused()) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= panelX + 8 && mouseX <= panelX + panelWidth - 8 && mouseY >= panelY + 4 && mouseY <= panelY + 30) {
            draggingPanel = true; dragOffsetX = mouseX - panelX; dragOffsetY = mouseY - panelY; return true;
        }
        if (button == 0 && mouseX >= panelX + panelWidth - 18 && mouseX <= panelX + panelWidth + 2 && mouseY >= panelY + panelHeight - 18 && mouseY <= panelY + panelHeight + 2) {
            resizingPanel = true; dragOffsetX = mouseX; dragOffsetY = mouseY; return true;
        }
        int x = panelX + 145, y = panelY + CONTENT_TOP - scrollOffset;
        if (mouseX >= x && mouseX <= panelX + panelWidth - 10) {
            for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
                if (!matches(module)) continue;
                if (mouseY >= y && mouseY <= y + 21) {
                    if (module.settings().isEmpty()) break;
                    if (button == 1) { if (collapsedModules.contains(module.id())) collapsedModules.remove(module.id()); else collapsedModules.add(module.id()); rebuild(); return true; }
                    break;
                }
                y += 25;
                if (collapsedModules.contains(module.id())) continue;
                for (Setting<?> ignored : module.settings()) y += 21;
                y += 6;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPanel && button == 0) {
            panelX = clampInt((int) (mouseX - dragOffsetX), 10, Math.max(10, width - panelWidth - 10));
            panelY = clampInt((int) (mouseY - dragOffsetY), 10, Math.max(10, height - panelHeight - 10));
            rebuild(); return true;
        }
        if (resizingPanel && button == 0) {
            panelWidth = clampInt((int) (mouseX - panelX), MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, width - panelX - 10));
            panelHeight = clampInt((int) (mouseY - panelY), MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, height - panelY - 10));
            rebuild(); return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingPanel || resizingPanel)) { draggingPanel = false; resizingPanel = false; rebuild(); return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void rebuildContentFromSearch() { String value = searchBox == null ? "" : searchBox.getValue(); rebuild(); if (searchBox != null) { searchBox.setValue(value); searchBox.setFocused(true); searchBox.moveCursorToEnd(false); } }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= panelX + 140 && mouseX <= panelX + panelWidth - 10 && mouseY >= panelY + CONTENT_TOP && mouseY <= panelY + panelHeight - 40) {
            int direction = verticalAmount > 0 ? -1 : 1; scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + direction * SCROLL_STEP)); rebuild(); return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0101014);
        graphics.outline(panelX, panelY, panelWidth, panelHeight, 0xFF4C4C56);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + 31, 0xD0181820);
        graphics.text(font, "Arson Client V3", panelX + 18, panelY + 10, 0xFFFFFFFF, true);
        graphics.text(font, "Modules / Settings", panelX + 18, panelY + 24, 0xFFAAAAAA, false);
        graphics.text(font, selectedCategory.displayName(), panelX + 145, panelY + 40, 0xFFFFFFFF, true);
        graphics.text(font, "Enabled: " + ArsonClient.getInstance().modules().enabledCount(), panelX + panelWidth - 145, panelY + 40, 0xFFAAAAAA, false);

        int categoryY = panelY + 58;
        for (Module.Category category : Module.Category.values()) {
            if (category == selectedCategory) graphics.fill(panelX + 15, categoryY - 1, panelX + 126, categoryY + 21, 0x553F78FF);
            categoryY += 24;
        }

        int x = panelX + 145, y = panelY + CONTENT_TOP - scrollOffset;
        for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
            if (!matches(module)) continue;
            if (y >= panelY + CONTENT_TOP - 21 && y <= panelY + panelHeight - 42) {
                int accent = module.enabled() ? 0x553FFF88 : 0x22101010;
                graphics.fill(x - 2, y - 1, panelX + panelWidth - 10, y + 21, accent);
            }
            y += 25;
            if (collapsedModules.contains(module.id())) continue;
            y += module.settings().size() * 21 + 6;
        }

        graphics.text(font, "Profile", panelX + 18, panelY + panelHeight - 72, 0xFFAAAAAA, false);
        if (editingString != null) graphics.text(font, "Editing: " + editingString.name(), panelX + 145, panelY + panelHeight - 91, 0xFFFFAA55, false);
        if (editingColor != null) graphics.text(font, "Color: " + editingColor.name() + "  ARGB hex", panelX + 145, panelY + panelHeight - 91, 0xFFFFAA55, false);
        if (bindingModule != null) graphics.text(font, "Binding: " + bindingModule.name() + " — press a key", panelX + 145, panelY + 48, 0xFFFFAA55, false);
        else if (maxScroll > 0) graphics.text(font, "Scroll", panelX + panelWidth - 55, panelY + 40, 0xFFAAAAAA, false);

        graphics.fill(panelX + panelWidth - 12, panelY + panelHeight - 12, panelX + panelWidth - 3, panelY + panelHeight - 3, 0xFF777777);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { bindingModule = null; saveConfig(); minecraft.gui.setScreen(parent); }
}
