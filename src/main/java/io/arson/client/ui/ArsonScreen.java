package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.module.Module;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ArsonScreen extends Screen {
    private final Screen parent;

    public ArsonScreen(Screen parent) {
        super(Component.literal("Arson Client V3"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelX = Math.max(20, this.width / 2 - 260);
        int panelY = Math.max(20, this.height / 2 - 170);
        int buttonX = panelX + 20;
        int y = panelY + 52;

        for (Module module : ArsonClient.getInstance().modules().all()) {
            Module current = module;
            this.addRenderableWidget(Button.builder(moduleLabel(current), button -> {
                current.toggle();
                button.setMessage(moduleLabel(current));
            }).bounds(buttonX, y, 220, 20).build());
            y += 24;

            for (Setting<?> setting : current.settings()) {
                if (setting instanceof BooleanSetting bool) {
                    this.addRenderableWidget(Button.builder(settingLabel(bool), button -> {
                        bool.set(!bool.enabled());
                        button.setMessage(settingLabel(bool));
                    }).bounds(buttonX + 14, y, 206, 18).build());
                    y += 21;
                } else if (setting instanceof DoubleSetting number) {
                    this.addRenderableWidget(Button.builder(settingLabel(number), button -> {
                        double next = number.get() + number.step();
                        if (next > number.max()) next = number.min();
                        number.set(next);
                        button.setMessage(settingLabel(number));
                    }).bounds(buttonX + 14, y, 206, 18).build());
                    y += 21;
                }
            }
            y += 5;
            if (y > panelY + 285) break;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(panelX + 280, panelY + 250, 210, 20).build());
    }

    private static Component moduleLabel(Module module) {
        return Component.literal(module.name() + "  [" + (module.enabled() ? "ON" : "OFF") + "]  " + module.category().displayName());
    }

    private static Component settingLabel(BooleanSetting setting) {
        return Component.literal(setting.name() + ": " + (setting.enabled() ? "ON" : "OFF"));
    }

    private static Component settingLabel(DoubleSetting setting) {
        return Component.literal(setting.name() + ": " + String.format(java.util.Locale.ROOT, "%.1f", setting.get()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = Math.max(20, this.width / 2 - 260);
        int panelY = Math.max(20, this.height / 2 - 170);
        graphics.fill(panelX, panelY, panelX + 520, panelY + 300, 0xE0101014);
        graphics.outline(panelX, panelY, 520, 300, 0xFF4C4C56);
        graphics.text(this.font, "Arson Client V3", panelX + 20, panelY + 18, 0xFFFFFFFF, true);
        graphics.text(this.font, "Modules / Settings", panelX + 20, panelY + 34, 0xFFAAAAAA, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
