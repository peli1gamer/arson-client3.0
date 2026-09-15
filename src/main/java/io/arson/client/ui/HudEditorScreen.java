package io.arson.client.ui;

import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.HudModule;
import io.arson.client.module.PlayerInfoModule;
import io.arson.client.module.WorldInfoModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Visual editor for positioning the HUD block. */
public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudModule hud;
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
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Reset Position"), b -> {
            if (hud != null) hud.setEditorPosition(6, 6);
        }).bounds(10, this.height - 30, 105, 20).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Grid: " + (hud != null && hud.gridSnap() ? "ON" : "OFF")), b -> {
            if (hud != null) {
                hud.setGridSnap(!hud.gridSnap());
                b.setMessage(Component.literal("Grid: " + (hud.gridSnap() ? "ON" : "OFF")));
            }
        }).bounds(120, this.height - 30, 85, 20).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width - 80, this.height - 30, 70, 20).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hud != null && hitHud(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - hud.x() * hud.scale();
            dragOffsetY = mouseY - hud.y() * hud.scale();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0 && hud != null) {
            hud.setEditorPosition((mouseX - dragOffsetX) / hud.scale(), (mouseY - dragOffsetY) / hud.scale());
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
        if (hud == null) return false;
        int width = 90;
        int rows = 1;
        PlayerInfoModule playerInfo = (PlayerInfoModule) ArsonClient.getInstance().modules().get("player-info");
        WorldInfoModule worldInfo = (WorldInfoModule) ArsonClient.getInstance().modules().get("world-info");
        if (hud.showCoordinates()) rows++;
        if (hud.showFps()) rows++;
        if (playerInfo != null && playerInfo.enabled()) {
            if (playerInfo.showHealth()) rows++;
            if (playerInfo.showHunger()) rows++;
            if (playerInfo.showArmor()) rows++;
            if (playerInfo.showHeldItem()) rows++;
        }
        if (worldInfo != null && worldInfo.enabled()) {
            if (worldInfo.showTime()) rows++;
            if (worldInfo.showDimension()) rows++;
            if (worldInfo.showWeather()) rows++;
        }
        int height = rows * hud.lineSpacing() + hud.padding() * 2;
        double left = hud.x() * hud.scale() - hud.padding() * hud.scale();
        double top = hud.y() * hud.scale() - hud.padding() * hud.scale();
        double right = left + width * hud.scale() + hud.padding() * 2 * hud.scale();
        double bottom = top + height * hud.scale();
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB0101014);
        graphics.text(font, "HUD Editor — drag the HUD to move it", 10, 10, 0xFFFFFFFF, true);
        if (hud != null) {
            int left = (int) Math.round(hud.x() * hud.scale() - hud.padding() * hud.scale());
            int top = (int) Math.round(hud.y() * hud.scale() - hud.padding() * hud.scale());
            int right = left + 120;
            int bottom = top + Math.max(22, hud.lineSpacing() * 3);
            graphics.fill(left, top, right, bottom, 0x50206080);
            graphics.outline(left, top, right - left, bottom - top, 0xFFFFFFFF);
            graphics.text(font, "Arson V3", left + hud.padding(), top + hud.padding(), hud.textColor(), hud.showShadow());
            graphics.text(font, "Drag here", left + hud.padding(), top + hud.padding() + hud.lineSpacing(), 0xFFFFFFFF, false);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void save() {
        if (minecraft != null) ConfigManager.save(minecraft, ArsonClient.getInstance().modules());
    }

    @Override
    public void onClose() {
        save();
        minecraft.gui.setScreen(parent);
    }
}
