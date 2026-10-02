package com.koutakutenn.overpoweredagain.mixin;

import com.koutakutenn.overpoweredagain.OverpoweredAgain;
import com.koutakutenn.overpoweredagain.AdvancementRecipeAccess;
import com.koutakutenn.overpoweredagain.RecipeAvailability;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin implements AdvancementRecipeAccess {
    @Shadow @Final private HolderLookup.Provider registries;
    @Unique private RecipeManager overpoweredAgain$recipes;

    @Override
    public void overpoweredAgain$setRecipeManager(RecipeManager recipes) {
        overpoweredAgain$recipes = recipes;
    }
    @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"), argsOnly = true)
    private Map<Identifier, Advancement> overpoweredAgain$configureAdvancements(
            Map<Identifier, Advancement> original) {
        Map<Identifier, Advancement> result = new HashMap<>(original);
        if (overpoweredAgain$recipes == null) throw new IllegalStateException("Recipe manager was not bound");
        if (RecipeAvailability.hasEnchantedGoldenAppleRecipe(overpoweredAgain$recipes, registries)) {
            result.remove(Identifier.withDefaultNamespace("overpowered_again/overpowered_again"));
        } else {
            result.remove(Identifier.fromNamespaceAndPath("overpowered_again", "overpowered"));
        }
        if (overpoweredAgain$recipes.byKey(ResourceKey.create(Registries.RECIPE, OverpoweredAgain.RECIPE_ID)).isEmpty()) {
            result.remove(Identifier.fromNamespaceAndPath("overpowered_again", "recipes/enchanted_golden_apple"));
        }
        return result;
    }
}
