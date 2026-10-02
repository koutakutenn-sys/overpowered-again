package com.koutakutenn.overpoweredagain;

import net.minecraft.world.item.crafting.RecipeManager;

/** Associates each advancement reload with its own recipe manager, including integrated worlds. */
public interface AdvancementRecipeAccess {
    void overpoweredAgain$setRecipeManager(RecipeManager recipes);
}
