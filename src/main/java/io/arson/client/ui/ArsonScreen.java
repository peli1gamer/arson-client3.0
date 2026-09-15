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

/** Multi-panel ClickGUI: categories -> modules -> settings. */
public final class ArsonScreen extends Screen {
    private static final int[] COLOR_PRESETS = {0xD6FFFFFF, 0xD6FF5555, 0xD6FFAA00, 0xD6FFFF55, 0xD655FF55, 0xD655FFFF, 0xD655AAFF, 0xD6AA55FF, 0xD6FF55FF, 0xD6AAAAAA};
    private static final int MIN_PANEL_WIDTH = 720;
    private static final int MAX_PANEL_WIDTH = 980;
    private static final int MIN_PANEL_HEIGHT = 420;
    private static final int MAX_PANEL_HEIGHT = 650;
    private static final int HEADER_HEIGHT = 34;
    private static final int FOOTER_HEIGHT = 54;
    private static final int CATEGORY_WIDTH = 126;
    private static final int MODULE_WIDTH = 225;
    private static final int GAP = 6;
    private final Screen parent;
    private Module.Category selectedCategory = Module.Category.RENDER;
    private Module selectedModule;
    private EditBox searchBox;
    private EditBox profileBox;
    private EditBox stringBox;
    private EditBox colorBox;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private Module bindingModule;
    private int panelX;
    private int panelY;
    private int panelWidth = 820;
    private int panelHeight = 500;
    private int settingsScroll;
    private int maxSettingsScroll;
    private boolean draggingPanel;
    private boolean resizingPanel;
    private double dragOffsetX;
    private double dragOffsetY;

    public ArsonScreen(Screen parent) {
        super(Component.literal("Arson Client V3"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = clampInt(panelX == 0 ? width / 2 - panelWidth / 2 : panelX, 8, Math.max(8, width - panelWidth - 8));
        panelY = clampInt(panelY == 0 ? height / 2 - panelHeight / 2 : panelY, 8, Math.max(8, height - panelHeight - 8));
        selectFirstModuleIfNeeded();
        rebuild();
    }

    private int contentTop() { return panelY + HEADER_HEIGHT + 8; }
    private int contentBottom() { return panelY + panelHeight - FOOTER_HEIGHT - 6; }
    private int categoryX() { return panelX + 8; }
    private int moduleX() { return panelX + CATEGORY_WIDTH + GAP; }
    private int settingsX() { return moduleX() + MODULE_WIDTH + GAP; }
    private int settingsWidth() { return panelWidth - CATEGORY_WIDTH - MODULE_WIDTH - GAP * 3 - 16; }

    private void rebuild() {
        clearWidgets();
        panelX = clampInt(panelX, 8, Math.max(8, width - panelWidth - 8));
        panelY = clampInt(panelY, 8, Math.max(8, height - panelHeight - 8));
        selectFirstModuleIfNeeded();

        int searchWidth = Math.max(220, panelWidth - 260);
        searchBox = new EditBox(font, panelX + 170, panelY + 7, searchWidth, 20, Component.literal("Search modules"));
        searchBox.setHint(Component.literal("Search modules..."));
        addRenderableWidget(searchBox);

        addCategoryButtons();
        addModuleButtons();
        addSettingWidgets();
        addFooterWidgets();

        if (editingString != null) {
            stringBox = new EditBox(font, settingsX(), panelY + panelHeight - 78, Math.max(180, settingsWidth() - 115), 20, Component.literal(editingString.name()));
            stringBox.setValue(editingString.get());
            stringBox.setMaxLength(128);
            addRenderableWidget(stringBox);
            stringBox.setFocused(true);
            stringBox.moveCursorToEnd(false);
        }
        if (editingColor != null) {
            colorBox = new EditBox(font, settingsX(), panelY + panelHeight - 78, 180, 20, Component.literal("ARGB hex"));
            colorBox.setValue(String.format(Locale.ROOT, "%08X", editingColor.get()));
            colorBox.setMaxLength(8);
            addRenderableWidget(colorBox);
            colorBox.setFocused(true);
            colorBox.moveCursorToEnd(false);
        }
    }

    private void addCategoryButtons() {
        int y = contentTop();
        for (Module.Category category : Module.Category.values()) {
            Module.Category current = category;
            String icon = categoryIcon(category);
            String label = icon + "  " + category.displayName();
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                selectedCategory = current;
                settingsScroll = 0;
                selectedModule = null;
                bindingModule = null;
                editingString = null;
                editingColor = null;
                selectFirstModuleIfNeeded();
                rebuild();
            }).bounds(categoryX(), y, CATEGORY_WIDTH, 23).build());
            y += 27;
        }
    }

    private void addModuleButtons() {
        String query = query();
        int y = contentTop();
        for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
            if (!matches(module, query)) continue;
            Module current = module;
            String marker = current.enabled() ? "● " : "○ ";
            Button moduleButton = Button.builder(Component.literal(marker + current.name()), b -> {
                selectedModule = current;
                settingsScroll = 0;
                editingString = null;
                editingColor = null;
                rebuild();
            }).bounds(moduleX(), y, MODULE_WIDTH - 74, 23).build();
            addRenderableWidget(moduleButton);
            Button bindButton = Button.builder(Component.literal(bindLabel(current)), b -> {
                bindingModule = current;
                b.setMessage(Component.literal("Press key"));
            }).bounds(moduleX() + MODULE_WIDTH - 70, y, 70, 23).build();
            addRenderableWidget(bindButton);
            y += 27;
            if (y > contentBottom() + 27) break;
        }
    }

    private void addSettingWidgets() {
        if (selectedModule == null) {
            maxSettingsScroll = 0;
            return;
        }
        int y = contentTop() - settingsScroll + 22;
        String previousGroup = "";
        for (Setting<?> setting : selectedModule.settings()) {
            String group = settingGroup(setting);
            if (!group.equals(previousGroup)) {
                y += 22;
                previousGroup = group;
            }
            if (setting instanceof BooleanSetting bool) {
                Button button = Button.builder(settingLabel(bool), b -> {
                    bool.set(!bool.enabled());
                    saveConfig();
                    rebuild();
                }).bounds(settingsX(), y, settingsWidth(), 20).build();
                addSettingWidget(button, y);
            } else if (setting instanceof DoubleSetting number) {
                int usable = settingsWidth();
                Button minus = Button.builder(Component.literal("−"), b -> {
                    number.set(clamp(number.get() - number.step(), number.min(), number.max()));
                    saveConfig();
                    rebuild();
                }).bounds(settingsX(), y, 25, 20).build();
                Button value = Button.builder(settingLabel(number), b -> {
                    number.set(clamp(number.get() + number.step(), number.min(), number.max()));
                    saveConfig();
                    rebuild();
                }).bounds(settingsX() + 29, y, Math.max(80, usable - 58), 20).build();
                Button plus = Button.builder(Component.literal("+"), b -> {
                    number.set(clamp(number.get() + number.step(), number.min(), number.max()));
                    saveConfig();
                    rebuild();
                }).bounds(settingsX() + usable - 25, y, 25, 20).build();
                addSettingWidget(minus, y);
                addSettingWidget(value, y);
                addSettingWidget(plus, y);
            } else if (setting instanceof ColorSetting color) {
                Button value = Button.builder(settingLabel(color), b -> {
                    color.set(nextColor(color.get()));
                    saveConfig();
                    rebuild();
                }).bounds(settingsX(), y, Math.max(90, settingsWidth() - 58), 20).build();
                Button edit = Button.builder(Component.literal("Edit"), b -> beginColorEdit(color)).bounds(settingsX() + settingsWidth() - 52, y, 52, 20).build();
                addSettingWidget(value, y);
                addSettingWidget(edit, y);
            } else if (setting instanceof StringSetting text) {
                Button button = Button.builder(settingLabel(text), b -> beginStringEdit(text)).bounds(settingsX(), y, settingsWidth(), 20).build();
                addSettingWidget(button, y);
            }
            y += 25;
        }
        int visible = Math.max(100, contentBottom() - contentTop());
        maxSettingsScroll = Math.max(0, y - contentTop() - visible);
        if (settingsScroll > maxSettingsScroll) settingsScroll = maxSettingsScroll;
    }

    private void addSettingWidget(Button button, int y) {
        if (y >= contentTop() && y <= contentBottom() - 20) addRenderableWidget(button);
    }

    private void addFooterWidgets() {
        int y = panelY + panelHeight - 48;
        int base = settingsX();
        addRenderableWidget(Button.builder(Component.literal("Reset Category"), b -> {
            for (Module module : ArsonClient.getInstance().modules().all()) {
                if (module.category() == selectedCategory) module.resetToDefaults();
            }
            saveConfig();
            rebuild();
        }).bounds(base, y, 105, 20).build());

        Module hudModule = ArsonClient.getInstance().modules().get("hud");
        if (hudModule instanceof HudModule hud) {
            addRenderableWidget(Button.builder(Component.literal("HUD Preset"), b -> {
                applyNextHudPreset(hud);
                saveConfig();
                rebuild();
            }).bounds(base + 111, y, 88, 20).build());
            addRenderableWidget(Button.builder(Component.literal("HUD Editor"), b -> minecraft.gui.setScreen(new HudEditorScreen(this))).bounds(base + 205, y, 88, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveConfig()).bounds(panelX + panelWidth - 106, y, 48, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(panelX + panelWidth - 54, y, 48, 20).build());

        if (editingString == null && editingColor == null) {
            profileBox = new EditBox(font, base, y - 26, 145, 20, Component.literal("Profile name"));
            profileBox.setHint(Component.literal("Profile name..."));
            profileBox.setMaxLength(32);
            addRenderableWidget(profileBox);
            addRenderableWidget(Button.builder(Component.literal("Save Profile"), b -> saveProfile()).bounds(base + 151, y - 26, 95, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Load Profile"), b -> loadProfile()).bounds(base + 252, y - 26, 95, 20).build());
        }

        int editorY = y - 26;
        if (editingString != null) {
            addRenderableWidget(Button.builder(Component.literal("Apply"), b -> finishStringEdit(true)).bounds(base + Math.max(0, settingsWidth() - 105), editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> finishStringEdit(false)).bounds(base + Math.max(52, settingsWidth() - 53), editorY, 48, 20).build());
        } else if (editingColor != null) {
            addRenderableWidget(Button.builder(Component.literal("Apply"), b -> finishColorEdit(true)).bounds(base + Math.max(0, settingsWidth() - 157), editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> finishColorEdit(false)).bounds(base + Math.max(52, settingsWidth() - 105), editorY, 48, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Preset"), b -> {
                editingColor.set(nextColor(editingColor.get()));
                rebuild();
            }).bounds(base + Math.max(104, settingsWidth() - 53), editorY, 53, 20).build());
        }
    }

    private void selectFirstModuleIfNeeded() {
        if (selectedModule != null && selectedModule.category() == selectedCategory && matches(selectedModule, query())) return;
        selectedModule = null;
        for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
            if (matches(module, query())) {
                selectedModule = module;
                return;
            }
        }
    }

    private String query() {
        return searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matches(Module module, String query) {
        return query.isEmpty() || module.name().toLowerCase(Locale.ROOT).contains(query) || module.id().toLowerCase(Locale.ROOT).contains(query);
    }

    private void beginStringEdit(StringSetting setting) {
        editingColor = null;
        editingString = setting;
        rebuild();
    }

    private void beginColorEdit(ColorSetting setting) {
        editingString = null;
        editingColor = setting;
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

    private void finishColorEdit(boolean save) {
        if (editingColor != null && colorBox != null && save) {
            String value = colorBox.getValue().trim();
            if (value.startsWith("#")) value = value.substring(1);
            try {
                if (!value.isEmpty() && value.length() <= 8) editingColor.set((int) Long.parseLong(value, 16));
                saveConfig();
            } catch (NumberFormatException ignored) {
                // Keep the current color when the field is invalid.
            }
        }
        editingColor = null;
        colorBox = null;
        rebuild();
    }

    private void saveProfile() {
        if (minecraft == null || profileBox == null || profileBox.getValue().isBlank()) return;
        ConfigManager.saveProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue());
        saveConfig();
    }

    private void loadProfile() {
        if (minecraft == null || profileBox == null || profileBox.getValue().isBlank()) return;
        ConfigManager.loadProfile(minecraft, ArsonClient.getInstance().modules(), profileBox.getValue());
        rebuild();
    }

    private static void applyNextHudPreset(HudModule hud) {
        int signature = hud.showCoordinates() ? 1 : 0;
        signature += hud.showBackground() ? 2 : 0;
        signature += hud.scale() > 1.04 ? 4 : 0;
        if (signature == 0 || signature == 1) hud.applyPreset("compact");
        else if (signature == 2 || signature == 3) hud.applyPreset("full");
        else hud.applyPreset("minimal");
    }

    private static String categoryIcon(Module.Category category) {
        return switch (category) {
            case COMBAT -> "C";
            case MOVEMENT -> "M";
            case RENDER -> "R";
            case PLAYER -> "P";
            case WORLD -> "W";
            case MISC -> "X";
        };
    }

    private static String settingGroup(Setting<?> setting) {
        String name = setting.name().toLowerCase(Locale.ROOT);
        if (name.contains("color") || name.contains("alpha") || name.contains("style") || name.contains("fill") || name.contains("outline") || name.contains("width") || name.contains("background") || name.contains("fade") || name.contains("scale")) return "Visual";
        if (name.contains("range") || name.contains("scan") || name.contains("interval") || name.contains("delay") || name.contains("target")) return "Behavior";
        return "General";
    }

    private static int nextColor(int current) {
        for (int i = 0; i < COLOR_PRESETS.length; i++) {
            if (COLOR_PRESETS[i] == current) return COLOR_PRESETS[(i + 1) % COLOR_PRESETS.length];
        }
        return COLOR_PRESETS[0];
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String bindLabel(Module module) {
        return module.keyCode() == 0 ? "Bind" : "Key " + module.keyCode();
    }

    private static Component settingLabel(BooleanSetting setting) {
        return Component.literal(setting.name() + ": " + (setting.enabled() ? "ON" : "OFF"));
    }

    private static Component settingLabel(DoubleSetting setting) {
        return Component.literal(setting.name() + ": " + String.format(Locale.ROOT, "%.2f", setting.get()));
    }

    private static Component settingLabel(ColorSetting setting) {
        return Component.literal(setting.name() + ": #" + String.format(Locale.ROOT, "%08X", setting.get()));
    }

    private static Component settingLabel(StringSetting setting) {
        String value = setting.get().isEmpty() ? "<empty>" : setting.get();
        return Component.literal(setting.name() + ": " + value);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (bindingModule == null && ((profileBox != null && profileBox.isFocused()) || (stringBox != null && stringBox.isFocused()) || (colorBox != null && colorBox.isFocused()))) return super.charTyped(codePoint, modifiers);
        if (bindingModule == null && searchBox != null && searchBox.isFocused()) {
            boolean handled = super.charTyped(codePoint, modifiers);
            if (handled) rebuildContentFromSearch();
            return handled;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                rebuild();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) bindingModule.setKeyCode(0);
            else bindingModule.setKeyCode(keyCode);
            bindingModule = null;
            saveConfig();
            rebuild();
            return true;
        }
        if (stringBox != null && stringBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) { finishStringEdit(true); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { finishStringEdit(false); return true; }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (colorBox != null && colorBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) { finishColorEdit(true); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { finishColorEdit(false); return true; }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (profileBox != null && profileBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) { saveProfile(); return true; }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { profileBox.setFocused(false); return true; }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= panelX + 8 && mouseX <= panelX + panelWidth - 8 && mouseY >= panelY + 1 && mouseY <= panelY + HEADER_HEIGHT - 2) {
            draggingPanel = true;
            dragOffsetX = mouseX - panelX;
            dragOffsetY = mouseY - panelY;
            return true;
        }
        if (button == 0 && mouseX >= panelX + panelWidth - 18 && mouseX <= panelX + panelWidth + 2 && mouseY >= panelY + panelHeight - 18 && mouseY <= panelY + panelHeight + 2) {
            resizingPanel = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPanel && button == 0) {
            panelX = clampInt((int) (mouseX - dragOffsetX), 8, Math.max(8, width - panelWidth - 8));
            panelY = clampInt((int) (mouseY - dragOffsetY), 8, Math.max(8, height - panelHeight - 8));
            rebuild();
            return true;
        }
        if (resizingPanel && button == 0) {
            panelWidth = clampInt((int) (mouseX - panelX), MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, width - panelX - 8));
            panelHeight = clampInt((int) (mouseY - panelY), MIN_PANEL_HEIGHT, Math.min(MAX_PANEL_HEIGHT, height - panelY - 8));
            rebuild();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (draggingPanel || resizingPanel)) {
            draggingPanel = false;
            resizingPanel = false;
            rebuild();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void rebuildContentFromSearch() {
        String value = searchBox == null ? "" : searchBox.getValue();
        rebuild();
        if (searchBox != null) {
            searchBox.setValue(value);
            searchBox.setFocused(true);
            searchBox.moveCursorToEnd(false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= settingsX() && mouseX <= panelX + panelWidth - 8 && mouseY >= contentTop() && mouseY <= contentBottom()) {
            int direction = verticalAmount > 0 ? -1 : 1;
            settingsScroll = Math.max(0, Math.min(maxSettingsScroll, settingsScroll + direction * 24));
            rebuild();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE00F1015);
        graphics.outline(panelX, panelY, panelWidth, panelHeight, 0xFF4C4C56);
        graphics.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + HEADER_HEIGHT, 0xD0181820);
        graphics.text(font, "Arson Client V3", panelX + 12, panelY + 10, 0xFFFFFFFF, true);
        graphics.text(font, "MULTI-PANEL", panelX + 12, panelY + 22, 0xFF777783, false);
        graphics.text(font, "Enabled: " + ArsonClient.getInstance().modules().enabledCount(), panelX + panelWidth - 92, panelY + 10, 0xFFAAAAAA, false);

        int top = contentTop();
        graphics.fill(panelX + 8, top - 4, panelX + CATEGORY_WIDTH, contentBottom(), 0x50161A21);
        graphics.fill(moduleX(), top - 4, moduleX() + MODULE_WIDTH, contentBottom(), 0x40161A21);
        graphics.fill(settingsX(), top - 4, panelX + panelWidth - 8, contentBottom(), 0x30161A21);
        graphics.outline(panelX + 8, top - 4, CATEGORY_WIDTH - 8, contentBottom() - top + 4, 0x334C4C56);
        graphics.outline(moduleX(), top - 4, MODULE_WIDTH, contentBottom() - top + 4, 0x334C4C56);

        graphics.text(font, "CATEGORIES", panelX + 15, top - 1, 0xFF777783, true);
        graphics.text(font, "MODULES", moduleX() + 8, top - 1, 0xFF777783, true);
        graphics.text(font, selectedModule == null ? "SETTINGS" : selectedModule.name().toUpperCase(Locale.ROOT), settingsX() + 8, top - 1, 0xFF777783, true);

        int categoryY = top + 22;
        for (Module.Category category : Module.Category.values()) {
            if (category == selectedCategory) graphics.fill(categoryX() + 2, categoryY - 1, categoryX() + CATEGORY_WIDTH - 2, categoryY + 22, 0x553F78FF);
            categoryY += 27;
        }

        int moduleY = top + 22;
        String query = query();
        for (Module module : ArsonClient.getInstance().modules().organized(selectedCategory)) {
            if (!matches(module, query)) continue;
            if (module == selectedModule) graphics.fill(moduleX() + 2, moduleY - 1, moduleX() + MODULE_WIDTH - 2, moduleY + 22, 0x554C78FF);
            if (module.enabled()) graphics.fill(moduleX() + 2, moduleY - 1, moduleX() + 5, moduleY + 22, 0xFF55DD88);
            moduleY += 27;
            if (moduleY > contentBottom()) break;
        }

        if (selectedModule != null) {
            String group = "";
            int groupY = top + 22 - settingsScroll;
            for (Setting<?> setting : selectedModule.settings()) {
                String currentGroup = settingGroup(setting);
                if (!currentGroup.equals(group)) {
                    group = currentGroup;
                    groupY += 22;
                    if (groupY >= top && groupY <= contentBottom()) {
                        graphics.text(font, group.toUpperCase(Locale.ROOT), settingsX() + 2, groupY - 1, 0xFF8E8E9A, true);
                    }
                }
                groupY += 25;
            }
        }

        graphics.text(font, "Profile", settingsX(), panelY + panelHeight - 72, 0xFF777783, false);
        if (editingString != null) graphics.text(font, "Editing: " + editingString.name(), settingsX(), panelY + panelHeight - 91, 0xFFFFAA55, false);
        if (editingColor != null) graphics.text(font, "Color: " + editingColor.name(), settingsX(), panelY + panelHeight - 91, 0xFFFFAA55, false);
        if (bindingModule != null) graphics.text(font, "Binding: " + bindingModule.name() + " — press a key", settingsX(), panelY + 48, 0xFFFFAA55, false);
        else if (maxSettingsScroll > 0) graphics.text(font, "Scroll settings", panelX + panelWidth - 88, panelY + 48, 0xFF777783, false);

        graphics.fill(panelX + panelWidth - 12, panelY + panelHeight - 12, panelX + panelWidth - 3, panelY + panelHeight - 3, 0xFF777777);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        bindingModule = null;
        saveConfig();
        minecraft.gui.setScreen(parent);
    }
}