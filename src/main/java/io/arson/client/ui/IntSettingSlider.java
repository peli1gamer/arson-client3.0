package io.arson.client.ui;

import io.arson.client.settings.IntSetting;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** Integer slider with step-aware mouse and keyboard input. */
public final class IntSettingSlider extends SliderWidget {
    private final IntSetting setting;
    private final Runnable changed;

    public IntSettingSlider(int x, int y, int width, int height, IntSetting setting, Runnable changed) {
        super(x, y, width, height, Text.empty(), setting.normalized());
        this.setting = setting;
        this.changed = changed;
        updateMessage();
    }

    @Override protected void updateMessage() {
        setMessage(Text.literal(setting.name() + ": " + String.format(Locale.ROOT, "%d", setting.get())));
    }

    @Override protected void applyValue() {
        double raw = setting.min() + value * ((double) setting.max() - setting.min());
        setting.set((int) Math.round(raw));
        setValue(setting.normalized());
        updateMessage();
        changed.run();
    }

    @Override public boolean keyPressed(KeyInput event) {
        long next = setting.get();
        switch (event.key()) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_DOWN -> next -= setting.step();
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_UP -> next += setting.step();
            case GLFW.GLFW_KEY_PAGE_DOWN -> next -= (long) setting.step() * 10;
            case GLFW.GLFW_KEY_PAGE_UP -> next += (long) setting.step() * 10;
            case GLFW.GLFW_KEY_HOME -> next = setting.min();
            case GLFW.GLFW_KEY_END -> next = setting.max();
            default -> { return super.keyPressed(event); }
        }
        setting.set((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, next)));
        setValue(setting.normalized());
        updateMessage();
        changed.run();
        return true;
    }
}
