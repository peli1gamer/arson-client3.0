package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Local-only world visuals for movement feedback. */
public final class WorldEffectsModule extends Module {
    public enum EffectType { LANDING_RIPPLE, MOVEMENT_TRAIL }
    public record Effect(EffectType type, double x, double y, double z, int age, float strength) {
        Effect aged() { return new Effect(type, x, y, z, age + 1, strength); }
    }

    private static final int MAX_EFFECTS = 28;
    private final BooleanSetting landingRipple = setting(new BooleanSetting("landing-ripple", "Landing Ripple", true));
    private final BooleanSetting movementTrail = setting(new BooleanSetting("movement-trail", "Movement Trail", true));
    private final ColorSetting effectColor = setting(new ColorSetting("effect-color", "Effect Color", 0xFF65D6FF));
    private final DoubleSetting rippleRadius = setting(new DoubleSetting("ripple-radius", "Ripple Radius", 1.6, 0.5, 4.0, 0.1));
    private final DoubleSetting lifetime = setting(new DoubleSetting("lifetime", "Effect Lifetime", 16.0, 4.0, 40.0, 1.0));
    private final DoubleSetting trailInterval = setting(new DoubleSetting("trail-interval", "Trail Interval", 4.0, 2.0, 12.0, 1.0));
    private final ArrayDeque<Effect> effects = new ArrayDeque<>();
    private boolean wasOnGround;
    private int trailClock;
    private String worldKey = "";

    public WorldEffectsModule() {
        super("world-effects", "World Effects", Category.RENDER,
                "Renders local landing ripples and subtle movement trails; no packets or gameplay state are changed.");
        landingRipple.description("Emit an expanding ground ring when the local player lands.");
        movementTrail.description("Leave short-lived visual marks while moving on the ground.");
        effectColor.description("Shared ARGB color for both effect types.");
        rippleRadius.description("Maximum radius of a landing ripple, in blocks.");
        lifetime.description("Duration of each effect in ticks.");
        trailInterval.description("Ticks between movement trail marks.");
    }

    @Override protected void onEnable() {
        effects.clear();
        wasOnGround = false;
        trailClock = 0;
        worldKey = "";
    }

    @Override protected void onDisable() {
        effects.clear();
        wasOnGround = false;
        trailClock = 0;
        worldKey = "";
    }

    @Override protected void onTick(Minecraft client) {
        Player player = client.player;
        if (player == null || client.level == null) {
            effects.clear();
            wasOnGround = false;
            trailClock = 0;
            worldKey = "";
            return;
        }
        String currentWorld = client.level.dimension().identifier().toString();
        if (!currentWorld.equals(worldKey)) {
            effects.clear();
            worldKey = currentWorld;
            wasOnGround = player.onGround();
            trailClock = 0;
        }

        List<Effect> updated = new ArrayList<>(effects.size());
        int maxAge = (int) Math.round(lifetime.get());
        for (Effect effect : effects) {
            Effect next = effect.aged();
            if (next.age() < maxAge) updated.add(next);
        }
        effects.clear();
        effects.addAll(updated);

        boolean onGround = player.onGround();
        if (landingRipple.enabled() && onGround && !wasOnGround) {
            addEffect(new Effect(EffectType.LANDING_RIPPLE, player.getX(), player.getY() + 0.03, player.getZ(), 0, 1.0f));
        }

        double vx = player.getDeltaMovement().x;
        double vz = player.getDeltaMovement().z;
        double speed = Math.sqrt(vx * vx + vz * vz);
        if (movementTrail.enabled() && onGround && speed > 0.12) {
            trailClock++;
            if (trailClock >= (int) Math.round(trailInterval.get())) {
                trailClock = 0;
                addEffect(new Effect(EffectType.MOVEMENT_TRAIL, player.getX(), player.getY() + 0.04, player.getZ(),
                        0, (float) Math.max(0.25, Math.min(1.0, speed * 2.0))));
            }
        } else {
            trailClock = 0;
        }
        wasOnGround = onGround;
    }

    private void addEffect(Effect effect) {
        while (effects.size() >= MAX_EFFECTS) effects.removeFirst();
        effects.addLast(effect);
    }

    public List<Effect> effects() { return List.copyOf(effects); }
    public int colorArgb() { return effectColor.get(); }
    public double rippleRadius() { return rippleRadius.get(); }
    public int lifetimeTicks() { return (int) Math.round(lifetime.get()); }
}
