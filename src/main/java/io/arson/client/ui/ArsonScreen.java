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

import java.util.ArrayList;
import java.util.List;

public final class ArsonScreen extends Screen {
    private final Screen parent;
    private Module.Category selectedCategory = Module.Category.RENDER;
    private final List<Button> contentButtons = new ArrayList<>();

    public ArsonScreen(Screen parent) {
        super(Component.literal("Arson Client V3"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        contentButtons.clear();

        int panelX = Math.max(20, this.width / 2 - 280);
        int panelY = Math.max(20, this.height / 2 - 180);

        int categoryX = panelX + 18;
        int categoryY = panelY + 58;
        for (Module.Category category : Module.Category.values()) {
            Module.Category current = category;
            Button button = Button.builder(Component.literal(category.displayName()), b -> {
                selectedCategory = current;
                rebuild();
            }).bounds(categoryX, categoryY, 105, 20).build();
            this.addRenderableWidget(button);
            categoryY += 24;
        }

        int x = panelX + 145;
        int y = panelY + 58;
        int right = panelX + 530;

        for (Module module : ArsonClient.getInstance().modules().all()) {
            if (module.category() != selectedCategory) continue;
            Module current = module;
            Button moduleButton = Button.builder(moduleLabel(current), button -> {
                current.toggle();
                button.setMessage(moduleLabel(current));
            }).bounds(x, y, 365, 21).build();
            this.addRenderableWidget(moduleButton);
            contentButtons.add(moduleButton);
            y += 25;

            for (Setting<?> setting : current.settings()) {
                if (setting instanceof BooleanSetting bool) {
                    Button settingButton = Button.builder(settingLabel(bool), button -> {
                        bool.set(!bool.enabled());
                        button.setMessage(settingLabel(bool));
                    }).bounds(x + 14, y, 351, 18).build();
                    this.addRenderableWidget(settingButton);
                    contentButtons.add(settingButton);
                    y += 21;
                } else if (setting instanceof DoubleSetting number) {
                    Button settingButton = Button.builder(settingLabel(number), button -> {
                        double next = number.get() + number.step();
                        if (next > number.max()) next = number.min();
                        number.set(next);
                        button.setMessage(settingLabel(number));
                    }).bounds(x + 14, y, 351, 18).build();
                    this.addRenderableWidget(settingButton);
                    contentButtons.add(settingButton);
                    y += 21;
                }
            }
            y += 6;
            if (y > panelY + 305) break;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(right - 100, panelY + 330, 100, 20).build());
    }

    private static Component moduleLabel(Module module) {
        return Component.literal((module.enabled() ? "[ON] " : "[OFF] ") + module.name());
    }

    private static Component settingLabel(BooleanSetting setting) {
        return Component.literal("  " + setting.name() + ": " + (setting.enabled() ? "ON" : "OFF"));
    }

    private static Component settingLabel(DoubleSetting setting) {
        return Component.literal("  " + setting.name() + ": " + String.format(java.util.Locale.ROOT, "%.1f", setting.get()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelX = Math.max(20, this.width / 2 - 280);
        int panelY = Math.max(20, this.height / 2 - 180);
        graphics.fill(panelX, panelY, panelX + 560, panelY + 360, 0xE0101014);
        graphics.outline(panelX, panelY, 560, 360, 0xFF4C4C56);
        graphics.text(this.font, "Arson Client V3", panelX + 18, panelY + 18, 0xFFFFFFFF, true);
        graphics.text(this.font, "Modules / Settings", panelX + 18, panelY + 34, 0xFFAAAAAA, false);
        graphics.text(this.font, selectedCategory.displayName(), panelX + 145, panelY + 40, 0xFFFFFFFF, true);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
