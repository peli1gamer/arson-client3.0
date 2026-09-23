package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.SettingGroup;
import java.util.Comparator;
import java.util.List;

/** Non-automating combat target and weapon information HUD. */
public final class CombatInfoModule extends Module {
    private final SettingGroup contentGroup = new SettingGroup("content", "Content", "Choose which local combat information is displayed.");
    private final SettingGroup layoutGroup = new SettingGroup("layout", "Layout", "Position, scale and spacing controls.");
    private final SettingGroup styleGroup = new SettingGroup("style", "Style", "Colors and backgrounds for the local display.");
    private final BooleanSetting showTarget = setting(new BooleanSetting("show-target", "Show Target", true)).group(contentGroup);
    private final BooleanSetting showPlayers = setting(new BooleanSetting("show-players", "Players", true)).group(contentGroup);
    private final BooleanSetting showMobs = setting(new BooleanSetting("show-mobs", "Mobs", true)).group(contentGroup);
    private final BooleanSetting showAnimals = setting(new BooleanSetting("show-animals", "Animals", true)).group(contentGroup);
    private final BooleanSetting showHeldItem = setting(new BooleanSetting("show-held-item", "Show Held Item", true)).group(contentGroup);
    private final BooleanSetting showTargetItem = setting(new BooleanSetting("show-target-item", "Show Target Item", true)).group(contentGroup);
    private final BooleanSetting showDurability = setting(new BooleanSetting("show-durability", "Show Durability", true)).group(contentGroup);
    private final BooleanSetting showTargetDurability = setting(new BooleanSetting("show-target-durability", "Show Target Durability", true)).group(contentGroup);
    private final BooleanSetting showAttackCooldown = setting(new BooleanSetting("show-attack-cooldown", "Show Attack Cooldown", true)).group(contentGroup);
    private final BooleanSetting showDistance = setting(new BooleanSetting("show-distance", "Show Distance", true)).group(contentGroup);
    private final BooleanSetting showHealth = setting(new BooleanSetting("show-health", "Show Health", true)).group(contentGroup);
    private final BooleanSetting showAbsorption = setting(new BooleanSetting("show-absorption", "Show Absorption", true)).group(contentGroup);
    private final BooleanSetting showArmor = setting(new BooleanSetting("show-armor", "Show Armor", true)).group(contentGroup);
    private final BooleanSetting showEffects = setting(new BooleanSetting("show-effects", "Show Status Effects", true)).group(contentGroup);
    private final DoubleSetting effectLimit = setting(new DoubleSetting("effect-limit", "Visible Effects", 4.0, 1.0, 8.0, 1.0)).group(contentGroup);
    private final BooleanSetting healthBar = setting(new BooleanSetting("health-bar", "Health Bar", true)).group(contentGroup);
    private final BooleanSetting healthBarBackground = setting(new BooleanSetting("health-bar-background", "Health Bar Background", true)).group(styleGroup);
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", true)).group(styleGroup);
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF)).group(styleGroup);
    private final ColorSetting healthColor = setting(new ColorSetting("health-color", "Health Color", 0xFFFF5555)).group(styleGroup);
    private final ColorSetting absorptionColor = setting(new ColorSetting("absorption-color", "Absorption Color", 0xFFFFAA00)).group(styleGroup);
    private final ColorSetting healthBarBackgroundColor = setting(new ColorSetting("health-bar-background-color", "Health Bar Background Color", 0x60202020)).group(styleGroup);
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0xA0101010)).group(styleGroup);
    private final DoubleSetting range = setting(new DoubleSetting("range", "Target Range", 16.0, 4.0, 64.0, 1.0)).group(contentGroup);
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 10.0, 0.0, 5000.0, 1.0)).group(layoutGroup);
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 10.0, 0.0, 5000.0, 1.0)).group(layoutGroup);
    private final DoubleSetting infoScale = setting(new DoubleSetting("info-scale", "Info Scale", 1.0, 0.5, 2.0, 0.1)).group(layoutGroup);
    private final DoubleSetting padding = setting(new DoubleSetting("padding", "Padding", 4.0, 0.0, 12.0, 1.0)).group(layoutGroup);
    private final DoubleSetting rowGap = setting(new DoubleSetting("row-gap", "Row Gap", 2.0, 0.0, 8.0, 1.0)).group(layoutGroup);
    private final DoubleSetting healthBarHeight = setting(new DoubleSetting("health-bar-height", "Health Bar Height", 3.0, 1.0, 8.0, 1.0)).group(layoutGroup);

    public CombatInfoModule() {
        super("combat-info", "Combat Info", Category.COMBAT, "Displays nearby entity, defensive stats, and local weapon status without changing combat input or attacking.");
        showTarget.description("Show the nearest eligible living entity in range.");
        showPlayers.description("Include player entities in the information display.");
        showMobs.description("Include hostile mobs in the information display.");
        showAnimals.description("Include passive animals in the information display.");
        showHeldItem.description("Display your own selected hotbar item.");
        showTargetItem.description("Display the selected entity’s main-hand item when available.");
        showTargetDurability.description("Display remaining durability for the selected entity’s held item when available.");
        showDurability.description("Display held-item durability when the item has durability.");
        showAttackCooldown.description("Display your local attack cooldown progress.");
        showDistance.description("Display distance from you to the selected entity.");
        showHealth.description("Display the selected entity’s current health.");
        showAbsorption.description("Display the selected entity’s temporary absorption health.");
        showArmor.description("Display the selected entity’s armor points.");
        showEffects.description("Display a sorted list of the selected entity’s active status effects and remaining time.");
        effectLimit.description("Maximum number of target effects to show before a compact remainder count.");
        healthBar.description("Show health as a compact bar in addition to the numeric value.");
        healthBarBackground.description("Draw a backing track behind the health bar.");
        background.description("Draw a panel behind the information rows.");
        range.description("Maximum distance used to find an entity for the display.");
        x.description("Horizontal screen position of the HUD panel.");
        y.description("Vertical screen position of the HUD panel.");
        infoScale.description("Scale the text and panel together.");
        padding.description("Space between the panel edge and its contents.");
        rowGap.description("Vertical spacing between information rows.");
        healthBarHeight.description("Thickness of the health indicator bar.");
        textColor.description("ARGB color used for informational text.");
        healthColor.description("ARGB color used for the health fill.");
        absorptionColor.description("ARGB color used for the absorption segment of the target health bar.");
        healthBarBackgroundColor.description("ARGB color used for the health bar track.");
        backgroundColor.description("ARGB color used for the panel background.");
    }

    public boolean showTarget() { return showTarget.enabled(); }
    public boolean showPlayers() { return showPlayers.enabled(); }
    public boolean showMobs() { return showMobs.enabled(); }
    public boolean showAnimals() { return showAnimals.enabled(); }
    public boolean showHeldItem() { return showHeldItem.enabled(); }
    public boolean showTargetItem() { return showTargetItem.enabled(); }
    public boolean showDurability() { return showDurability.enabled(); }
    public boolean showTargetDurability() { return showTargetDurability.enabled(); }
    public boolean showAttackCooldown() { return showAttackCooldown.enabled(); }
    public boolean showDistance() { return showDistance.enabled(); }
    public boolean showHealth() { return showHealth.enabled(); }
    public boolean showAbsorption() { return showAbsorption.enabled(); }
    public boolean showArmor() { return showArmor.enabled(); }
    public boolean showEffects() { return showEffects.enabled(); }
    public int effectLimit() { return Math.max(1, (int) Math.round(effectLimit.get())); }
    public boolean healthBar() { return healthBar.enabled(); }
    public boolean healthBarBackground() { return healthBarBackground.enabled(); }
    public boolean background() { return background.enabled(); }
    public int textColor() { return textColor.get(); }
    public int healthColor() { return healthColor.get(); }
    public int absorptionColor() { return absorptionColor.get(); }
    public int healthBarBackgroundColor() { return healthBarBackgroundColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }
    public double range() { return range.get(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public double infoScale() { return infoScale.get(); }
    public int padding() { return (int) Math.round(padding.get()); }
    public int rowGap() { return (int) Math.round(rowGap.get()); }
    public int healthBarHeight() { return (int) Math.round(healthBarHeight.get()); }

    public record HealthBarSegments(float health, float absorption) {}

    /** Fractions of a health bar reserved for health then absorption, both clamped to [0, 1]. */
    public static HealthBarSegments healthBarSegments(float health, float maxHealth, float absorption) {
        float maximum = Float.isFinite(maxHealth) && maxHealth > 0.0f ? maxHealth : 0.0f;
        if (maximum == 0.0f) return new HealthBarSegments(0.0f, 0.0f);
        float safeHealth = Float.isFinite(health) ? Math.max(0.0f, health) : 0.0f;
        float safeAbsorption = Float.isFinite(absorption) ? Math.max(0.0f, absorption) : 0.0f;
        float healthFraction = Math.min(1.0f, safeHealth / maximum);
        float combinedFraction = Math.min(1.0f, (safeHealth + safeAbsorption) / maximum);
        return new HealthBarSegments(healthFraction, Math.max(0.0f, combinedFraction - healthFraction));
    }

    public record EffectSnapshot(String name, int amplifier, int durationTicks) {}

    public static List<String> formatEffects(List<EffectSnapshot> effects, int limit) {
        if (effects == null || effects.isEmpty() || limit <= 0) return List.of();
        List<EffectSnapshot> sorted = effects.stream().filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(effect -> normalizedEffectName(effect.name()), String.CASE_INSENSITIVE_ORDER))
                .toList();
        int visible = Math.min(limit, sorted.size());
        java.util.ArrayList<String> rows = new java.util.ArrayList<>(visible + 1);
        for (int i = 0; i < visible; i++) {
            EffectSnapshot effect = sorted.get(i);
            rows.add(formatEffect(effect.name(), effect.amplifier(), effect.durationTicks()));
        }
        if (sorted.size() > visible) rows.add("+" + (sorted.size() - visible) + " more effects");
        return List.copyOf(rows);
    }

    public static String formatEffect(String name, int amplifier, int durationTicks) {
        int seconds = Math.max(0, durationTicks) / 20;
        String duration = durationTicks < 0 ? "∞"
                : String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
        return normalizedEffectName(name) + " " + (Math.max(0, amplifier) + 1) + " " + duration;
    }

    private static String normalizedEffectName(String name) {
        return name == null || name.isBlank() ? "Unknown" : name.trim();
    }

    public static String formatAbsorption(float value) {
        float finiteValue = Float.isFinite(value) ? Math.max(0.0f, value) : 0.0f;
        return "Absorption " + (Math.round(finiteValue * 10.0f) / 10.0f);
    }

    public static String formatArmor(int points) {
        return "Armor " + Math.max(0, points);
    }
}
