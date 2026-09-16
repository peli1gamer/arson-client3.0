package io.arson.client.api;

import io.arson.client.ArsonClient;
import io.arson.client.module.Module;
import java.util.List;
import java.util.Optional;

/** Small stable public facade for addons that only need Arson module discovery/control. */
public final class ArsonApi {
    private ArsonApi() {}

    public static Optional<Module> module(String id) {
        if (ArsonClient.getInstance() == null || id == null || id.isBlank()) return Optional.empty();
        return Optional.ofNullable(ArsonClient.getInstance().modules().get(id));
    }

    public static List<Module> modules() {
        if (ArsonClient.getInstance() == null) return List.of();
        return List.copyOf(ArsonClient.getInstance().modules().all());
    }

    public static boolean setEnabled(String id, boolean enabled) {
        Optional<Module> module = module(id);
        if (module.isEmpty()) return false;
        module.get().setEnabled(enabled);
        return true;
    }

    public static boolean setFavorite(String id, boolean favorite) {
        Optional<Module> module = module(id);
        if (module.isEmpty()) return false;
        module.get().setFavorite(favorite);
        return true;
    }
}
