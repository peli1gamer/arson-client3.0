package io.arson.client.ui;

import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Step-aware 1.21.11 slider with mouse drag plus keyboard fallback. */
public final class DoubleSettingSlider extends AbstractSliderButton {
    private final DoubleSetting setting;
    private final Runnable changed;

    public DoubleSettingSlider(int x, int y, int width, int height, DoubleSetting setting, Runnable changed) {
        super(x, y, width, height, Component.empty(), setting.normalized());
        this.setting = setting;
        this.changed = changed;
        updateMessage();
    }

    @Override protected void updateMessage() {
        setMessage(Component.literal(setting.name() + ": " + String.format(Locale.ROOT, "%.2f", setting.get())));
    }

    @Override protected void applyValue() {
        double raw = setting.min() + value * (setting.max() - setting.min());
        setting.set(raw);
        setValue(setting.normalized());
        updateMessage();
        changed.run();
    }

    @Override public boolean keyPressed(KeyEvent event) {
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

    @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        int usable = Math.max(1, width - 8);
        int fill = (int) Math.round(usable * setting.normalized());
        graphics.fill(getX() + 4, getY() + height - 3, getX() + 4 + fill, getY() + height - 1, 0xFF55AAFF);
    }
}
