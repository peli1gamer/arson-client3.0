package io.arson.client.module;

import io.arson.client.settings.DoubleSetting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Lists modules with assigned keybinds for the World Info HUD. */
public final class HotkeysModule extends Module {
    private final DoubleSetting maxRows = setting(new DoubleSetting(
            "max-rows", "Maximum Rows", 12.0, 1.0, 32.0, 1.0));

    public HotkeysModule() {
        super("hotkeys", "Hotkeys", Category.RENDER,
                "Lists modules with assigned keybinds and shows their active state in the World Info HUD.");
        maxRows.description("Maximum number of bound modules to display. Additional bindings are counted in a final row.");
    }

    public int maxRows() {
        return Math.max(1, (int) Math.round(maxRows.get()));
    }

    public List<Module> boundModules(Collection<Module> modules) {
        if (modules == null || modules.isEmpty()) return List.of();
        ArrayList<Module> bound = new ArrayList<>();
        for (Module module : modules) {
            if (module != null && module != this && module.hasKeybind()) bound.add(module);
        }
        bound.sort(Comparator.comparing(Module::name, String.CASE_INSENSITIVE_ORDER));
        if (bound.size() > maxRows()) bound.subList(maxRows(), bound.size()).clear();
        return List.copyOf(bound);
    }

    public int boundModuleCount(Collection<Module> modules) {
        if (modules == null || modules.isEmpty()) return 0;
        int count = 0;
        for (Module module : modules) {
            if (module != null && module != this && module.hasKeybind()) count++;
        }
        return count;
    }
}
