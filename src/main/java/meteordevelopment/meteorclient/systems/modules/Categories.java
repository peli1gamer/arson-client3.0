/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson registers the upstream Meteor categories without loading Meteor's addon system.
 */
package meteordevelopment.meteorclient.systems.modules;


public final class Categories {
    public static final Category Combat = new Category("Combat");
    public static final Category Player = new Category("Player");
    public static final Category Movement = new Category("Movement");
    public static final Category Render = new Category("Render");
    public static final Category World = new Category("World");
    public static final Category Misc = new Category("Misc");

    private Categories() {
    }
}
