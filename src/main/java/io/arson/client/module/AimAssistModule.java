package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.StringSetting;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

    @Override protected void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.currentScreen != null || !isActivated(client)) { target = null; return; }
        LivingEntity selected = chooseTarget(client);
        if (selected == null) { target = null; return; }
        target = selected;
        rotateTowards(client, aimPoint(client, target));
    }

    private boolean isActivated(MinecraftClient client) {
        String mode = activation.get().trim().toLowerCase();
        return switch (mode) {
            case "hold right click", "right click" -> client.options.useKey.isPressed();
            case "hold left click", "left click" -> client.options.attackKey.isPressed();
            case "hold key", "key" -> keyCode() > 0 && InputUtil.isKeyPressed(client.getWindow(), keyCode());
            default -> true;
        };
    }

    private LivingEntity chooseTarget(MinecraftClient client) {
        Box search = client.player.getBoundingBox().expand(range.get());
        LivingEntity locked = target;
        boolean keepLocked = sticky.enabled() && locked != null && isValid(client, locked) && inCrosshairGate(client, locked);
        LivingEntity best = client.world.getEntitiesByClass(LivingEntity.class, search, entity -> isValid(client, entity) && inCrosshairGate(client, entity))
                .stream().min(Comparator.comparingDouble((LivingEntity entity) -> crosshairDistance(client, entity))
                .thenComparingDouble(entity -> client.player.squaredDistanceTo(entity))).orElse(null);
        if (!keepLocked || locked == best || best == null) return best;
        double lockedScore = crosshairDistance(client, locked);
        double bestScore = crosshairDistance(client, best);
        return lockedScore <= bestScore * (1.0 + targetLock.get()) ? locked : best;
    }

    private boolean isValid(MinecraftClient client, LivingEntity entity) {
        if (entity == null || entity == client.player || !entity.isAlive() || entity.isRemoved()) return false;
        if (client.player.squaredDistanceTo(entity) > range.get() * range.get()) return false;
        if (visibleOnly.enabled() && !client.player.canSee(entity)) return false;
        if (entity instanceof PlayerEntity player) {
            FriendsModule friends = friendsModule();
            if (friends != null && friends.isFriend(player)) return false;
            return players.enabled();
        }
        if (entity instanceof HostileEntity) return monsters.enabled();
        if (entity instanceof AnimalEntity) return animals.enabled();
        return false;
    }

    private FriendsModule friendsModule() {
        if (io.arson.client.ArsonClient.getInstance() == null) return null;
        Module module = io.arson.client.ArsonClient.getInstance().modules().get("friends");
        return module instanceof FriendsModule friends ? friends : null;
    }

    private boolean inCrosshairGate(MinecraftClient client, LivingEntity entity) {
        Vec3d point = aimPoint(client, entity);
        double[] angles = anglesTo(client, point);
        double yawDelta = MathHelper.wrapDegrees(angles[0] - client.player.getYaw());
        double pitchDelta = angles[1] - client.player.getPitch();
        if (Math.hypot(yawDelta, pitchDelta) > fov.get() * 0.5) return false;
        double yawPixels = pixelsFromYaw(client, Math.abs(yawDelta));
        double pitchPixels = pixelsFromPitch(client, Math.abs(angles[1] - client.player.getPitch()));
        double halfW = crosshairWidth.get() * 0.5;
        double halfH = crosshairHeight.get() * 0.5;
        return yawPixels <= halfW && pitchPixels <= halfH;
    }

    private double crosshairDistance(MinecraftClient client, LivingEntity entity) {
        Vec3d point = aimPoint(client, entity);
        double[] angles = anglesTo(client, point);
        double yaw = MathHelper.wrapDegrees(angles[0] - client.player.getYaw());
        double pitch = angles[1] - client.player.getPitch();
        return Math.hypot(pixelsFromYaw(client, Math.abs(yaw)), pixelsFromPitch(client, Math.abs(pitch)));
    }

    private Vec3d aimPoint(MinecraftClient client, LivingEntity entity) {
        Box box = entity.getBoundingBox();
        String mode = aimPoint.get().trim().toLowerCase();
        if (mode.equals("head")) return new Vec3d(box.getCenter().x, box.maxY - 0.12, box.getCenter().z);
        if (mode.equals("body")) return new Vec3d(box.getCenter().x, box.minY + box.getLengthY() * 0.55, box.getCenter().z);
        if (nearestHitboxPoint.enabled()) return closestPointOnBox(client.player.getEyePos(), box);
        return box.getCenter();
    }

    private Vec3d closestPointOnBox(Vec3d origin, Box box) {
        return new Vec3d(MathHelper.clamp(origin.x, box.minX, box.maxX), MathHelper.clamp(origin.y, box.minY, box.maxY), MathHelper.clamp(origin.z, box.minZ, box.maxZ));
    }

    private double[] anglesTo(MinecraftClient client, Vec3d point) {
        Vec3d eye = client.player.getEyePos();
        double dx = point.x - eye.x, dy = point.y - eye.y, dz = point.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return new double[]{Math.toDegrees(Math.atan2(dz, dx)) - 90.0, -Math.toDegrees(Math.atan2(dy, horizontal))};
    }

    private void rotateTowards(MinecraftClient client, Vec3d point) {
        double[] desired = anglesTo(client, point);
        double yawDelta = MathHelper.wrapDegrees(desired[0] - client.player.getYaw());
        double pitchDelta = desired[1] - client.player.getPitch();
        if (randomize.enabled() && randomNoise.get() > 0) {
            yawDelta += (random.nextDouble() - 0.5) * randomNoise.get() * 2.0;
            pitchDelta += (random.nextDouble() - 0.5) * randomNoise.get() * 2.0;
        }
        if (instant.enabled()) {
            client.player.setYaw((float)(client.player.getYaw() + yawDelta));
            client.player.setPitch((float)MathHelper.clamp(client.player.getPitch() + pitchDelta, -90.0, 90.0));
            return;
        }
        double limit = Math.min(maxRotation.get(), speed.get());
        client.player.setYaw(client.player.getYaw() + (float)MathHelper.clamp(yawDelta, -limit, limit));
        client.player.setPitch((float)MathHelper.clamp(client.player.getPitch() + MathHelper.clamp(pitchDelta, -limit, limit), -90.0, 90.0));
    }

    private double pixelsFromYaw(MinecraftClient client, double degrees) {
        return degrees / Math.max(0.01, client.options.getFov().getValue()) * Math.max(1, client.getWindow().getScaledWidth());
    }

    private double pixelsFromPitch(MinecraftClient client, double degrees) {
        return degrees / Math.max(0.01, verticalFov(client)) * Math.max(1, client.getWindow().getScaledHeight());
    }

    private double verticalFov(MinecraftClient client) {
        double horizontal = Math.toRadians(client.options.getFov().getValue());
        double aspect = (double)client.getWindow().getScaledWidth() / Math.max(1, client.getWindow().getScaledHeight());
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
