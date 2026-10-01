package com.koutakutenn.overpoweredagain.mixin;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.koutakutenn.overpoweredagain.OverpoweredEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

/**
 * Replaces the status effects of an enchanted golden apple.
 *
 * <p>In 26.2 a consumable's effects live on the {@code minecraft:consumable} item component as a
 * list of {@link ConsumeEffect}s, and {@link Consumable#onConsume} applies them on the server
 * through {@code onConsumeEffects.forEach(...)}. Redirecting that one call site swaps the effect
 * list while leaving every other part of eating alone: particles, sounds, statistics, the
 * {@code consume_item} advancement trigger and the item consumption all stay vanilla.
 */
@Mixin(Consumable.class)
public abstract class ConsumableMixin {

    @Redirect(method = "onConsume", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void overpoweredAgain$replaceEnchantedGoldenAppleEffects(List<ConsumeEffect> effects,
            Consumer<ConsumeEffect> action, Level level, LivingEntity entity, ItemStack stack) {
        if (level.isClientSide() || !stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            effects.forEach(action);
            return;
        }

        OverpoweredEffects.enchantedGoldenAppleEffects().forEach(entity::addEffect);
    }
}
