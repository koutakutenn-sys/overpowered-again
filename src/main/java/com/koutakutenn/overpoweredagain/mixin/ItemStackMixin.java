package com.koutakutenn.overpoweredagain.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void overpoweredAgain$awardCraftingAchievement(Player player, int amount, CallbackInfo ci) {
        if (amount <= 0
                || !((ItemStack) (Object) this).is(Items.ENCHANTED_GOLDEN_APPLE)
                || !(player instanceof ServerPlayer serverPlayer)) return;
        // ResultSlot calls this for both normal extraction and shift-click crafting. Match the
        // crafted item so another mod's identical recipe cannot bypass the achievement.
        var advancement = serverPlayer.level().getServer().getAdvancements().get(
                Identifier.fromNamespaceAndPath("overpowered_again", "overpowered"));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "craft_enchanted_golden_apple");
        }
    }
}
