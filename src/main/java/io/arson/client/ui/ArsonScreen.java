package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.module.Module;
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
        int panelX = Math.max(20, this.width / 2 - 210);
        int panelY = Math.max(20, this.height / 2 - 130);
        int buttonX = panelX + 20;
        int y = panelY + 48;

        for (Module module : ArsonClient.getInstance().modules().all()) {
            Module current = module;
            this.addRenderableWidget(Button.builder(
                    Component.literal(current.name() + (current.enabled() ? "  [ON]" : "  [OFF]")),
                    button -> {
                        current.toggle();
                        button.setMessage(Component.literal(current.name() + (current.enabled() ? "  [ON]" : "  [OFF]")));
                    }
            ).bounds(buttonX, y, 170, 20).build());
            y += 26;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(buttonX, panelY + 180, 170, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int panelX = Math.max(20, this.width / 2 - 210);
        int panelY = Math.max(20, this.height / 2 - 130);
        graphics.fill(panelX, panelY, panelX + 420, panelY + 220, 0xE0101014);
        graphics.outline(panelX, panelY, 420, 220, 0xFF4C4C56);
        graphics.text(this.font, "Arson Client V3", panelX + 20, panelY + 18, 0xFFFFFFFF, true);
        graphics.text(this.font, "Modules", panelX + 20, panelY + 34, 0xFFAAAAAA, false);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
