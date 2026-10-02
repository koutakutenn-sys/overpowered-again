package com.koutakutenn.overpoweredagain.test.mixin;

import com.koutakutenn.overpoweredagain.RecipeAvailability;
import com.koutakutenn.overpoweredagain.test.RecipeDisplayGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeAvailability.class)
public abstract class RecipeAvailabilityGuardMixin {
    @Inject(method = "hasEnchantedGoldenAppleRecipe", at = @At("HEAD"))
    private static void overpoweredAgain$beginDetection(CallbackInfoReturnable<Boolean> cir) {
        RecipeDisplayGuard.DETECTING.set(true);
        RecipeDisplayGuard.DETECTION_CALLS.incrementAndGet();
    }

    @Inject(method = "hasEnchantedGoldenAppleRecipe", at = @At("RETURN"))
    private static void overpoweredAgain$endDetection(CallbackInfoReturnable<Boolean> cir) {
        RecipeDisplayGuard.DETECTING.set(false);
    }
}
