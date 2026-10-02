package com.koutakutenn.overpoweredagain;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.ShapedRecipe;
import com.koutakutenn.overpoweredagain.mixin.RecipeResultAccessor;

public final class OverpoweredAgain implements ModInitializer {
    public static final OverpoweredConfig CONFIG = OverpoweredConfig.load();
    public static final Identifier RECIPE_ID =
            Identifier.fromNamespaceAndPath("overpowered_again", "enchanted_golden_apple");

    @Override
    public void onInitialize() {
        // Loading CONFIG before recipes are read also works on dedicated servers.
    }

    public static RecipeMap configuredRecipes(RecipeMap recipes) {
        if (CONFIG.legacyRecipe) {
            var own = recipes.values().stream()
                    .filter(recipe -> recipe.id().identifier().equals(RECIPE_ID))
                    .map(recipe -> recipe.value()).findFirst().orElse(null);
            if (!(own instanceof ShapedRecipe shaped)) return recipes;
            boolean duplicate = recipes.values().stream()
                    .filter(recipe -> !recipe.id().identifier().equals(RECIPE_ID))
                    .anyMatch(recipe -> recipe.value() instanceof ShapedRecipe other
                            && other.getWidth() == shaped.getWidth()
                            && other.getHeight() == shaped.getHeight()
                            && other.getIngredients().equals(shaped.getIngredients())
                            && ((RecipeResultAccessor) other).overpoweredAgain$getResult()
                                    .equals(((RecipeResultAccessor) shaped).overpoweredAgain$getResult()));
            if (!duplicate) return recipes;
        }
        // Keep the other provider's recipe; only suppress our own duplicate or disabled recipe.
        return RecipeMap.create(recipes.values().stream()
                .filter(recipe -> !recipe.id().identifier().equals(RECIPE_ID)).toList());
    }
}
