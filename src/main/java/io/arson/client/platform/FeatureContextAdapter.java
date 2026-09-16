package io.arson.client.platform;

import io.arson.client.context.FeatureContext;

/** Platform boundary for feeding live lifecycle/tick/render data into the feature context. */
public interface FeatureContextAdapter {
    void initialize(FeatureContext context);
    void tick(FeatureContext context);
    void render(FeatureContext context, long frame, float partialTick);
    void disconnect(FeatureContext context);
}
