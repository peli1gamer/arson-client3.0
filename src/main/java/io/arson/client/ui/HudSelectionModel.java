package io.arson.client.ui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Pure selection state for the HUD editor; keeps multi-selection deterministic and testable. */
public final class HudSelectionModel {
    private final Set<String> selected = new LinkedHashSet<>();
    public HudSelectionModel(String initial) { if (initial != null && !initial.isBlank()) selected.add(initial); }
    public void select(String element, boolean additive) {
        if (element == null || element.isBlank()) return;
        if (!additive) selected.clear();
        if (!selected.add(element) && additive) selected.remove(element);
    }
    public boolean contains(String element) { return selected.contains(element); }
    public Set<String> elements() { return Set.copyOf(selected); }
    public int size() { return selected.size(); }
    public void clear() { selected.clear(); }
    public void replace(Collection<String> elements) { selected.clear(); if (elements != null) for (String e : elements) if (e != null && !e.isBlank()) selected.add(e); }
}
