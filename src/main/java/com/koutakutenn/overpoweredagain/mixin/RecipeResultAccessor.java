package com.koutakutenn.overpoweredagain.mixin;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads fixed recipe outputs without creating stacks or rendering ingredient displays. */
@Mixin({ShapedRecipe.class, ShapelessRecipe.class, SingleItemRecipe.class, SmithingTransformRecipe.class})
public interface RecipeResultAccessor {
    @Accessor("result")
    ItemStackTemplate overpoweredAgain$getResult();
}
