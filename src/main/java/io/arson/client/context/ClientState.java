package io.arson.client.context;

/** Immutable platform-neutral snapshot of the client data exposed to features. */
public record ClientState(
        long tick,
        long gameTime,
        boolean inWorld,
        boolean inScreen,
        String dimension,
        double playerX,
        double playerY,
        double playerZ,
        float yaw,
        float pitch,
        float health,
        float maxHealth
) {
    public static ClientState empty() {
        return new ClientState(0L, 0L, false, false, "", 0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public boolean hasPlayer() {
        return inWorld && maxHealth > 0.0f;
    }
}
