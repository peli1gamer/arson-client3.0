package io.arson.client.ui;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.Setting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.font.TextRenderer;
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
            return ButtonWidget.builder(label(setting.name(), bool.enabled() ? "ON" : "OFF"), button -> {
                bool.set(!bool.enabled());
                changed.run();
            }).dimensions(x, y, width, height).build();
        }

        if (setting instanceof DoubleSetting number) {
            return new DoubleSettingSlider(x, y, width, height, number, persist);
        }

        if (setting instanceof EnumSetting<?> enumeration) {
            return ButtonWidget.builder(label(setting.name(), pretty(enumeration.get())), button -> {
                enumeration.cycle(1);
                changed.run();
            }).dimensions(x, y, width, height).build();
        }

        if (setting instanceof ColorSetting color) {
            return ButtonWidget.builder(label(setting.name(), "#" + String.format(Locale.ROOT, "%08X", color.get())),
                    button -> edit.accept(setting)).dimensions(x, y, width, height).build();
        }

        if (setting instanceof StringSetting string) {
            return ButtonWidget.builder(label(setting.name(), string.get()),
                    button -> edit.accept(setting)).dimensions(x, y, width, height).build();
        }

        return ButtonWidget.builder(label(setting.name(), String.valueOf(setting.get())), button -> { })
                .dimensions(x, y, width, height).build();
    }

    public static TextFieldWidget createEditor(TextRenderer renderer, int x, int y, int width, int height,
                                               StringSetting string, ColorSetting color, String currentText) {
        if (string == null && color == null) throw new IllegalArgumentException("A string or color setting is required");
        String title = string != null ? string.name() : color.name();
        TextFieldWidget editor = new TextFieldWidget(renderer, x, y, width, height, Text.literal(title));
        editor.setMaxLength(string != null ? string.maxLength() : 8);
        String initial = currentText == null ? "" : currentText;
        if (initial.isEmpty()) {
            initial = string != null ? string.get() : String.format(Locale.ROOT, "%08X", color.get());
        }
        editor.setText(initial);
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
