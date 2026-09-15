package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.HudModule;
import io.arson.client.module.PlayerInfoModule;
import io.arson.client.module.WorldInfoModule;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Visual editor for independently positioning and styling HUD elements. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS = {"watermark", "coordinates", "fps", "player-info", "world-info"};
    private static final String[] LABELS = {"Watermark", "Coordinates", "FPS", "Player Info", "World Info"};
    private static final int[] COLOR_PRESETS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF55AAFF, 0xFFAA55FF, 0xFFFF55FF, 0xFFAAAAAA};

    private final Screen parent;
    private HudModule hud;
    private EditBox watermarkBox;
    private String selected = "watermark";
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        if (hud == null) return;

        watermarkBox = new EditBox(font, 10, 82, 180, 20, Component.literal("Watermark"));
        watermarkBox.setValue(hud.watermarkText());
        watermarkBox.setMaxLength(32);
        watermarkBox.setHint(Component.literal("Watermark text..."));
        addRenderableWidget(watermarkBox);

        for (int i = 0; i < ELEMENTS.length; i++) {
            final int index = i;
            addRenderableWidget(Button.builder(Component.literal(elementButtonText(index)), b -> {
                saveWatermarkText();
                selected = ELEMENTS[index];
                rebuild();
            }).bounds(10 + i * 86, 50, 82, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Color"), b -> {
            cycleColor(selected);
            save();
            b.setMessage(Component.literal("Color: " + colorHex(hud.elementColor(selected))));
        }).bounds(200, 82, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scale -"), b -> {
            adjustScale(selected, -0.05);
            save();
        }).bounds(278, 82, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scale +"), b -> {
            adjustScale(selected, 0.05);
            save();
        }).bounds(354, 82, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Background: " + (hud.elementBackground(selected) ? "ON" : "OFF")), b -> {
            toggleBackground(selected);
            b.setMessage(Component.literal("Background: " + (hud.elementBackground(selected) ? "ON" : "OFF")));
            save();
        }).bounds(430, 82, 120, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Reset Selected"), b -> {
            hud.resetElement(selected);
            save();
        }).bounds(10, this.height - 30, 105, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset All"), b -> {
            for (String element : ELEMENTS) hud.resetElement(element);
            save();
        }).bounds(120, this.height - 30, 85, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Align: " + hud.elementAlignment(selected)), b -> {
            hud.cycleAlignment(selected);
            b.setMessage(Component.literal("Align: " + hud.elementAlignment(selected)));
            save();
        }).bounds(215, this.height - 30, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Visible: " + (hud.elementVisible(selected) ? "ON" : "OFF")), b -> {
            hud.setElementVisible(selected, !hud.elementVisible(selected));
            b.setMessage(Component.literal("Visible: " + (hud.elementVisible(selected) ? "ON" : "OFF")));
            save();
        }).bounds(315, this.height - 30, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Grid: " + (hud.gridSnap() ? "ON" : "OFF")), b -> {
            hud.setGridSnap(!hud.gridSnap());
            b.setMessage(Component.literal("Grid: " + (hud.gridSnap() ? "ON" : "OFF")));
            save();
        }).bounds(420, this.height - 30, 85, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width - 80, this.height - 30, 70, 20).build());
    }

    private String elementButtonText(int index) {
        String label = LABELS[index];
        return ELEMENTS[index].equals(selected) ? "> " + label : label;
    }

    private void cycleColor(String element) {
        ColorSetting setting = colorSetting(element);
        if (setting == null) return;
        int current = setting.get();
        int next = COLOR_PRESETS[0];
        for (int i = 0; i < COLOR_PRESETS.length; i++) {
            if (COLOR_PRESETS[i] == current) {
                next = COLOR_PRESETS[(i + 1) % COLOR_PRESETS.length];
                break;
            }
        }
        setting.set(next);
    }

    private ColorSetting colorSetting(String element) {
        for (var setting : hud.settings()) {
            if (!(setting instanceof ColorSetting color)) continue;
            String id = color.id();
            String expected = element + "-color";
            if (id.equals(expected)) return color;
        }
        return null;
    }

    private void adjustScale(String element, double delta) {
        for (var setting : hud.settings()) {
            if (setting instanceof DoubleSetting value && value.id().equals(element + "-scale")) {
                value.set(Math.max(0.5, Math.min(2.0, value.get() + delta)));
                return;
            }
        }
    }

    private void toggleBackground(String element) {
        for (var setting : hud.settings()) {
            if (setting instanceof BooleanSetting value && value.id().equals(element + "-background")) {
                value.set(!value.enabled());
                return;
            }
        }
    }

    private String colorHex(int color) {
        return String.format(java.util.Locale.ROOT, "#%08X", color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hud != null && hitHud(mouseX, mouseY)) {
            dragging = true;
            double[] position = position(selected);
            dragOffsetX = mouseX - position[0] * hud.scale();
            dragOffsetY = mouseY - position[1] * hud.scale();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0 && hud != null) {
            hud.setEditorPosition(selected, (mouseX - dragOffsetX) / hud.scale(), (mouseY - dragOffsetY) / hud.scale());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hitHud(double mouseX, double mouseY) {
        if (hud == null || !hud.elementVisible(selected)) return false;
        double[] position = position(selected);
        int width = Math.max(55, selected.equals("watermark") ? font.width(hud.watermarkText()) : 55);
        int height = selected.equals("player-info") ? playerInfoHeight() : selected.equals("world-info") ? worldInfoHeight() : hud.lineSpacing();
        double elementScale = hud.elementScale(selected) * hud.scale();
        double left = alignedLeft(position[0], width, hud.elementAlignment(selected)) * hud.scale() - hud.padding() * elementScale;
        double top = position[1] * hud.scale() - hud.padding() * elementScale;
        double right = left + width * elementScale + hud.padding() * 2 * elementScale;
        double bottom = top + height * elementScale + hud.padding() * 2 * elementScale;
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    private double[] position(String element) {
        return switch (element) {
            case "coordinates" -> new double[]{hud.coordinatesX(), hud.coordinatesY()};
            case "fps" -> new double[]{hud.fpsX(), hud.fpsY()};
            case "player-info" -> new double[]{hud.playerInfoX(), hud.playerInfoY()};
            case "world-info" -> new double[]{hud.worldInfoX(), hud.worldInfoY()};
            default -> new double[]{hud.x(), hud.y()};
        };
    }

    private double alignedLeft(double x, double width, String alignment) {
        return switch (alignment == null ? "left" : alignment.toLowerCase(java.util.Locale.ROOT)) {
            case "center" -> x - width / 2.0;
            case "right" -> x - width;
            default -> x;
        };
    }

    private int playerInfoHeight() {
        PlayerInfoModule module = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        int rows = 0;
        if (module != null && module.enabled()) {
            if (module.showHealth()) rows++; if (module.showHunger()) rows++; if (module.showArmor()) rows++; if (module.showHeldItem()) rows++;
        }
        return Math.max(hud.lineSpacing(), rows * hud.lineSpacing());
    }

    private int worldInfoHeight() {
        WorldInfoModule module = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        int rows = 0;
        if (module != null && module.enabled()) {
            if (module.showTime()) rows++; if (module.showDimension()) rows++; if (module.showWeather()) rows++;
        }
        return Math.max(hud.lineSpacing(), rows * hud.lineSpacing());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB0101014);
        graphics.text(font, "HUD Editor — select an element, then drag its box", 10, 16, 0xFFFFFFFF, true);
        graphics.text(font, "Selected: " + selected + "  X=" + (int) position(selected)[0] + " Y=" + (int) position(selected)[1]
                + "  Scale=" + String.format(java.util.Locale.ROOT, "%.2f", hud.elementScale(selected)), 10, 32, 0xFFD0D0D0, false);
        graphics.text(font, "Watermark text", 10, 72, 0xFFFFFFFF, false);

        if (hud != null) {
            for (int i = 0; i < ELEMENTS.length; i++) {
                String element = ELEMENTS[i];
                if (!hud.elementVisible(element)) continue;
                drawPreviewElement(graphics, element, LABELS[i]);
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawPreviewElement(GuiGraphicsExtractor graphics, String element, String label) {
        double[] pos = position(element);
        double totalScale = hud.scale() * hud.elementScale(element);
        int width = element.equals("watermark") ? Math.max(70, font.width(hud.watermarkText())) : 70;
        int height = element.equals("player-info") ? playerInfoHeight() : element.equals("world-info") ? worldInfoHeight() : hud.lineSpacing();
        int left = (int) Math.round(alignedLeft(pos[0], width, hud.elementAlignment(element)) * hud.scale() - hud.padding() * totalScale);
        int top = (int) Math.round(pos[1] * hud.scale() - hud.padding() * totalScale);
        int right = left + (int) Math.round(width * totalScale + hud.padding() * 2 * totalScale);
        int bottom = top + (int) Math.round(height * totalScale + hud.padding() * 2 * totalScale);
        int outline = element.equals(selected) ? 0xFFFFFFFF : 0xFF6A6A6A;
        int fill = hud.elementBackground(element) ? hud.elementBackgroundColor() : 0x30303038;
        graphics.fill(left, top, right, bottom, fill);
        graphics.outline(left, top, right - left, bottom - top, outline);
        String preview = element.equals("watermark") ? hud.watermarkText() : label;
        graphics.text(font, preview, left + hud.padding(), top + hud.padding(), hud.elementColor(element), hud.showShadow());
    }

    private void saveWatermarkText() {
        if (hud != null && watermarkBox != null) {
            for (var setting : hud.settings()) {
                if (setting instanceof StringSetting text && text.id().equals("watermark-text")) {
                    text.set(watermarkBox.getValue());
                    break;
                }
            }
        }
    }

    private void save() {
        saveWatermarkText();
        if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules());
    }

    @Override
    public void onClose() {
        save();
        if (minecraft != null) minecraft.gui.setScreen(parent);
    }
}
