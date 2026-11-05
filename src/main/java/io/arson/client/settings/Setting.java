package io.arson.client.settings;

import net.minecraft.nbt.NbtCompound;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Arson metadata and behavior adapter over Meteor's typed setting API. */
public abstract class Setting<T> extends meteordevelopment.meteorclient.settings.Setting<T> {
    private final String id;
    private String arsonDescription = "";
    private BooleanSupplier visibility = () -> true;
    private SettingGroup group;
    private final List<Consumer<T>> changeListeners = new ArrayList<>();
    private final List<Consumer<Setting<T>>> activationListeners = new ArrayList<>();

    protected Setting(String id, String name, T defaultValue) {
        super(name, "", defaultValue, null, null);
        this.id = Objects.requireNonNull(id);
    }

    public final String id() { return id; }
    @Override public final String getId() { return id; }
    public final String name() { return name; }
    public T defaultValue() { return getDefaultValue(); }

    public String description() { return arsonDescription; }

    public Setting<T> description(String value) {
        arsonDescription = value == null ? "" : value;
        setDescription(arsonDescription);
        return this;
    }

    public Setting<T> onChanged(Consumer<T> listener) {
        changeListeners.add(Objects.requireNonNull(listener));
        return this;
    }

    public Setting<T> onModuleActivated(Consumer<Setting<T>> listener) {
        activationListeners.add(Objects.requireNonNull(listener));
        return this;
    }

    @Override public void onChanged() {
        super.onChanged();
        for (Consumer<T> listener : List.copyOf(changeListeners)) listener.accept(get());
    }

    @Override public void onActivated() {
        super.onActivated();
        for (Consumer<Setting<T>> listener : List.copyOf(activationListeners)) listener.accept(this);
    }

    @Override public boolean isVisible() { return visible(); }

    public Setting<T> visibleWhen(BooleanSupplier predicate) {
        Objects.requireNonNull(predicate);
        BooleanSupplier previous = visibility;
        visibility = () -> previous.getAsBoolean() && predicate.getAsBoolean();
        return this;
    }

    public Setting<T> dependsOn(BooleanSupplier predicate) { return visibleWhen(predicate); }

    public Setting<T> dependsOn(Setting<?> dependency, Predicate<Object> predicate) {
        Objects.requireNonNull(dependency);
        Objects.requireNonNull(predicate);
        return visibleWhen(() -> predicate.test(dependency.get()));
    }

    public Setting<T> dependsOnValue(Setting<?> dependency, Object expected) {
        return dependsOn(dependency, value -> Objects.equals(value, expected));
    }

    public Setting<T> dependsOn(BooleanSetting dependency) { return dependsOnValue(dependency, Boolean.TRUE); }
    public Setting<T> dependsOnNot(BooleanSetting dependency) { return dependsOnValue(dependency, Boolean.FALSE); }

    public boolean visible() { return visibility.getAsBoolean() && (group == null || group.visible()); }
    public SettingGroup group() { return group; }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S group(SettingGroup value) {
        group = Objects.requireNonNull(value);
        value.add(this);
        return (S) this;
    }

    @Override public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        tag.putString("id", id);
        return tag;
    }
}
