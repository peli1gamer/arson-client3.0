package io.arson.client.settings;

import net.minecraft.nbt.NbtCompound;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Arson metadata and visibility adapter over Meteor's setting group. */
public final class SettingGroup extends meteordevelopment.meteorclient.settings.SettingGroup {
    private final String id;
    private final String arsonDescription;
    private final List<Setting<?>> arsonSettings = new ArrayList<>();
    private BooleanSupplier visibility = () -> true;

    public SettingGroup(String id, String name, String description) {
        super(name, true);
        this.id = Objects.requireNonNull(id);
        this.arsonDescription = description == null ? "" : description;
    }

    public String id() { return id; }
    @Override public String getId() { return id; }
    public String name() { return name; }
    public String description() { return arsonDescription; }

    public SettingGroup visibleWhen(BooleanSupplier predicate) {
        visibility = Objects.requireNonNull(predicate);
        return this;
    }

    public boolean visible() { return visibility.getAsBoolean(); }
    public List<Setting<?>> settings() { return List.copyOf(arsonSettings); }

    @Override public <T extends meteordevelopment.meteorclient.settings.Setting<?>> T add(T setting) {
        Objects.requireNonNull(setting, "setting");
        if (setting instanceof Setting<?> arsonSetting) {
            for (Setting<?> existing : arsonSettings) {
                if (existing == arsonSetting) return setting;
                if (existing.id().equals(arsonSetting.id())) {
                    throw new IllegalArgumentException("Duplicate setting ID in group " + id + ": " + arsonSetting.id());
                }
            }
            arsonSettings.add(arsonSetting);
        }
        return super.add(setting);
    }

    @Override public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        tag.putString("id", id);
        return tag;
    }
}
