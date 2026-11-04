package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.HudLayoutModule;
import io.arson.client.module.HudModule;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** HUD editor with a Meteor-inspired canvas, element navigator and properties panel. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS = {"watermark", "coordinates", "fps", "player-info", "world-info", "array-list"};
    private final Screen parent;
    private HudModule hud;
    private HudLayoutModule layout;
    private ArrayListModule arrayList;
    private final HudSelectionModel selection = new HudSelectionModel("watermark");
    private final Map<String, Snapshot> clipboard = new LinkedHashMap<>();
    private boolean dragging;
    private String dragAnchor;
    private double offsetX, offsetY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        layout = (HudLayoutModule) ArsonClient.getInstance().modules().get("hud-layout");
        arrayList = (ArrayListModule) ArsonClient.getInstance().modules().get("array-list");
        if (hud == null) return;
        repairLayout();

        int leftX = width < 520 ? 8 : 12, leftY = 58, leftW = width < 520 ? 96 : 154;
        for (int i = 0; i < ELEMENTS.length; i++) {
            String element = ELEMENTS[i];
            int rowY = leftY + i * 25;
            addDrawableChild(ButtonWidget.builder(Text.literal((selection.contains(element) ? "◆ " : "◇ ")
                            + title(element) + (visible(element) ? "  ON" : "  OFF")),
                    b -> { selection.select(element, false); rebuild(); })
                    .dimensions(leftX, rowY, leftW, 21).build());
        }

        int rightX = rightX(), rightW = rightWidth(), y = 58;
        addDrawableChild(ButtonWidget.builder(Text.literal("Visibility  " + (allVisible() ? "ON" : "OFF")),
                b -> { toggleVisible(); repairLayout(); save(); rebuild(); })
                .dimensions(rightX, y, rightW, 21).build());
        y += 25;
        int half = (rightW - 4) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale −"), b -> {
            adjustScale(-0.05); repairLayout(); save(); rebuild();
        }).dimensions(rightX, y, half, 21).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"), b -> {
            adjustScale(0.05); repairLayout(); save(); rebuild();
        }).dimensions(rightX + half + 4, y, half, 21).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Alignment"), b -> {
            cycleAlignment(); save(); rebuild();
        }).dimensions(rightX, y, half, 21).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Anchor"), b -> {
            anchorSelected(); repairLayout(); save(); rebuild();
        }).dimensions(rightX + half + 4, y, half, 21).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Background"), b -> {
            toggleBackground(); save(); rebuild();
        }).dimensions(rightX, y, rightW, 21).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Copy"), b -> {
            copySelected(); rebuild();
        }).dimensions(rightX, y, half, 21).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Paste"), b -> {
            pasteSelected(); repairLayout(); save(); rebuild();
        }).dimensions(rightX + half + 4, y, half, 21).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Duplicate"), b -> {
            duplicateSelected(); repairLayout(); save(); rebuild();
        }).dimensions(rightX, y, half, 21).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            resetSelected(); repairLayout(); save(); rebuild();
        }).dimensions(rightX + half + 4, y, half, 21).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Select All"), b -> {
            selection.replace(List.of(ELEMENTS)); rebuild();
        }).dimensions(rightX, y, half, 21).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> {
            selection.clear(); rebuild();
        }).dimensions(rightX + half + 4, y, half, 21).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width - 78, height - 30, 66, 21).build());
    }

    private int rightWidth() { return width < 520 ? 120 : Math.max(142, Math.min(180, width / 4)); }
    private int rightX() { return width - rightWidth() - (width < 520 ? 8 : 12); }
    private int canvasLeft() { return width < 520 ? 112 : 178; }
    private int canvasRight() { return rightX() - 12; }
    private int canvasTop() { return 42; }
    private int canvasBottom() { return height - 12; }

    private void rebuild() {
        clearChildren();
        init();
    }

    private void repairLayout() {
        if (layout == null) return;
        if (arrayList != null) layout.setArrayListPreferredPosition(arrayList.x(), arrayList.y());
        if (layout.repairNoOverlap(width, height, hud.scale(),
                Math.max(90, Math.min(width / 2, 220)),
                Math.max(24, Math.min(height / 2, 320)), arrayList, hud)) save();
    }

    private boolean visible(String element) {
        return "array-list".equals(element) ? arrayList != null && arrayList.enabled() : hud.elementVisible(element);
    }

    private boolean allVisible() {
        if (selection.size() == 0) return false;
        for (String element : selection.elements()) if (!visible(element)) return false;
        return true;
    }

    private void toggleVisible() {
        boolean target = !allVisible();
        for (String element : selection.elements()) {
            if ("array-list".equals(element)) {
                if (arrayList != null && arrayList.enabled() != target) arrayList.toggle();
            } else hud.setElementVisible(element, target);
        }
    }

    private void adjustScale(double delta) {
        for (String element : selection.elements()) {
            if ("array-list".equals(element)) {
                if (arrayList != null) arrayList.adjustScale(delta);
            } else hud.adjustElementScale(element, delta);
        }
    }

    private void toggleBackground() {
        for (String element : selection.elements()) {
            if ("array-list".equals(element)) {
                if (arrayList != null) arrayList.toggleBackground();
                continue;
            }
            for (Setting<?> setting : hud.settings()) {
                if (setting instanceof BooleanSetting value && value.id().equals(element + "-background")) {
                    value.set(!value.enabled());
                }
            }
        }
    }

    private void cycleAlignment() {
        for (String element : selection.elements()) if (!"array-list".equals(element)) hud.cycleAlignment(element);
    }

    private void resetSelected() {
        for (String element : selection.elements()) {
            if ("array-list".equals(element)) {
                if (arrayList != null) arrayList.resetSettings();
            } else {
                hud.resetElement(element);
                if (layout != null) layout.resetElement(element);
            }
        }
    }

    private void save() {
        if (client != null) ConfigManager.save(client, ArsonClient.getInstance().modules());
    }

    /** Returns screen-space coordinates, matching the global HUD scale used by HudRenderer. */
    private double[] position(String element) {
        if ("array-list".equals(element)) return new double[]{arrayList.x(), arrayList.y()};
        double globalScale = Math.max(0.001, hud.scale());
        double logicalWidth = width / globalScale;
        double logicalHeight = height / globalScale;
        double elementScale = hud.elementScale(element);
        if (layout != null) {
            double[] resolved = layout.resolve(element, logicalWidth, logicalHeight,
                    190 * elementScale, 35 * elementScale);
            return new double[]{resolved[0] * globalScale, resolved[1] * globalScale};
        }
        double[] fallback = switch (element) {
            case "coordinates" -> new double[]{hud.coordinatesX(), hud.coordinatesY()};
            case "fps" -> new double[]{hud.fpsX(), hud.fpsY()};
            case "player-info" -> new double[]{hud.playerInfoX(), hud.playerInfoY()};
            case "world-info" -> new double[]{hud.worldInfoX(), hud.worldInfoY()};
            default -> new double[]{hud.x(), hud.y()};
        };
        return new double[]{fallback[0] * globalScale, fallback[1] * globalScale};
    }

    private double elementScale(String element) {
        return "array-list".equals(element) ? arrayList.scale() : hud.scale() * hud.elementScale(element);
    }

    private void setPosition(String element, double x, double y) {
        if ("array-list".equals(element)) {
            arrayList.setEditorPosition(x, y);
            return;
        }
        double globalScale = Math.max(0.001, hud.scale());
        double logicalWidth = width / globalScale;
        double logicalHeight = height / globalScale;
        double elementScale = hud.elementScale(element);
        if (layout != null) {
            layout.setPositionPreservingAnchor(element, x / globalScale, y / globalScale,
                    logicalWidth, logicalHeight, 190 * elementScale, 35 * elementScale);
        } else hud.setEditorPosition(element, x / globalScale, y / globalScale);
    }

    private boolean hit(String element, double x, double y) {
        if (!visible(element)) return false;
        double[] p = position(element);
        double scale = elementScale(element);
        double boxWidth = "array-list".equals(element) ? 220 * scale : 190 * scale;
        double boxHeight = "array-list".equals(element) ? 300 * scale : 35 * scale;
        return x >= p[0] - 8 && x <= p[0] + boxWidth && y >= p[1] - 8 && y <= p[1] + boxHeight;
    }

    private Snapshot snapshot(String element) {
        if ("array-list".equals(element) || hud == null) return null;
        double[] p = position(element);
        HudLayoutModule.Anchor anchor = layout == null ? HudLayoutModule.Anchor.TOP_LEFT : layout.anchor(element);
        return new Snapshot(element, visible(element), p[0], p[1], hud.elementScale(element),
                hud.elementBackground(element), hud.elementAlignment(element), hud.elementColor(element), anchor);
    }

    private void copySelected() {
        clipboard.clear();
        for (String element : selection.elements()) {
            Snapshot value = snapshot(element);
            if (value != null) clipboard.put(element, value);
        }
    }

    private void pasteSelected() {
        if (clipboard.isEmpty() || hud == null) return;
        List<Snapshot> values = new ArrayList<>(clipboard.values());
        for (String target : selection.elements()) {
            if ("array-list".equals(target)) continue;
            Snapshot source = clipboard.get(target);
            if (source == null) source = values.get(0);
            if (source != null && HudClipboardCompatible.isCompatible(source.source, target)) applySnapshot(target, source);
        }
    }

    private void duplicateSelected() {
        if (selection.size() != 1) return;
        String source = selection.elements().iterator().next();
        Snapshot value = snapshot(source);
        if (value == null) return;
        for (String target : ELEMENTS) {
            if (target.equals(source) || selection.contains(target) || "array-list".equals(target)) continue;
            if (HudClipboardCompatible.isCompatible(source, target)) {
                applySnapshot(target, new Snapshot(source, value.visible, value.x + 14, value.y + 14,
                        value.scale, value.background, value.alignment, value.color, value.anchor));
                selection.select(target, false);
                break;
            }
        }
    }

    private void applySnapshot(String element, Snapshot value) {
        if (value == null || "array-list".equals(element)) return;
        hud.setElementVisible(element, value.visible);
        if (layout != null) {
            layout.setAnchor(element, value.anchor);
            double globalScale = Math.max(0.001, hud.scale());
            layout.setPositionPreservingAnchor(element, value.x / globalScale, value.y / globalScale,
                    width / globalScale, height / globalScale, 190 * value.scale, 35 * value.scale);
        } else hud.setEditorPosition(element, value.x / hud.scale(), value.y / hud.scale());
        for (Setting<?> setting : hud.settings()) {
            if (setting instanceof DoubleSetting d && d.id().equals(element + "-scale")) d.set(value.scale);
            else if (setting instanceof BooleanSetting b && b.id().equals(element + "-background")) b.set(value.background);
            else if (setting instanceof ColorSetting c && c.id().equals(element + "-color")) c.set(value.color);
            else if (setting instanceof io.arson.client.settings.StringSetting str && str.id().equals(element + "-align")) str.set(value.alignment);
        }
    }

    private void anchorSelected() {
        if (layout == null) return;
        HudLayoutModule.Anchor[] anchors = HudLayoutModule.Anchor.values();
        for (String element : selection.elements()) {
            if ("array-list".equals(element)) continue;
            Snapshot current = snapshot(element);
            HudLayoutModule.Anchor next = anchors[(layout.anchor(element).ordinal() + 1) % anchors.length];
            double globalScale = Math.max(0.001, hud.scale());
            layout.setAnchorPreservingPosition(element, next, current.x / globalScale, current.y / globalScale,
                    width / globalScale, height / globalScale,
                    190 * hud.elementScale(element), 35 * hud.elementScale(element));
        }
    }

    @Override
    public boolean mouseClicked(Click event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() == 0) {
            for (String element : ELEMENTS) {
                if (!hit(element, event.x(), event.y())) continue;
                selection.select(element, event.hasShift());
                dragAnchor = element;
                double[] p = position(element);
                offsetX = event.x() - p[0];
                offsetY = event.y() - p[1];
                dragging = true;
                rebuild();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click event, double dragX, double dragY) {
        if (dragging && event.button() == 0 && dragAnchor != null) {
            double x = event.x() - offsetX;
            double y = event.y() - offsetY;
            double[] anchorPosition = position(dragAnchor);
            double dx = x - anchorPosition[0];
            double dy = y - anchorPosition[1];
            for (String element : selection.elements()) {
                double[] p = position(element);
                setPosition(element, p[0] + dx, p[1] + dy);
            }
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(Click event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            dragAnchor = null;
            repairLayout();
            save();
            rebuild();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyInput event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_C && event.hasCtrl()) {
            copySelected();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_V && event.hasCtrl()) {
            pasteSelected(); repairLayout(); save(); rebuild();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_D && event.hasCtrl()) {
            duplicateSelected(); repairLayout(); save(); rebuild();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA6000000);
        graphics.fill(0, 0, width, 30, 0xF010131A);
        graphics.drawTextWithShadow(textRenderer, "HUD EDITOR", 14, 10, 0xFFFFFFFF);
        String help = width < 520 ? "Drag • Shift group • Ctrl+C/V/D" : "Drag elements  •  Shift-click to group  •  Ctrl+C / Ctrl+V / Ctrl+D";
        graphics.drawTextWithShadow(textRenderer, help, width < 520 ? 116 : 178, 10, 0xFF8995A4);

        int leftX = width < 520 ? 8 : 12;
        int leftW = width < 520 ? 96 : 154;
        graphics.fill(leftX, 34, leftX + leftW, height - 12, 0xE510141C);
        graphics.drawStrokedRectangle(leftX, 34, leftW, height - 46, 0xFF35404E);
        graphics.drawTextWithShadow(textRenderer, "ELEMENTS", leftX + 10, 43, 0xFFB9C4D2);

        int rightX = rightX(), rightW = rightWidth();
        graphics.fill(rightX, 34, rightX + rightW, height - 12, 0xE510141C);
        graphics.drawStrokedRectangle(rightX, 34, rightW, height - 46, 0xFF35404E);
        graphics.drawTextWithShadow(textRenderer, "PROPERTIES", rightX + 10, 43, 0xFFB9C4D2);
        graphics.drawTextWithShadow(textRenderer, selection.size() + " selected", rightX + 10, 78, 0xFF8793A2);

        int canvasX = canvasLeft(), canvasR = canvasRight();
        graphics.fill(canvasX, canvasTop(), canvasR, canvasBottom(), 0x1EFFFFFF);
        for (int x = canvasX; x < canvasR; x += 32) graphics.fill(x, canvasTop(), x + 1, canvasBottom(), 0x1FFFFFFF);
        for (int y = canvasTop(); y < canvasBottom(); y += 32) graphics.fill(canvasX, y, canvasR, y + 1, 0x1FFFFFFF);
        graphics.drawStrokedRectangle(canvasX, canvasTop(), Math.max(1, canvasR - canvasX),
                Math.max(1, canvasBottom() - canvasTop()), 0x553B4654);

        for (String element : ELEMENTS) {
            if (!visible(element) && !selection.contains(element)) continue;
            double[] p = position(element);
            double scale = elementScale(element);
            int boxW = (int) Math.ceil(("array-list".equals(element) ? 220 : 190) * scale);
            int boxH = (int) Math.ceil(("array-list".equals(element) ? 300 : 35) * scale);
            int x = (int) Math.round(p[0]), y = (int) Math.round(p[1]);
            boolean selected = selection.contains(element);
            int color = selected ? 0xFF55A7FF : 0x887B8796;
            if (selected) graphics.fill(x - 3, y - 3, x + boxW + 3, y + boxH + 3, 0x2255A7FF);
            graphics.drawStrokedRectangle(x - 3, y - 3, Math.max(8, boxW + 6), Math.max(8, boxH + 6), color);
            graphics.drawTextWithShadow(textRenderer, title(element), x + 3, y + 3, selected ? 0xFFFFFFFF : 0xFFB7C1CE);
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    private static String title(String element) {
        return switch (element) {
            case "player-info" -> "Player Info";
            case "world-info" -> "World Info";
            case "array-list" -> "Array List";
            default -> Character.toUpperCase(element.charAt(0)) + element.substring(1);
        };
    }

    @Override
    public void close() {
        save();
        client.setScreen(parent);
    }

    private record Snapshot(String source, boolean visible, double x, double y, double scale,
                            boolean background, String alignment, int color, HudLayoutModule.Anchor anchor) {}

    static final class HudClipboardCompatible {
        private HudClipboardCompatible() {}
        static boolean isCompatible(String source, String target) {
            return source != null && target != null && !source.equals("array-list") && !target.equals("array-list")
                    && STANDARD.contains(source) && STANDARD.contains(target);
        }
        private static final Set<String> STANDARD = Set.of("watermark", "coordinates", "fps", "player-info", "world-info");
    }
}
