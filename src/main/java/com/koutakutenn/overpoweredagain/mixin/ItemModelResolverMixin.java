package com.koutakutenn.overpoweredagain.mixin;

import com.koutakutenn.overpoweredagain.OverpoweredAgain;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelResolverMixin {
    private static final Identifier VANILLA = Identifier.withDefaultNamespace("enchanted_golden_apple");
    private static final Identifier LEGACY =
            Identifier.fromNamespaceAndPath("overpowered_again", "enchanted_golden_apple_legacy");

    @Redirect(method = "appendItemLayers", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object overpoweredAgain$legacyModel(ItemStack stack, DataComponentType<?> type) {
        Object model = stack.get(type);
        // Respect explicit custom item models, and leave ordinary golden apples alone.
        return OverpoweredAgain.CONFIG.legacyTexture && stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                && VANILLA.equals(model) ? LEGACY : model;
    }
}
