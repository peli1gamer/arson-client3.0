package com.arson.client.render;

import java.util.EnumMap;
import java.util.Map;

/** Per-storage-type visual profile. Kept independent from world scanning/render hooks. */
public final class StorageRenderProfile {
    private final Map<StorageType, RenderColor> colors = new EnumMap<>(StorageType.class);
    private final Map<StorageType, Boolean> enabled = new EnumMap<>(StorageType.class);

    public StorageRenderProfile() {
        setDefaults();
    }

    private void setDefaults() {
        for (StorageType type : StorageType.values()) {
            enabled.put(type, true);
            colors.put(type, new RenderColor(255, 80, 80, 220));
        }
        colors.put(StorageType.CHEST, new RenderColor(255, 190, 70, 220));
        colors.put(StorageType.BARREL, new RenderColor(190, 130, 70, 220));
        colors.put(StorageType.SHULKER, new RenderColor(190, 90, 255, 220));
        colors.put(StorageType.ENDER_CHEST, new RenderColor(80, 220, 255, 220));
        colors.put(StorageType.OTHER, new RenderColor(255, 80, 80, 220));
    }

    public boolean enabled(StorageType type) {
        return Boolean.TRUE.equals(enabled.get(type));
    }

    public void enabled(StorageType type, boolean value) {
        enabled.put(type, value);
    }

    public RenderColor color(StorageType type) {
        return colors.get(type);
    }

    public void color(StorageType type, RenderColor color) {
        if (type != null && color != null) {
            colors.put(type, color);
        }
    }

    public Map<StorageType, RenderColor> colors() {
        return Map.copyOf(colors);
    }
}
