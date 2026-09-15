package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.Module;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** HUD renderer for the enabled-module array list. */
public final class ArrayListRenderer {
    private ArrayListRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        ArrayListModule module = (ArrayListModule) ArsonClient.getInstance().modules().get("array-list");
        if (module == null || !module.enabled()) return;

        List<Module> enabled = new ArrayList<>();
        for (Module candidate : ArsonClient.getInstance().modules().all()) {
            if (candidate.enabled() && !candidate.id().equals("array-list")) enabled.add(candidate);
        }
        enabled.sort(Comparator.comparingInt((Module value) -> client.font.width(label(value, module))).reversed());
        if (enabled.size() > module.maxModules()) enabled = enabled.subList(0, module.maxModules());

        graphics.pose().pushMatrix();
        graphics.pose().translate(module.x(), module.y());
        graphics.pose().scale(module.scale(), module.scale());

        int line = module.spacing();
        int padding = module.padding();
        for (int i = 0; i < enabled.size(); i++) {
            Module candidate = enabled.get(i);
            String text = label(candidate, module);
            int width = client.font.width(text);
            int x = module.rightAlign() ? -width : 0;
            int top = i * line;

            if (module.background()) {
                graphics.fill(x - padding, top - padding, x + width + padding,
                        top + line - 1 + padding, module.backgroundColor());
            }
            graphics.drawString(client.font, text, x, top, module.textColor(), module.shadow());
        }
        graphics.pose().popMatrix();
    }

    private static String label(Module module, ArrayListModule settings) {
        if (!settings.showCategory()) return module.name();
        return module.name() + " [" + module.category().displayName() + "]";
    }
}
