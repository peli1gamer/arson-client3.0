package io.arson.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Random;

/** Crosshair-focused aim assistance with configurable target highlighting. */
public final class AimAssistModule extends Module {
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 5.0, 1.0, 16.0, 0.5));
    private final DoubleSetting fov = setting(new DoubleSetting("fov", "FOV", 180.0, 1.0, 360.0, 1.0));
    private final DoubleSetting crosshairWidth = setting(new DoubleSetting("crosshair-width", "Crosshair Width", 15.0, 1.0, 100.0, 1.0));
    private final DoubleSetting crosshairHeight = setting(new DoubleSetting("crosshair-height", "Crosshair Height", 15.0, 1.0, 100.0, 1.0));
    private final DoubleSetting speed = setting(new DoubleSetting("speed", "Aim Speed", 5.0, 0.1, 30.0, 0.1));
    private final DoubleSetting maxRotation = setting(new DoubleSetting("max-rotation", "Max Rotation", 2.5, 0.1, 20.0, 0.1));
    private final DoubleSetting randomNoise = setting(new DoubleSetting("random-noise", "Random Rotation", 0.5, 0.0, 3.0, 0.05));
    private final DoubleSetting targetLock = setting(new DoubleSetting("target-lock", "Target Lock", 0.35, 0.0, 1.0, 0.05));

    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting monsters = setting(new BooleanSetting("monsters", "Monsters", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting visibleOnly = setting(new BooleanSetting("visible-only", "Visible Only", true));
    private final BooleanSetting instant = setting(new BooleanSetting("instant", "Instant", false));
    private final BooleanSetting randomize = setting(new BooleanSetting("randomize", "Randomize Rotation", true));
    private final BooleanSetting sticky = setting(new BooleanSetting("sticky", "Sticky Target", true));
    private final BooleanSetting nearestHitboxPoint = setting(new BooleanSetting("nearest-hitbox-point", "Nearest Hitbox Point", true));
    private final StringSetting activation = setting(new StringSetting("activation", "Activation", "Always"));
    private final StringSetting aimPoint = setting(new StringSetting("aim-point", "Aim Point", "Closest"));

    private final BooleanSetting highlightTarget = setting(new BooleanSetting("highlight-target", "Highlight Target", true));
    private final ColorSetting highlightColor = setting(new ColorSetting("highlight-color", "Highlight Color", 0xFFFF4040));
    private final BooleanSetting highlightFill = setting(new BooleanSetting("highlight-fill", "Highlight Fill", false));
    private final BooleanSetting highlightOutline = setting(new BooleanSetting("highlight-outline", "Highlight Outline", true));
    private final DoubleSetting highlightFillAlpha = setting(new DoubleSetting("highlight-fill-alpha", "Fill Alpha", 0.18, 0.0, 1.0, 0.01));
    private final DoubleSetting highlightOutlineAlpha = setting(new DoubleSetting("highlight-outline-alpha", "Outline Alpha", 1.0, 0.0, 1.0, 0.01));
    private final DoubleSetting highlightLineWidth = setting(new DoubleSetting("highlight-line-width", "Outline Width", 2.0, 0.5, 8.0, 0.5));
    private final DoubleSetting highlightExpand = setting(new DoubleSetting("highlight-expand", "Box Expand", 0.02, 0.0, 0.25, 0.01));

    private final Random random = new Random();
    private LivingEntity target;

    public AimAssistModule() { super("aim-assist", "Aim Assist", Category.COMBAT); }

    @Override protected void onDisable() { target = null; }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null || client.level == null || !isActivated(client)) { target = null; return; }
        LivingEntity selected = chooseTarget(client);
        if (selected == null) { target = null; return; }
        target = selected;
        rotateTowards(client, aimPoint(client, target));
    }

    private boolean isActivated(Minecraft client) {
        String mode = activation.get().trim().toLowerCase();
        return switch (mode) {
            case "hold right click", "right click" -> client.options.keyUse.isDown();
            case "hold left click", "left click" -> client.options.keyAttack.isDown();
            case "hold key", "key" -> keyCode() > 0 && InputConstants.isKeyDown(client.getWindow(), keyCode());
            default -> true;
        };
    }

    private LivingEntity chooseTarget(Minecraft client) {
        AABB search = client.player.getBoundingBox().inflate(range.get());
        LivingEntity locked = target;
        if (sticky.enabled() && locked != null && isValid(client, locked) && inCrosshairGate(client, locked)) return locked;
        return client.level.getEntitiesOfClass(LivingEntity.class, search, entity -> isValid(client, entity) && inCrosshairGate(client, entity))
                .stream().min(Comparator.comparingDouble((LivingEntity entity) -> crosshairDistance(client, entity))
                .thenComparingDouble(entity -> client.player.distanceToSqr(entity))).orElse(null);
    }

    private boolean isValid(Minecraft client, LivingEntity entity) {
        if (entity == null || entity == client.player || !entity.isAlive() || entity.isRemoved()) return false;
        if (client.player.distanceToSqr(entity) > range.get() * range.get()) return false;
        if (visibleOnly.enabled() && !client.player.hasLineOfSight(entity)) return false;
        if (entity instanceof Player) return players.enabled();
        if (entity instanceof Monster) return monsters.enabled();
        if (entity instanceof Animal) return animals.enabled();
        return false;
    }

    private boolean inCrosshairGate(Minecraft client, LivingEntity entity) {
        Vec3 point = aimPoint(client, entity);
        double[] angles = anglesTo(client, point);
        double yawPixels = pixelsFromYaw(client, Math.abs(Mth.wrapDegrees(angles[0] - client.player.getYRot())));
        double pitchPixels = pixelsFromPitch(client, Math.abs(angles[1] - client.player.getXRot()));
        double halfW = crosshairWidth.get() * 0.5;
        double halfH = crosshairHeight.get() * 0.5;
        return yawPixels <= halfW && pitchPixels <= halfH;
    }

    private double crosshairDistance(Minecraft client, LivingEntity entity) {
        Vec3 point = aimPoint(client, entity);
        double[] angles = anglesTo(client, point);
        double yaw = Mth.wrapDegrees(angles[0] - client.player.getYRot());
        double pitch = angles[1] - client.player.getXRot();
        return Math.hypot(pixelsFromYaw(client, Math.abs(yaw)), pixelsFromPitch(client, Math.abs(pitch)));
    }

    private Vec3 aimPoint(Minecraft client, LivingEntity entity) {
        AABB box = entity.getBoundingBox();
        String mode = aimPoint.get().trim().toLowerCase();
        if (mode.equals("head")) return new Vec3(box.getCenter().x, box.maxY - 0.12, box.getCenter().z);
        if (mode.equals("body")) return new Vec3(box.getCenter().x, box.minY + box.getYsize() * 0.55, box.getCenter().z);
        if (nearestHitboxPoint.enabled()) return closestPointOnBox(client.player.getEyePosition(), box);
        return box.getCenter();
    }

    private Vec3 closestPointOnBox(Vec3 origin, AABB box) {
        return new Vec3(Mth.clamp(origin.x, box.minX, box.maxX), Mth.clamp(origin.y, box.minY, box.maxY), Mth.clamp(origin.z, box.minZ, box.maxZ));
    }

    private double[] anglesTo(Minecraft client, Vec3 point) {
        Vec3 eye = client.player.getEyePosition();
        double dx = point.x - eye.x, dy = point.y - eye.y, dz = point.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return new double[]{Math.toDegrees(Math.atan2(dz, dx)) - 90.0, -Math.toDegrees(Math.atan2(dy, horizontal))};
    }

    private void rotateTowards(Minecraft client, Vec3 point) {
        double[] desired = anglesTo(client, point);
        double yawDelta = Mth.wrapDegrees(desired[0] - client.player.getYRot());
        double pitchDelta = desired[1] - client.player.getXRot();
        if (randomize.enabled() && randomNoise.get() > 0) {
            yawDelta += (random.nextDouble() - 0.5) * randomNoise.get() * 2.0;
            pitchDelta += (random.nextDouble() - 0.5) * randomNoise.get() * 2.0;
        }
        if (instant.enabled()) {
            client.player.setYRot((float)(client.player.getYRot() + yawDelta));
            client.player.setXRot((float)Mth.clamp(client.player.getXRot() + pitchDelta, -90.0, 90.0));
            return;
        }
        double limit = Math.min(maxRotation.get(), speed.get());
        client.player.setYRot(client.player.getYRot() + (float)Mth.clamp(yawDelta, -limit, limit));
        client.player.setXRot((float)Mth.clamp(client.player.getXRot() + Mth.clamp(pitchDelta, -limit, limit), -90.0, 90.0));
    }

    private double pixelsFromYaw(Minecraft client, double degrees) {
        return degrees / Math.max(0.01, client.options.fov().get()) * Math.max(1, client.getWindow().getGuiScaledWidth());
    }

    private double pixelsFromPitch(Minecraft client, double degrees) {
        return degrees / Math.max(0.01, verticalFov(client)) * Math.max(1, client.getWindow().getGuiScaledHeight());
    }

    private double verticalFov(Minecraft client) {
        double horizontal = Math.toRadians(client.options.fov().get());
        double aspect = (double)client.getWindow().getGuiScaledWidth() / Math.max(1, client.getWindow().getGuiScaledHeight());
        return Math.toDegrees(2.0 * Math.atan(Math.tan(horizontal * 0.5) / Math.max(0.01, aspect)));
    }

    public LivingEntity target() { return target; }
    public boolean highlightTarget() { return highlightTarget.enabled(); }
    public int highlightColor() { return highlightColor.get(); }
    public boolean highlightFill() { return highlightFill.enabled(); }
    public boolean highlightOutline() { return highlightOutline.enabled(); }
    public double highlightFillAlpha() { return highlightFillAlpha.get(); }
    public double highlightOutlineAlpha() { return highlightOutlineAlpha.get(); }
    public double highlightLineWidth() { return highlightLineWidth.get(); }
    public double highlightExpand() { return highlightExpand.get(); }
}
