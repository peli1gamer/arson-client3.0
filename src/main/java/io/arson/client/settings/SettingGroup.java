package io.arson.client.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Small metadata/grouping primitive for richer ClickGUI presentation without changing setting storage. */
public final class SettingGroup {
    private final String id;
    private final String name;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();
    private BooleanSupplier visibility = () -> true;

    public SettingGroup(String id, String name, String description) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = description == null ? "" : description;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public SettingGroup visibleWhen(BooleanSupplier predicate) { visibility = Objects.requireNonNull(predicate); return this; }
    public boolean visible() { return visibility.getAsBoolean(); }
    public List<Setting<?>> settings() { return List.copyOf(settings); }
    void add(Setting<?> setting) { if (!settings.contains(setting)) settings.add(setting); }
}
