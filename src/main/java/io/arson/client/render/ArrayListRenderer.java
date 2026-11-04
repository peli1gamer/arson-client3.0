package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.ArrayListModule;
import io.arson.client.module.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** HUD renderer for the enabled-module array list. */
public final class ArrayListRenderer {
    private static final Map<String, Float> ANIMATION = new HashMap<>();
    private static long lastFrameNanos = System.nanoTime();

    private ArrayListRenderer() {}

    public static void render(DrawContext graphics, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        ArrayListModule module = (ArrayListModule) ArsonClient.getInstance().modules().get("array-list");
        if (module == null || !module.enabled()) return;

        long now = System.nanoTime();
        float deltaSeconds = Math.min(0.1f, Math.max(0.0f, (now - lastFrameNanos) / 1_000_000_000.0f));
        lastFrameNanos = now;

        List<Module> all = new ArrayList<>();
        for (Module candidate : ArsonClient.getInstance().modules().all()) {
            if (!candidate.id().equals("array-list")) all.add(candidate);
        }

        for (Module candidate : all) {
            float target = candidate.enabled() ? 1.0f : 0.0f;
            float current = ANIMATION.getOrDefault(candidate.id(), target);
            if (!module.animation()) {
                current = target;
            } else {
                float speed = 7.0f * module.animationSpeed();
                float step = Math.min(1.0f, deltaSeconds * speed);
                current += (target - current) * step;
                if (Math.abs(target - current) < 0.01f) current = target;
            }
            ANIMATION.put(candidate.id(), current);
        }

        all.removeIf(candidate -> ANIMATION.getOrDefault(candidate.id(), 0.0f) <= 0.01f);
        all.sort(Comparator
                .comparingInt((Module value) -> client.textRenderer.getWidth(label(value, module)))
                .reversed()
                .thenComparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        if (all.size() > module.maxModules()) all = all.subList(0, module.maxModules());
        int viewportW=client.getWindow().getScaledWidth(), viewportH=client.getWindow().getScaledHeight();
        int maxVisible=Math.max(1,(int)Math.floor(Math.max(1,viewportH-12)/(double)Math.max(1,module.spacing())));
        if(all.size()>maxVisible) all=all.subList(0,maxVisible);
        int maxWidth=1; for(Module candidate:all) maxWidth=Math.max(maxWidth,client.textRenderer.getWidth(label(candidate,module)));
        double renderScale=module.scale();
        if(maxWidth>0) renderScale=Math.min(renderScale,(viewportW-12.0)/maxWidth);
        if(!all.isEmpty()) renderScale=Math.min(renderScale,(viewportH-12.0)/(all.size()*module.spacing()+module.padding()*2.0));
        renderScale=Math.max(0.05,renderScale);

        graphics.getMatrices().pushMatrix();
        graphics.getMatrices().translate(module.rightAlign() ? viewportW - module.x() : module.x(), module.y());
        graphics.getMatrices().scale((float)renderScale, (float)renderScale);

        int line = module.spacing();
        int padding = module.padding();
        for (int i = 0; i < all.size(); i++) {
            Module candidate = all.get(i);
            float progress = ANIMATION.getOrDefault(candidate.id(), 0.0f);
            String text = label(candidate, module);
            int width = client.textRenderer.getWidth(text);
            int baseX = module.rightAlign() ? -width : 0;
            int slide = Math.round((1.0f - progress) * 12.0f);
            int x = module.rightAlign() ? baseX + slide : baseX - slide;
            int top = i * line;

            if (module.background()) {
                graphics.fill(x - padding, top - padding, x + width + padding,
                        top + line - 1 + padding, applyAlpha(module.backgroundColor(), progress));
            }

            int color = module.categoryColors() ? module.colorFor(candidate.category()) : module.textColor();
            graphics.drawText(client.textRenderer, text, x, top, applyAlpha(color, progress), module.shadow());
        }
        graphics.getMatrices().popMatrix();

        cleanupAnimationState(all);
    }

    private static void cleanupAnimationState(List<Module> rendered) {
        if (ANIMATION.size() < 128) return;
        Iterator<String> iterator = ANIMATION.keySet().iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            if (ANIMATION.getOrDefault(id, 0.0f) <= 0.0f) iterator.remove();
        }
    }

    private static int applyAlpha(int color, float progress) {
        int alpha = (color >>> 24) & 0xFF;
        int animatedAlpha = Math.max(0, Math.min(255, Math.round(alpha * progress)));
        return (color & 0x00FFFFFF) | (animatedAlpha << 24);
    }

    private static String label(Module module, ArrayListModule settings) {
        if (!settings.showCategory()) return module.name();
        return module.name() + " [" + module.category().displayName() + "]";
    }
}
