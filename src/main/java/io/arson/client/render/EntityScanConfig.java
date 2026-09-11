package io.arson.client.render;

/** Common discovery settings used by entity visualization modules. */
public interface EntityScanConfig {
    boolean enabled();
    boolean showPlayers();
    boolean showMobs();
    boolean showAnimals();
    boolean showItems();
    double range();
    int playerColor();
    int mobColor();
    int animalColor();
    int itemColor();
}
