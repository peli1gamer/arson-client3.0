package com.arson.client.render;

import java.util.ArrayList;
import java.util.List;

/** Lightweight dispatcher; modules are only visited when enabled by the caller. */
public final class RenderDispatcher {
    private final List<RenderModule> modules = new ArrayList<>();

    public void register(RenderModule module) {
        if (module != null && !modules.contains(module)) {
            modules.add(module);
        }
    }

    public void render(float tickDelta) {
        for (RenderModule module : modules) {
            module.render(tickDelta);
        }
    }

    public void clear() {
        modules.clear();
    }
}
