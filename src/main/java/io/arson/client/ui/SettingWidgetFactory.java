package io.arson.client.ui;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.IntSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Creates the ClickGUI controls for Arson's Meteor-compatible typed settings.
 * Keeping row and editor construction here makes the setting UI reusable by
 * other screens without duplicating value-specific behavior.
 */
public final class SettingWidgetFactory {
    private SettingWidgetFactory() { }

    public static ClickableWidget createRow(Setting<?> setting, int x, int y, int width, int height,
                                            Runnable changed, Runnable persist, Consumer<Setting<?>> edit) {
        if (setting instanceof BooleanSetting bool) {
            return withDescription(ButtonWidget.builder(label(setting.name(), bool.enabled() ? "ON" : "OFF"), button -> {
                bool.set(!bool.enabled());
                changed.run();
            }).dimensions(x, y, width, height).build(), setting);
        }

        if (setting instanceof IntSetting number) {
            return withTooltip(new IntSettingSlider(x, y, width, height, number, persist), setting,
                    "Range: " + number.min() + " to " + number.max() + "  •  Step: " + number.step());
        }

        if (setting instanceof DoubleSetting number) {
            return withTooltip(new DoubleSettingSlider(x, y, width, height, number, persist), setting,
                    "Range: " + number.min() + " to " + number.max() + "  •  Step: " + number.step());
        }

        if (setting instanceof EnumSetting<?> enumeration) {
            ClickableWidget widget = ButtonWidget.builder(label(setting.name(), pretty(enumeration.get()) + "  ›"), button -> {
                enumeration.cycle(1);
                changed.run();
            }).dimensions(x, y, width, height).build();
            String options = enumeration.values().stream().map(SettingWidgetFactory::pretty)
                    .reduce((left, right) -> left + "  •  " + right).orElse("");
            return withTooltip(widget, setting, "Click to cycle. Options: " + options);
        }

        if (setting instanceof ColorSetting color) {
            return withTooltip(ButtonWidget.builder(label(setting.name(), "Color"),
                    button -> edit.accept(setting)).dimensions(x, y, width, height).build(), setting,
                    "ARGB: #" + String.format(Locale.ROOT, "%08X", color.get()) + ". Click to edit as RRGGBB or AARRGGBB.");
        }

        if (setting instanceof StringSetting string) {
            return withTooltip(ButtonWidget.builder(label(setting.name(), string.get()),
                    button -> edit.accept(setting)).dimensions(x, y, width, height).build(), setting,
                    "Text value. Maximum length: " + string.maxLength() + ". Click to edit.");
        }

        return withDescription(ButtonWidget.builder(label(setting.name(), String.valueOf(setting.get())), button -> { })
                .dimensions(x, y, width, height).build(), setting);
    }

    private static <T extends ClickableWidget> T withDescription(T widget, Setting<?> setting) {
        return withTooltip(widget, setting, "");
    }

    private static <T extends ClickableWidget> T withTooltip(T widget, Setting<?> setting, String detail) {
        String description = setting.description();
        String tooltip = description == null || description.isBlank() ? detail
                : detail == null || detail.isBlank() ? description : description + "\n" + detail;
        if (tooltip != null && !tooltip.isBlank()) {
            widget.setTooltip(Tooltip.of(Text.literal(tooltip)));
        }
        return widget;
    }

    public static TextFieldWidget createEditor(TextRenderer renderer, int x, int y, int width, int height,
                                               StringSetting string, ColorSetting color, String currentText) {
        if (string == null && color == null) throw new IllegalArgumentException("A string or color setting is required");
        String title = string != null ? string.name() : color.name();
        TextFieldWidget editor = new TextFieldWidget(renderer, x, y, width, height, Text.literal(title));
        editor.setMaxLength(string != null ? string.maxLength() : 9);
        String initial = currentText != null ? currentText
                : string != null ? string.get() : String.format(Locale.ROOT, "%08X", color.get());
        editor.setText(initial);
        String description = string != null ? string.description() : color.description();
        if (description != null && !description.isBlank()) {
            editor.setTooltip(Tooltip.of(Text.literal(description)));
        }
        return editor;
    }

    private static Text label(String name, String value) {
        return Text.literal(name + "  " + value);
    }

    private static String pretty(Object value) {
        if (value == null) return "None";
        String raw = value.toString().toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(raw.length());
        boolean upper = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '_' || c == '-') {
                result.append(' ');
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return result.toString();
    }
}
