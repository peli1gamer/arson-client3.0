/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 *
 * Arson registers the upstream Meteor categories without loading Meteor's addon system.
 */
package meteordevelopment.meteorclient.systems.modules;

import net.minecraft.item.Items;

public final class Categories {
    public static final Category Combat = new Category("Combat", Items.GOLDEN_SWORD.getDefaultStack());
    public static final Category Player = new Category("Player", Items.ARMOR_STAND.getDefaultStack());
    public static final Category Movement = new Category("Movement", Items.DIAMOND_BOOTS.getDefaultStack());
    public static final Category Render = new Category("Render", Items.GLASS.getDefaultStack());
    public static final Category World = new Category("World", Items.GRASS_BLOCK.getDefaultStack());
    public static final Category Misc = new Category("Misc", Items.LAVA_BUCKET.getDefaultStack());

    private Categories() {
    }
}
