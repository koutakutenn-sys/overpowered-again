package com.koutakutenn.overpoweredagain.test.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Test-only copy of the other mod's recipe, enabled for the compatibility scenario. */
@Mixin(value = RecipeManager.class, priority = 500)
public abstract class ExternalRecipeFixtureMixin {
    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
            at = @At("RETURN"), cancellable = true)
    private void overpoweredAgain$externalFixture(CallbackInfoReturnable<RecipeMap> cir) {
        if (!Boolean.getBoolean("overpowered_again.test.externalRecipe")) {
            var id = Identifier.fromNamespaceAndPath("craftable_enchanted_golden_apple", "enchanted_golden_apple");
            cir.setReturnValue(RecipeMap.create(cir.getReturnValue().values().stream()
                    .filter(recipe -> !recipe.id().identifier().equals(id)).toList()));
        }
    }
}
