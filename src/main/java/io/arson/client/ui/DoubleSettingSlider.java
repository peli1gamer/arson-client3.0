package io.arson.client.ui;

import io.arson.client.settings.DoubleSetting;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** Step-aware 1.21.11 slider with mouse drag plus keyboard fallback. */
public final class DoubleSettingSlider extends SliderWidget {
    private final DoubleSetting setting;
    private final Runnable changed;

    public DoubleSettingSlider(int x, int y, int width, int height, DoubleSetting setting, Runnable changed) {
        super(x, y, width, height, Text.empty(), setting.normalized());
        this.setting = setting;
        this.changed = changed;
        updateMessage();
    }

    @Override protected void updateMessage() {
        setMessage(Text.literal(setting.name() + ": " + String.format(Locale.ROOT, "%.2f", setting.get())));
    }

    @Override protected void applyValue() {
        double raw = setting.min() + value * (setting.max() - setting.min());
        setting.set(raw);
        setValue(setting.normalized());
        updateMessage();
        changed.run();
    }

    @Override public boolean keyPressed(KeyInput event) {
        double next = setting.get();
        switch (event.key()) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_DOWN -> next -= setting.step();
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_UP -> next += setting.step();
            case GLFW.GLFW_KEY_PAGE_DOWN -> next -= setting.step() * 10.0;
            case GLFW.GLFW_KEY_PAGE_UP -> next += setting.step() * 10.0;
            case GLFW.GLFW_KEY_HOME -> next = setting.min();
            case GLFW.GLFW_KEY_END -> next = setting.max();
            default -> { return super.keyPressed(event); }
        }
        setting.set(next);
        setValue(setting.normalized());
        updateMessage();
        changed.run();
        return true;
    }
}
