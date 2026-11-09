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

        for (Module candidate : ArsonClient.getInstance().modules().all()) {
            if (candidate.id().equals("array-list")) continue;
            float target = candidate.enabled() ? 1.0f : 0.0f;
            float current = ANIMATION.getOrDefault(candidate.id(), target);
            if (!module.animation()) {
                current = target;
            } else {
                current = advanceAnimation(current, target, deltaSeconds, 7.0f * module.animationSpeed());
            }
            ANIMATION.put(candidate.id(), current);
        }

        int viewportW = client.getWindow().getScaledWidth();
        int viewportH = client.getWindow().getScaledHeight();
        List<Module> all = visibleModules(client, module);
        int maxWidth = 1;
        for (Module candidate : all) maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(label(candidate, module)));
        double renderScale = fitScale(module.scale(), maxWidth, all.size(), viewportW, viewportH,
                module.spacing(), module.padding());

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

    /** Measures the exact rows and fitted scale used by the renderer for HUD collision layout. */
    static int[] estimateBounds(MinecraftClient client, ArrayListModule module) {
        if (client == null || module == null || !module.enabled()) return new int[]{1, 1};
        int viewportW = client.getWindow().getScaledWidth();
        int viewportH = client.getWindow().getScaledHeight();
        List<Module> visible = visibleModules(client, module);
        int maxWidth = 1;
        for (Module candidate : visible) maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(label(candidate, module)));
        double scale = fitScale(module.scale(), maxWidth, visible.size(), viewportW, viewportH,
                module.spacing(), module.padding());
        return new int[]{
                Math.max(1, (int) Math.ceil((maxWidth + module.padding() * 2.0) * scale)),
                Math.max(1, (int) Math.ceil((visible.size() * module.spacing() + module.padding() * 2.0) * scale))
        };
    }

    private static List<Module> visibleModules(MinecraftClient client, ArrayListModule module) {
        List<Module> visible = new ArrayList<>();
        for (Module candidate : ArsonClient.getInstance().modules().all()) {
            if (!candidate.id().equals("array-list")
                    && ANIMATION.getOrDefault(candidate.id(), candidate.enabled() ? 1.0f : 0.0f) > 0.01f) {
                visible.add(candidate);
            }
        }
        visible.sort(Comparator
                .comparingInt((Module value) -> client.textRenderer.getWidth(label(value, module)))
                .reversed()
                .thenComparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        if (visible.size() > module.maxModules()) visible = new ArrayList<>(visible.subList(0, module.maxModules()));
        int viewportH = client.getWindow().getScaledHeight();
        int maxVisible = Math.max(1, (int) Math.floor(Math.max(1, viewportH - 12)
                / (double) Math.max(1, module.spacing())));
        if (visible.size() > maxVisible) visible = new ArrayList<>(visible.subList(0, maxVisible));
        return visible;
    }

    static float fitScale(double configuredScale, int maxWidth, int rowCount,
                          int viewportWidth, int viewportHeight, int spacing, int padding) {
        double scale = Math.max(0.05, configuredScale);
        if (maxWidth > 0) {
            scale = Math.min(scale, (Math.max(1, viewportWidth - 12.0))
                    / Math.max(1.0, maxWidth + padding * 2.0));
        }
        if (rowCount > 0) {
            scale = Math.min(scale, (Math.max(1, viewportHeight - 12.0))
                    / Math.max(1.0, rowCount * spacing + padding * 2.0));
        }
        return (float) Math.max(0.05, scale);
    }

    /**
     * Advances a HUD row with exponential smoothing, matching the frame-rate-independent
     * animation approach used by Anubis while retaining Arson's configurable speed.
     */
    static float advanceAnimation(float current, float target, float deltaSeconds, float speed) {
        if (!Float.isFinite(target)) target = 0.0f;
        target = Math.max(0.0f, Math.min(1.0f, target));
        if (!Float.isFinite(current)) current = target;
        current = Math.max(0.0f, Math.min(1.0f, current));
        if (!Float.isFinite(deltaSeconds) || !Float.isFinite(speed)) return current;

        float delta = Math.max(0.0f, Math.min(0.1f, deltaSeconds));
        float rate = Math.max(0.0f, speed);
        float blend = 1.0f - (float) Math.exp(-rate * delta);
        float next = current + (target - current) * blend;
        if (Math.abs(target - next) < 0.01f) next = target;
        return Math.max(0.0f, Math.min(1.0f, next));
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
