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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Windowed, Meteor-inspired ClickGUI. Each module category has its own compact
 * panel; module activation is immediate and right-click opens its settings.
 */
public final class ArsonScreen extends Screen {
    private static final int[] CATEGORY_COLORS = {
            0xFFE46D73, 0xFF65B7E8, 0xFF9B83E8, 0xFF65C99A, 0xFFE0B35F, 0xFF9AA5B5
    };
    private final Screen parent;
    private final int[] windowOffsetX = new int[Module.Category.values().length];
    private final int[] windowOffsetY = new int[Module.Category.values().length];
    private final int[] windowScroll = new int[Module.Category.values().length];
    private final boolean[] collapsed = new boolean[Module.Category.values().length];

    private EditBox search;
    private EditBox valueEditor;
    private EditBox profile;
    private StringSetting editingString;
    private ColorSetting editingColor;
    private Module selected;
    private Module bindingModule;
    private Module.Category activeCategory = Module.Category.RENDER;
    private int draggingWindow = -1;
    private double dragOffsetX;
    private double dragOffsetY;
    private int settingsScroll;
    private boolean searchFocused;
    private boolean favoritesOnly;
    private boolean enabledOnly;
    private boolean alphabetical;
    private ClickGuiPreferencesModule.Theme theme = ClickGuiPreferencesModule.Theme.MIDNIGHT;

    public ArsonScreen(Screen parent) {
        super(Component.literal("Arson Client V3"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        syncPreferences();
        rebuildWidgets();
    }

    private void syncPreferences() {
        ClickGuiPreferencesModule p = preferences();
        if (p == null) return;
        theme = p.theme();
        alphabetical = p.alphabetical();
        favoritesOnly = p.favoritesOnly();
        enabledOnly = p.enabledOnly();
    }

    private ClickGuiPreferencesModule preferences() {
        Module module = ArsonClient.getInstance().modules().get("clickgui-preferences");
        return module instanceof ClickGuiPreferencesModule p ? p : null;
    }

    private void savePreferences() {
        ClickGuiPreferencesModule p = preferences();
        if (p != null) {
            p.setAlphabetical(alphabetical);
            p.setFavoritesOnly(favoritesOnly);
            p.setEnabledOnly(enabledOnly);
            while (p.theme() != theme) p.cycleTheme();
        }
        saveConfig();
    }

    private void rebuildWidgets() {
        String searchValue = search == null ? "" : search.getValue();
        String profileValue = profile == null ? "" : profile.getValue();
        String editValue = valueEditor == null ? "" : valueEditor.getValue();
        clearWidgets();
        search = null;
        profile = null;
        valueEditor = null;

        int headerY = 12;
        int searchWidth = width < 520 ? Math.max(80, width - 190) : Math.max(100, Math.min(230, width - 420));
        search = new EditBox(font, 112, headerY, searchWidth, 22, Component.literal("Search modules"));
        search.setHint(Component.literal("Search modules..."));
        search.setValue(searchValue);
        search.setFocused(searchFocused);
        addRenderableWidget(search);

        addRenderableWidget(Button.builder(Component.literal("HUD"), b -> {
            if (ArsonClient.getInstance().modules().get("hud") instanceof HudModule) {
                minecraft.setScreen(new HudEditorScreen(this));
            }
        }).bounds(width - (width < 520 ? 60 : 270), headerY, 48, 22).build());

        if (width >= 520) {
            addRenderableWidget(Button.builder(Component.literal("Theme"), b -> {
                ClickGuiPreferencesModule.Theme[] themes = ClickGuiPreferencesModule.Theme.values();
                theme = themes[(theme.ordinal() + 1) % themes.length];
                savePreferences();
                rebuildWidgets();
            }).bounds(width - 216, headerY, 54, 22).build());

            addRenderableWidget(Button.builder(Component.literal(favoritesOnly ? "★" : "☆"), b -> {
                favoritesOnly = !favoritesOnly;
                savePreferences();
                rebuildWidgets();
            }).bounds(width - 156, headerY, 28, 22).build());

            addRenderableWidget(Button.builder(Component.literal(enabledOnly ? "ON" : "ALL"), b -> {
                enabledOnly = !enabledOnly;
                savePreferences();
                rebuildWidgets();
            }).bounds(width - 124, headerY, 38, 22).build());

            addRenderableWidget(Button.builder(Component.literal(alphabetical ? "A-Z" : "Smart"), b -> {
                alphabetical = !alphabetical;
                savePreferences();
                rebuildWidgets();
            }).bounds(width - 80, headerY, 48, 22).build());
        }

        if (selected != null) addSettingsWidgets(editValue);
        if (width < 520) addNarrowCategoryTabs();
        profile = new EditBox(font, 12, height - 28, 112, 18, Component.literal("Profile"));
        profile.setHint(Component.literal("profile"));
        profile.setValue(profileValue);
        addRenderableWidget(profile);
        addRenderableWidget(Button.builder(Component.literal("Load"), b -> loadProfile())
                .bounds(130, height - 28, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveProfile())
                .bounds(174, height - 28, 40, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Save Config"), b -> saveConfig())
                .bounds(width - 112, height - 28, 72, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(width - 34, height - 28, 28, 18).build());
    }

    private void addNarrowCategoryTabs() {
        int x = 12;
        int y = 40;
        for (Module.Category category : Module.Category.values()) {
            int tabWidth = Math.max(42, (width - 24) / Module.Category.values().length);
            Module.Category chosen = category;
            String[] labels = {"CMB", "MOV", "REN", "PLY", "WRL", "MSC"};
            addRenderableWidget(Button.builder(Component.literal(labels[category.ordinal()]), b -> {
                activeCategory = chosen;
                rebuildWidgets();
            }).bounds(x, y, tabWidth - 2, 18).build());
            x += tabWidth;
        }
    }

    private void addSettingsWidgets(String editValue) {
        int x = settingsX() + 10;
        int y = settingsY() + 48;
        int w = settingsWidth() - 20;
        addRenderableWidget(Button.builder(Component.literal(selected.enabled() ? "Disable" : "Enable"),
                b -> toggleSelected()).bounds(x, y, 74, 20).build());
        addRenderableWidget(Button.builder(Component.literal(selected.favorite() ? "★ Favorite" : "☆ Favorite"),
                b -> {
                    selected.setFavorite(!selected.favorite());
                    saveConfig();
                    rebuildWidgets();
                }).bounds(x + 80, y, 86, 20).build());
        addRenderableWidget(Button.builder(Component.literal(bindingModule == selected ? "Press a key" : "Bind"),
                b -> {
                    bindingModule = selected;
                    rebuildWidgets();
                }).bounds(x + 172, y, Math.max(42, w - 172), 20).build());
        y += 26;
        addRenderableWidget(Button.builder(Component.literal("Reset settings"), b -> {
            selected.resetSettings();
            saveConfig();
            rebuildWidgets();
        }).bounds(x, y, w, 20).build());
        y += 25;

        int bottom = Math.min(height - 38, settingsY() + settingsHeight() - 8);
        int rowHeight = 23;
        List<Setting<?>> visible = selected.settings().stream().filter(Setting::visible).toList();
        int maxRows = Math.max(0, (bottom - y) / rowHeight);
        int start = Math.min(settingsScroll, Math.max(0, visible.size() - maxRows));
        int shown = 0;
        String lastGroup = null;
        for (int i = start; i < visible.size() && shown < maxRows; i++, shown++) {
            Setting<?> setting = visible.get(i);
            if (setting.group() != null && !setting.group().id().equals(lastGroup)) {
                lastGroup = setting.group().id();
            }
            if (setting instanceof BooleanSetting bs) {
                addRenderableWidget(Button.builder(Component.literal(setting.name() + "  " + (bs.enabled() ? "ON" : "OFF")),
                        b -> { bs.set(!bs.enabled()); saveConfig(); rebuildWidgets(); })
                        .bounds(x, y, w, 20).build());
            } else if (setting instanceof DoubleSetting ds) {
                addRenderableWidget(new DoubleSettingSlider(x, y, w, 20, ds, this::saveConfig));
            } else if (setting instanceof EnumSetting<?> es) {
                addRenderableWidget(Button.builder(Component.literal(setting.name() + "  " + pretty(es.get())),
                        b -> { es.cycle(1); saveConfig(); rebuildWidgets(); })
                        .bounds(x, y, w, 20).build());
            } else if (setting instanceof ColorSetting cs) {
                addRenderableWidget(Button.builder(Component.literal(setting.name() + "  #" + String.format(Locale.ROOT, "%08X", cs.get())),
                        b -> { editingColor = cs; editingString = null; rebuildWidgets(); })
                        .bounds(x, y, w, 20).build());
            } else if (setting instanceof StringSetting ss) {
                addRenderableWidget(Button.builder(Component.literal(setting.name() + "  " + ss.get()),
                        b -> { editingString = ss; editingColor = null; rebuildWidgets(); })
                        .bounds(x, y, w, 20).build());
            } else {
                addRenderableWidget(Button.builder(Component.literal(setting.name() + "  " + String.valueOf(setting.get())),
                        b -> {}).bounds(x, y, w, 20).build());
            }
            y += rowHeight;
        }

        if (editingString != null || editingColor != null) {
            valueEditor = new EditBox(font, x, settingsY() + 28, Math.max(80, w - 56), 18,
                    Component.literal(editingString != null ? editingString.name() : editingColor.name()));
            valueEditor.setMaxLength(128);
            valueEditor.setValue(editValue.isEmpty()
                    ? editingString != null ? editingString.get() : String.format(Locale.ROOT, "%08X", editingColor.get())
                    : editValue);
            addRenderableWidget(valueEditor);
            addRenderableWidget(Button.builder(Component.literal("Apply"), b -> applyEdit())
                    .bounds(x + w - 50, settingsY() + 28, 50, 18).build());
        }
    }

    private List<Module> visibleModules(Module.Category category) {
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<Module> modules = new ArrayList<>(ArsonClient.getInstance().modules().organized(category));
        modules.removeIf(module -> favoritesOnly && !module.favorite()
                || enabledOnly && !module.enabled()
                || !query.isEmpty() && !(module.name() + " " + module.id() + " " + module.description())
                .toLowerCase(Locale.ROOT).contains(query));
        if (alphabetical) modules.sort(Comparator.comparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        return modules;
    }

    private int columns() {
        if (width < 520) return 1;
        return width < 760 ? 3 : 6;
    }

    private int windowCount() { return width < 520 ? 1 : Module.Category.values().length; }
    private int windowWidth() {
        if (width < 520) return Math.max(120, width - 24);
        int cols = columns();
        int gaps = (cols - 1) * 8;
        return Math.max(100, Math.min(190, (width - 24 - gaps) / cols));
    }
    private int windowHeight() {
        if (width < 520) return Math.max(90, height - 112);
        if (columns() == 6) return Math.max(100, height - 104);
        return Math.max(72, (height - 142) / 2);
    }
    private int windowOffsetIndex(int index) { return width < 520 ? activeCategory.ordinal() : index; }
    private int windowX(int index) {
        int cols = columns();
        int column = index % cols;
        int base = width < 520 ? 12 : Math.max(12, (width - (cols * windowWidth() + (cols - 1) * 8)) / 2);
        return base + column * (windowWidth() + 8) + windowOffsetX[windowOffsetIndex(index)];
    }
    private int windowY(int index) {
        int offsetIndex = windowOffsetIndex(index);
        if (width < 520) return 66 + windowOffsetY[offsetIndex];
        if (columns() == 6) return 48 + windowOffsetY[offsetIndex];
        return 48 + (index / columns()) * (windowHeight() + 8) + windowOffsetY[offsetIndex];
    }

    private int settingsWidth() { return Math.min(300, Math.max(230, width / 3)); }
    private int settingsHeight() { return Math.max(120, Math.min(height - 92, 430)); }
    private int settingsX() { return width - settingsWidth() - 14; }
    private int settingsY() { return 52; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        drawBackground(g);
        drawHeader(g);
        if (width < 520) drawNarrowTabs(g);
        for (int i = 0; i < windowCount(); i++) {
            Module.Category category = width < 520 ? activeCategory : Module.Category.values()[i];
            drawCategoryWindow(g, i, category, mouseX, mouseY);
        }
        if (selected != null) drawSettingsPanel(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, delta);
    }

    private void drawBackground(GuiGraphics g) {
        g.fill(0, 0, width, height, 0xB8000000);
        g.fill(0, 0, width, 38, 0xF0101218);
        g.fill(0, height - 36, width, height, 0xE90C0E13);
        g.fill(0, 37, width, 39, accentColor());
    }

    private void drawHeader(GuiGraphics g) {
        g.drawString(font, "ARSON", 14, 9, 0xFFFFFFFF);
        g.drawString(font, "V3", 57, 9, accentColor());
        g.drawString(font, "CLIENT", 14, 22, 0xFF7F8998);
        int enabled = ArsonClient.getInstance().modules().enabledCount();
        if (width >= 520) g.drawString(font, enabled + " ACTIVE", 14, 42, 0xFF9AA5B5);
        if (width >= 760) g.drawString(font, "RIGHT CLICK = SETTINGS", 360, 23, 0xFF727D8C);
    }

    private void drawNarrowTabs(GuiGraphics g) {
        int x = 12;
        int tabWidth = Math.max(42, (width - 24) / Module.Category.values().length);
        for (Module.Category category : Module.Category.values()) {
            boolean active = category == activeCategory;
            int color = active ? 0xFF26364A : 0xD8171B23;
            g.fill(x, 42, x + tabWidth - 2, 61, color);
            String[] labels = {"CMB", "MOV", "REN", "PLY", "WRL", "MSC"};
            String label = labels[category.ordinal()];
            g.drawString(font, label, x + (tabWidth - font.width(label)) / 2, 48,
                    active ? accentColor() : 0xFFADB6C3);
            x += tabWidth;
        }
    }

    private void drawCategoryWindow(GuiGraphics g, int index, Module.Category category, int mouseX, int mouseY) {
        int x = windowX(index), y = windowY(index), w = windowWidth(), h = windowHeight();
        int headerHeight = 23;
        g.fill(x, y, x + w, y + h, panelColor());
        g.fill(x, y, x + w, y + headerHeight, 0xFF1B202A);
        g.fill(x, y, x + 3, y + headerHeight, CATEGORY_COLORS[category.ordinal()]);
        g.renderOutline(x, y, w, h, 0xFF303946);
        String title = category.displayName().toUpperCase(Locale.ROOT);
        g.drawString(font, title, x + 9, y + 8, 0xFFE5EAF1);
        int enabled = (int) ArsonClient.getInstance().modules().organized(category).stream().filter(Module::enabled).count();
        String count = enabled + "/" + ArsonClient.getInstance().modules().categoryCount(category);
        g.drawString(font, count, x + w - font.width(count) - 16, y + 8, 0xFF8E9AAA);
        g.drawString(font, collapsed[category.ordinal()] ? "+" : "−", x + w - 11, y + 8, 0xFFADB8C7);
        if (collapsed[category.ordinal()]) return;

        List<Module> modules = visibleModules(category);
        int rowY = y + headerHeight + 3;
        int rowHeight = 18;
        int maxRows = Math.max(0, (h - headerHeight - 7) / rowHeight);
        int start = Math.min(windowScroll[category.ordinal()], Math.max(0, modules.size() - maxRows));
        int shown = 0;
        for (int i = start; i < modules.size() && shown < maxRows; i++, shown++) {
            Module module = modules.get(i);
            int ry = rowY + shown * rowHeight;
            boolean hovered = mouseX >= x + 3 && mouseX < x + w - 3 && mouseY >= ry && mouseY < ry + rowHeight;
            if (module == selected) g.fill(x + 3, ry, x + w - 3, ry + rowHeight, 0xFF27384D);
            else if (hovered) g.fill(x + 3, ry, x + w - 3, ry + rowHeight, 0xFF202833);
            g.drawString(font, module.enabled() ? "●" : "○", x + 8, ry + 5,
                    module.enabled() ? accentColor() : 0xFF657181);
            String name = module.name();
            while (name.length() > 3 && font.width(name) > w - 43) name = name.substring(0, name.length() - 1);
            if (name.length() != module.name().length()) name += "…";
            g.drawString(font, name, x + 22, ry + 5, module.enabled() ? 0xFFE7EDF5 : 0xFFADB6C2);
            if (module.favorite()) g.drawString(font, "★", x + w - 15, ry + 5, 0xFFE6C56F);
        }
        if (modules.isEmpty()) g.drawString(font, "No modules", x + 9, rowY + 4, 0xFF707B89);
        else if (modules.size() > maxRows) {
            String page = (start + 1) + "-" + Math.min(modules.size(), start + maxRows) + "/" + modules.size();
            g.drawString(font, page, x + w - font.width(page) - 6, y + h - 13, 0xFF697585);
        }
    }

    private void drawSettingsPanel(GuiGraphics g, int mouseX, int mouseY) {
        int x = settingsX(), y = settingsY(), w = settingsWidth(), h = settingsHeight();
        g.fill(x, y, x + w, y + h, 0xF00E1219);
        g.fill(x, y, x + w, y + 30, 0xFF202834);
        g.fill(x, y, x + 3, y + 30, accentColor());
        g.renderOutline(x, y, w, h, 0xFF3A4655);
        g.drawString(font, selected.name(), x + 10, y + 10, 0xFFFFFFFF);
        g.drawString(font, "×", x + w - 14, y + 10, 0xFF9AA5B5);
        String description = selected.description();
        if (!description.isBlank()) {
            String clipped = description;
            while (clipped.length() > 12 && font.width(clipped) > w - 20) clipped = clipped.substring(0, clipped.length() - 1);
            if (clipped.length() != description.length()) clipped += "…";
            g.drawString(font, clipped, x + 10, y + 35, 0xFF8995A4);
        }
        g.drawString(font, selected.enabled() ? "ENABLED" : "DISABLED", x + 10, y + h - 14,
                selected.enabled() ? accentColor() : 0xFF77818F);
        g.drawString(font, "SCROLL FOR SETTINGS", x + w - 126, y + h - 14, 0xFF697585);
    }

    private int accentColor() {
        return switch (theme) {
            case MIDNIGHT -> 0xFF4C8DFF;
            case GRAPHITE -> 0xFF9BAEC4;
            case CONTRAST -> 0xFFFFFFFF;
        };
    }
    private int panelColor() {
        return switch (theme) {
            case MIDNIGHT -> 0xE510141C;
            case GRAPHITE -> 0xE5171B22;
            case CONTRAST -> 0xEE08090B;
        };
    }

    private void toggleSelected() {
        if (selected == null) return;
        selected.toggle();
        NotificationCenter.push(selected.name(), selected.enabled() ? "Enabled" : "Disabled");
        saveConfig();
        rebuildWidgets();
    }

    private void applyEdit() {
        if (valueEditor == null) return;
        if (editingString != null) editingString.set(valueEditor.getValue());
        else if (editingColor != null) {
            try {
                String raw = valueEditor.getValue().trim().replace("#", "");
                if (raw.length() == 6) raw = "FF" + raw;
                if (raw.length() != 8) throw new NumberFormatException();
                editingColor.set((int) Long.parseLong(raw, 16));
            } catch (NumberFormatException ignored) {
                NotificationCenter.push("Invalid color", "Use RRGGBB or AARRGGBB");
                return;
            }
        }
        editingString = null;
        editingColor = null;
        saveConfig();
        rebuildWidgets();
    }

    private void saveConfig() {
        if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules());
    }

    private void saveProfile() {
        if (minecraft == null || profile == null || profile.getValue().isBlank()) return;
        if (ConfigManager.saveProfile(minecraft, ArsonClient.getInstance().modules(), profile.getValue())) {
            NotificationCenter.push("Profile", "Saved " + profile.getValue());
        } else NotificationCenter.push("Profile", "Invalid profile name");
    }

    private void loadProfile() {
        if (minecraft == null || profile == null || profile.getValue().isBlank()) return;
        if (ConfigManager.loadProfile(minecraft, ArsonClient.getInstance().modules(), profile.getValue())) {
            NotificationCenter.push("Profile", "Loaded " + profile.getValue());
            rebuildWidgets();
        } else NotificationCenter.push("Profile", "Profile not found or invalid");
    }

    private static String pretty(Object value) {
        if (value == null) return "None";
        String raw = value.toString().toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder();
        for (String part : raw.split("_")) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            searchFocused = search != null && search.isFocused();
            return true;
        }
        searchFocused = false;
        if (search != null) search.setFocused(false);
        if (event.button() == 0 && selected != null && inSettingsClose(event.x(), event.y())) {
            selected = null;
            editingString = null;
            editingColor = null;
            rebuildWidgets();
            return true;
        }
        if (selected != null && event.x() >= settingsX() && event.x() <= settingsX() + settingsWidth()
                && event.y() >= settingsY() && event.y() <= settingsY() + settingsHeight()) return true;
        if (width < 520 && event.button() == 0 && event.y() >= 42 && event.y() <= 61) {
            int tabWidth = Math.max(42, (width - 24) / Module.Category.values().length);
            int index = Math.max(0, Math.min(Module.Category.values().length - 1, (int) ((event.x() - 12) / tabWidth)));
            activeCategory = Module.Category.values()[index];
            rebuildWidgets();
            return true;
        }
        for (int i = 0; i < windowCount(); i++) {
            Module.Category category = width < 520 ? activeCategory : Module.Category.values()[i];
            int x = windowX(i), y = windowY(i), w = windowWidth(), h = windowHeight();
            if (event.x() < x || event.x() >= x + w || event.y() < y || event.y() >= y + h) continue;
            if (event.y() < y + 23) {
                if (event.button() == 0) {
                    if (event.x() >= x + w - 20) collapsed[category.ordinal()] = !collapsed[category.ordinal()];
                    else {
                        draggingWindow = category.ordinal();
                        dragOffsetX = event.x() - x;
                        dragOffsetY = event.y() - y;
                    }
                    return true;
                }
            } else if (!collapsed[category.ordinal()]) {
                List<Module> modules = visibleModules(category);
                int rowHeight = 18;
                int row = (int) ((event.y() - (y + 26)) / rowHeight);
                int maxRows = Math.max(0, (h - 30) / rowHeight);
                int index = windowScroll[category.ordinal()] + row;
                if (row >= 0 && row < maxRows && index >= 0 && index < modules.size()) {
                    Module module = modules.get(index);
                    if (event.button() == 0) {
                        module.toggle();
                        NotificationCenter.push(module.name(), module.enabled() ? "Enabled" : "Disabled");
                        saveConfig();
                        rebuildWidgets();
                    } else if (event.button() == 1) {
                        selected = module;
                        settingsScroll = 0;
                        editingString = null;
                        editingColor = null;
                        rebuildWidgets();
                    } else if (event.button() == 2) {
                        module.setFavorite(!module.favorite());
                        saveConfig();
                        rebuildWidgets();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean inSettingsClose(double mouseX, double mouseY) {
        int x = settingsX(), y = settingsY(), w = settingsWidth();
        return mouseX >= x + w - 26 && mouseX <= x + w && mouseY >= y && mouseY <= y + 30;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingWindow >= 0 && event.button() == 0) {
            int index = draggingWindow;
            int baseX = width < 520 ? 12 : Math.max(12, (width - (columns() * windowWidth() + (columns() - 1) * 8)) / 2)
                    + (index % columns()) * (windowWidth() + 8);
            int baseY = width < 520 ? 66 : columns() == 6 ? 48 : 48 + (index / columns()) * (windowHeight() + 8);
            windowOffsetX[index] = Math.max(-baseX, Math.min(width - baseX - windowWidth(), (int) Math.round(event.x() - dragOffsetX - baseX)));
            windowOffsetY[index] = Math.max(-baseY, Math.min(height - baseY - 36, (int) Math.round(event.y() - dragOffsetY - baseY)));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && draggingWindow >= 0) {
            draggingWindow = -1;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (selected != null && mouseX >= settingsX() && mouseX <= settingsX() + settingsWidth()
                && mouseY >= settingsY() && mouseY <= settingsY() + settingsHeight()) {
            settingsScroll = Math.max(0, settingsScroll + (vertical > 0 ? -1 : 1));
            rebuildWidgets();
            return true;
        }
        for (int i = 0; i < windowCount(); i++) {
            Module.Category category = width < 520 ? activeCategory : Module.Category.values()[i];
            int x = windowX(i), y = windowY(i);
            if (mouseX >= x && mouseX <= x + windowWidth() && mouseY >= y && mouseY <= y + windowHeight()) {
                windowScroll[category.ordinal()] = Math.max(0, windowScroll[category.ordinal()] + (vertical > 0 ? -1 : 1));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (bindingModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                rebuildWidgets();
                return true;
            }
            bindingModule.setKeyCode(key);
            NotificationCenter.push(bindingModule.name(), "Keybind updated");
            bindingModule = null;
            saveConfig();
            rebuildWidgets();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (event.hasControlDown() && key == GLFW.GLFW_KEY_K && search != null) {
            searchFocused = true;
            search.setFocused(true);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean handled = super.charTyped(event);
        if (search != null && search.isFocused()) {
            searchFocused = true;
            for (int i = 0; i < windowCount(); i++) {
                Module.Category category = width < 520 ? activeCategory : Module.Category.values()[i];
                windowScroll[category.ordinal()] = 0;
            }
            rebuildWidgets();
        }
        return handled;
    }

    @Override
    public void onClose() {
        bindingModule = null;
        saveConfig();
        minecraft.setScreen(parent);
    }
}
