package com.koutakutenn.overpoweredagain.test.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.koutakutenn.overpoweredagain.test.CapturedVanillaEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

/**
 * Notices that the consumption path reached {@code Consumable.onConsume} on the server.
 *
 * <p>Injected at {@code HEAD} with a low priority (Mixin runs higher priorities first) so it is
 * entered even though the main mixin redirects the effect list later in the same method.
 */
@Mixin(value = Consumable.class, priority = 500)
public abstract class CaptureVanillaEffectsMixin {

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void overpoweredAgain$observe(Level level, LivingEntity entity, ItemStack stack,
            CallbackInfoReturnable<ItemStack> callbackInfo) {
        if (!level.isClientSide()) {
            CapturedVanillaEffects.observe();
        }
    }
}
