package io.arson.client.settings;

import java.util.Arrays;
import java.util.List;

public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final List<E> values;

    public EnumSetting(String id, String name, E defaultValue) {
        super(id, name, defaultValue);
        values = List.copyOf(Arrays.asList(defaultValue.getDeclaringClass().getEnumConstants()));
    }

    public List<E> values() { return values; }
    public void cycle(int direction) {
        int index = values.indexOf(get());
        if (index < 0) index = 0;
        int next = Math.floorMod(index + (direction == 0 ? 1 : direction), values.size());
        set(values.get(next));
    }
}
