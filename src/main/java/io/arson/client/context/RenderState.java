package io.arson.client.context;

/** Platform-neutral render lifecycle snapshot. */
public record RenderState(long frame, long tick, float partialTick, Phase phase) {
    public enum Phase { NONE, AFTER_ENTITIES }

    public static RenderState empty() {
        return new RenderState(0L, 0L, 0.0f, Phase.NONE);
    }
}
