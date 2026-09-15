package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.HudModule;
import io.arson.client.module.Module;
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
import net.minecraft.world.item.ItemStack;

/** Visual editor for independently positioning and styling HUD elements. */
public final class HudEditorScreen extends Screen {
    private static final String[] ELEMENTS = {"watermark", "coordinates", "fps", "player-info", "world-info", "array-list"};
    private static final String[] LABELS = {"Watermark", "Coordinates", "FPS", "Player Info", "World Info", "Array List"};
    private static final int[] COLOR_PRESETS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF55AAFF, 0xFFAA55FF, 0xFFFF55FF, 0xFFAAAAAA};

    private final Screen parent;
    private HudModule hud;
    private ArrayListModule arrayList;
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
        arrayList = (ArrayListModule) ArsonClient.getInstance().modules().get("array-list");
        if (hud == null) return;

        watermarkBox = new EditBox(font, 10, 82, 180, 20, Component.literal("Watermark"));
        watermarkBox.setValue(hud.watermarkText());
        watermarkBox.setMaxLength(32);
        watermarkBox.setHint(Component.literal("Watermark text..."));
        addRenderableWidget(watermarkBox);

        for (int i = 0; i < ELEMENTS.length; i++) {
            final int index = i;
            int buttonWidth = 78;
            addRenderableWidget(Button.builder(Component.literal(elementButtonText(index)), b -> {
                saveWatermarkText();
                selected = ELEMENTS[index];
                rebuild();
            }).bounds(10 + i * 80, 50, buttonWidth, 20).build());
        }

        if (isArrayList()) {
            addRenderableWidget(Button.builder(Component.literal("Color: " + colorHex(arrayList.textColor())), b -> {
                arrayList.cycleTextColor(COLOR_PRESETS);
                b.setMessage(Component.literal("Color: " + colorHex(arrayList.textColor())));
                save();
            }).bounds(200, 82, 100, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Scale -"), b -> {
                arrayList.adjustScale(-0.05);
                save();
                rebuild();
            }).bounds(306, 82, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Scale +"), b -> {
                arrayList.adjustScale(0.05);
                save();
                rebuild();
            }).bounds(382, 82, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Background: " + (arrayList.background() ? "ON" : "OFF")), b -> {
                arrayList.toggleBackground();
                b.setMessage(Component.literal("Background: " + (arrayList.background() ? "ON" : "OFF")));
                save();
            }).bounds(458, 82, 120, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Color: " + colorHex(hud.elementColor(selected))), b -> {
                cycleColor(selected);
                b.setMessage(Component.literal("Color: " + colorHex(hud.elementColor(selected))));
                save();
            }).bounds(200, 82, 100, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Scale -"), b -> {
                adjustScale(selected, -0.05);
                save();
                rebuild();
            }).bounds(306, 82, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Scale +"), b -> {
                adjustScale(selected, 0.05);
                save();
                rebuild();
            }).bounds(382, 82, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Background: " + (hud.elementBackground(selected) ? "ON" : "OFF")), b -> {
                toggleBackground(selected);
                b.setMessage(Component.literal("Background: " + (hud.elementBackground(selected) ? "ON" : "OFF")));
                save();
            }).bounds(458, 82, 120, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Reset Selected"), b -> {
            if (isArrayList()) arrayList.resetSettings();
            else hud.resetElement(selected);
            save();
            rebuild();
        }).bounds(10, this.height - 30, 105, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset All"), b -> {
            for (String element : ELEMENTS) {
                if (element.equals("array-list")) {
                    if (arrayList != null) arrayList.resetSettings();
                } else {
                    hud.resetElement(element);
                }
            }
            save();
            rebuild();
        }).bounds(120, this.height - 30, 85, 20).build());
        addRenderableWidget(Button.builder(Component.literal(isArrayList() ? "Align: " + (arrayList.rightAlign() ? "Right" : "Left") : "Align: " + hud.elementAlignment(selected)), b -> {
            if (isArrayList()) arrayList.toggleRightAlign();
            else hud.cycleAlignment(selected);
            b.setMessage(Component.literal(isArrayList() ? "Align: " + (arrayList.rightAlign() ? "Right" : "Left") : "Align: " + hud.elementAlignment(selected)));
            save();
        }).bounds(215, this.height - 30, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Visible: " + (isArrayList() ? (arrayList.enabled() ? "ON" : "OFF") : (hud.elementVisible(selected) ? "ON" : "OFF"))), b -> {
            if (isArrayList()) arrayList.toggle();
            else hud.setElementVisible(selected, !hud.elementVisible(selected));
            b.setMessage(Component.literal("Visible: " + (isArrayList() ? (arrayList.enabled() ? "ON" : "OFF") : (hud.elementVisible(selected) ? "ON" : "OFF"))));
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

    private boolean isArrayList() { return "array-list".equals(selected) && arrayList != null; }

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
            if (setting instanceof ColorSetting color && color.id().equals(element + "-color")) return color;
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
        if (button == 0 && hitHud(mouseX, mouseY)) {
            dragging = true;
            double[] position = position(selected);
            double scale = isArrayList() ? arrayList.scale() : hud.scale();
            dragOffsetX = mouseX - position[0] * scale;
            dragOffsetY = mouseY - position[1] * scale;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            if (isArrayList()) {
                arrayList.setEditorPosition((mouseX - dragOffsetX) / arrayList.scale(), (mouseY - dragOffsetY) / arrayList.scale());
            } else if (hud != null) {
                hud.setEditorPosition(selected, (mouseX - dragOffsetX) / hud.scale(), (mouseY - dragOffsetY) / hud.scale());
            }
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
        if (isArrayList()) {
            if (!arrayList.enabled()) return false;
        } else if (hud == null || !hud.elementVisible(selected)) {
            return false;
        }
        int[] bounds = previewBounds(selected, position(selected));
        return mouseX >= bounds[0] && mouseX <= bounds[2] && mouseY >= bounds[1] && mouseY <= bounds[3];
    }

    private double[] position(String element) {
        if (isArrayList()) return new double[]{arrayList.x(), arrayList.y()};
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

    private int[] previewBounds(String element, double[] pos) {
        if (isArrayList()) return arrayListPreviewBounds(pos);
        PreviewData data = previewData(element);
        double totalScale = hud.scale() * hud.elementScale(element);
        int left = (int) Math.round(alignedLeft(pos[0], data.width, hud.elementAlignment(element)) * hud.scale() - hud.padding() * totalScale);
        int top = (int) Math.round(pos[1] * hud.scale() - hud.padding() * totalScale);
        int right = left + (int) Math.round(data.width * totalScale + hud.padding() * 2 * totalScale);
        int bottom = top + (int) Math.round(data.height * totalScale + hud.padding() * 2 * totalScale);
        return new int[]{left, top, right, bottom};
    }

    private int[] arrayListPreviewBounds(double[] pos) {
        String[] rows = arrayListRows();
        int width = 70;
        for (String row : rows) width = Math.max(width, font.width(row));
        double scale = arrayList.scale();
        int height = Math.max(12, rows.length * arrayList.spacing());
        int left = arrayList.rightAlign() ? (int) Math.round(pos[0] * scale - width * scale) : (int) Math.round(pos[0] * scale);
        int top = (int) Math.round(pos[1] * scale);
        int padding = (int) Math.round(arrayList.padding() * scale);
        return new int[]{left - padding, top - padding, left + (int) Math.round(width * scale) + padding, top + (int) Math.round(height * scale) + padding};
    }

    private String[] arrayListRows() {
        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        for (Module module : ArsonClient.getInstance().modules().all()) {
            if (!module.enabled() || module.id().equals("array-list")) continue;
            String label = arrayList.showCategory() ? module.name() + "  [" + module.category().displayName() + "]" : module.name();
            rows.add(label);
        }
        rows.sort((a, b) -> Integer.compare(font.width(b), font.width(a)));
        int max = Math.min(rows.size(), arrayList.maxModules());
        return rows.subList(0, max).toArray(String[]::new);
    }

    private PreviewData previewData(String element) {
        String[] rows = previewRows(element);
        int width = 0;
        for (String row : rows) width = Math.max(width, font.width(row));
        width = Math.max(55, width);
        return new PreviewData(width, Math.max(hud.lineSpacing(), rows.length * hud.lineSpacing()), rows);
    }

    private String[] previewRows(String element) {
        var client = minecraft;
        if (client == null || client.player == null || client.level == null) {
            return switch (element) {
                case "watermark" -> new String[]{hud.watermarkText()};
                case "coordinates" -> new String[]{"XYZ 0 64 0"};
                case "fps" -> new String[]{"FPS 60"};
                case "player-info" -> new String[]{"Health 20.0/20.0", "Food 20", "Armor 4/4", "Held Hand"};
                case "world-info" -> new String[]{"Time 12:00", "Dimension minecraft:overworld", "Weather Clear"};
                default -> new String[]{LABELS[elementIndex(element)]};
            };
        }
        return switch (element) {
            case "watermark" -> new String[]{hud.watermarkText()};
            case "coordinates" -> new String[]{String.format(java.util.Locale.ROOT, "XYZ %d %d %d", client.player.blockPosition().getX(), client.player.blockPosition().getY(), client.player.blockPosition().getZ())};
            case "fps" -> new String[]{"FPS " + client.getFps()};
            case "player-info" -> playerRows(client);
            case "world-info" -> worldRows(client);
            default -> new String[]{LABELS[elementIndex(element)]};
        };
    }

    private String[] playerRows(net.minecraft.client.Minecraft client) {
        PlayerInfoModule module = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        if (module == null || !module.enabled()) return new String[]{"Player Info disabled"};
        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        if (module.showHealth()) rows.add(String.format(java.util.Locale.ROOT, "Health %.1f/%.1f", client.player.getHealth(), client.player.getMaxHealth()));
        if (module.showHunger()) rows.add("Food " + client.player.getFoodData().getFoodLevel());
        if (module.showArmor()) rows.add(armor(client));
        if (module.showHeldItem()) {
            ItemStack stack = client.player.getMainHandItem();
            rows.add(stack.isEmpty() ? "Held Hand" : "Held " + stack.getHoverName().getString());
        }
        return rows.isEmpty() ? new String[]{"Player Info empty"} : rows.toArray(String[]::new);
    }

    private String[] worldRows(net.minecraft.client.Minecraft client) {
        WorldInfoModule module = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        if (module == null || !module.enabled()) return new String[]{"World Info disabled"};
        java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        if (module.showTime()) {
            long dayTime = Math.floorMod(client.level.getDayTime(), 24000L);
            long hours = (dayTime / 1000L + 6L) % 24L;
            long minutes = Math.round((dayTime % 1000L) * 60.0 / 1000.0);
            if (minutes == 60) { minutes = 0; hours = (hours + 1) % 24; }
            rows.add(String.format(java.util.Locale.ROOT, "Time %02d:%02d", hours, minutes));
        }
        if (module.showDimension()) rows.add("Dimension " + client.level.dimension().location());
        if (module.showWeather()) rows.add("Weather " + (client.level.isThundering() ? "Thunder" : client.level.isRaining() ? "Rain" : "Clear"));
        return rows.isEmpty() ? new String[]{"World Info empty"} : rows.toArray(String[]::new);
    }

    private String armor(net.minecraft.client.Minecraft client) {
        int equipped = 0, durabilityTotal = 0, durabilityMax = 0;
        for (ItemStack stack : client.player.getArmorSlots()) {
            if (!stack.isEmpty()) {
                equipped++;
                if (stack.isDamageableItem()) {
                    durabilityTotal += stack.getMaxDamage() - stack.getDamageValue();
                    durabilityMax += stack.getMaxDamage();
                }
            }
        }
        return durabilityMax > 0
                ? String.format(java.util.Locale.ROOT, "Armor %d/4  Durability %d%%", equipped, Math.round(durabilityTotal * 100.0f / durabilityMax))
                : "Armor " + equipped + "/4";
    }

    private int elementIndex(String element) {
        for (int i = 0; i < ELEMENTS.length; i++) if (ELEMENTS[i].equals(element)) return i;
        return 0;
    }

    private record PreviewData(int width, int height, String[] rows) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB0101014);
        graphics.text(font, "HUD Editor — select an element, then drag its box", 10, 16, 0xFFFFFFFF, true);
        double[] selectedPosition = hud == null ? new double[]{0, 0} : position(selected);
        String scaleText = isArrayList()
                ? String.format(java.util.Locale.ROOT, "%.2f", arrayList.scale())
                : String.format(java.util.Locale.ROOT, "%.2f", hud.elementScale(selected));
        graphics.text(font, "Selected: " + selected + "  X=" + (int) selectedPosition[0] + " Y=" + (int) selectedPosition[1]
                + "  Scale=" + scaleText, 10, 32, 0xFFD0D0D0, false);
        graphics.text(font, "Watermark text", 10, 72, 0xFFFFFFFF, false);

        if (hud != null) {
            for (int i = 0; i < ELEMENTS.length; i++) {
                String element = ELEMENTS[i];
                if (element.equals("array-list")) {
                    if (arrayList == null || !arrayList.enabled()) continue;
                } else if (!hud.elementVisible(element)) {
                    continue;
                }
                drawPreviewElement(graphics, element);
            }
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawPreviewElement(GuiGraphicsExtractor graphics, String element) {
        if (element.equals("array-list")) {
            drawArrayListPreview(graphics);
            return;
        }
        double[] pos = position(element);
        PreviewData data = previewData(element);
        int[] bounds = previewBounds(element, pos);
        int outline = element.equals(selected) ? 0xFFFFFFFF : 0xFF6A6A6A;
        int fill = hud.elementBackground(element) ? hud.elementBackgroundColor() : 0x30303038;
        graphics.fill(bounds[0], bounds[1], bounds[2], bounds[3], fill);
        graphics.outline(bounds[0], bounds[1], bounds[2] - bounds[0], bounds[3] - bounds[1], outline);

        double totalScale = hud.scale() * hud.elementScale(element);
        int padding = (int) Math.round(hud.padding() * totalScale);
        int baseLeft = (int) Math.round(alignedLeft(pos[0], data.width, hud.elementAlignment(element)) * hud.scale());
        int baseTop = (int) Math.round(pos[1] * hud.scale());
        for (int i = 0; i < data.rows.length; i++) {
            String row = data.rows[i];
            int rowWidth = font.width(row);
            int rowX = switch (hud.elementAlignment(element)) {
                case "center" -> baseLeft - (int) Math.round(rowWidth * totalScale / 2.0);
                case "right" -> baseLeft - (int) Math.round(rowWidth * totalScale);
                default -> baseLeft;
            };
            graphics.text(font, row, rowX, baseTop + padding + (int) Math.round(i * hud.lineSpacing() * totalScale), hud.elementColor(element), hud.showShadow());
        }
    }

    private void drawArrayListPreview(GuiGraphicsExtractor graphics) {
        double[] pos = position("array-list");
        String[] rows = arrayListRows();
        int[] bounds = previewBounds("array-list", pos);
        int outline = selected.equals("array-list") ? 0xFFFFFFFF : 0xFF6A6A6A;
        graphics.fill(bounds[0], bounds[1], bounds[2], bounds[3], arrayList.background() ? arrayList.backgroundColor() : 0x30303038);
        graphics.outline(bounds[0], bounds[1], bounds[2] - bounds[0], bounds[3] - bounds[1], outline);
        double scale = arrayList.scale();
        int width = bounds[2] - bounds[0];
        int baseLeft = arrayList.rightAlign() ? (int) Math.round(pos[0] * scale) : (int) Math.round(pos[0] * scale);
        int top = (int) Math.round(pos[1] * scale);
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            int textWidth = font.width(row);
            int x = arrayList.rightAlign() ? baseLeft - textWidth : baseLeft;
            if (!arrayList.rightAlign()) x = Math.max(0, x);
            graphics.text(font, row, x, top + i * (int) Math.round(arrayList.spacing() * scale), arrayList.textColor(), arrayList.shadow());
        }
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
