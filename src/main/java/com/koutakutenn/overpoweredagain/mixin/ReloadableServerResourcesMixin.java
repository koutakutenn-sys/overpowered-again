package com.koutakutenn.overpoweredagain.mixin;

import com.koutakutenn.overpoweredagain.AdvancementRecipeAccess;
import java.util.List;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Shadow @Final private RecipeManager recipes;
    @Shadow @Final private ServerAdvancementManager advancements;

    @Inject(method = "listeners", at = @At("HEAD"))
    private void overpoweredAgain$bindRecipes(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        // Vanilla applies the recipe listener before the advancement listener. Bind the same
        // reload's managers rather than reading global state from parallel preparation tasks.
        ((AdvancementRecipeAccess) advancements).overpoweredAgain$setRecipeManager(recipes);
    }
}
