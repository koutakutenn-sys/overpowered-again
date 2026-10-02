package com.koutakutenn.overpoweredagain.test.mixin;

import com.koutakutenn.overpoweredagain.test.RecipeDisplayGuard;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class IngredientDisplayGuardMixin {
    @Inject(method = "display", at = @At("HEAD"))
    private void overpoweredAgain$rejectEarlyIngredientDisplay(CallbackInfoReturnable<SlotDisplay> cir) {
        if (RecipeDisplayGuard.DETECTING.get()) {
            throw new IllegalStateException("Recipe detection entered ingredient display before components were bound");
        }
    }
}
