package com.koutakutenn.overpoweredagain;

import net.minecraft.world.item.Items;
import com.koutakutenn.overpoweredagain.mixin.RecipeResultAccessor;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public final class RecipeAvailability {
    private RecipeAvailability() {}

    public static boolean hasEnchantedGoldenAppleRecipe(RecipeManager recipes, HolderLookup.Provider registries) {
        return recipes.getRecipes().stream()
                .anyMatch(holder -> producesEnchantedGoldenApple(holder.value(), registries));
    }

    private static boolean producesEnchantedGoldenApple(Recipe<?> recipe, HolderLookup.Provider registries) {
        // display() also builds ingredient displays, which can invoke other mods' remainder
        // logic and instantiate stacks before components are bound during new-world creation.
        if (recipe instanceof RecipeResultAccessor accessor) {
            var result = accessor.overpoweredAgain$getResult();
            return result.count() > 0 && result.item().value() == Items.ENCHANTED_GOLDEN_APPLE;
        }
        // Custom serializers may also expose a fixed output. Read the encoded result rather
        // than constructing a preview. A serializer that cannot encode is not a fixed output.
        try {
            var encoded = Recipe.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), recipe).result();
            if (encoded.isEmpty() || !encoded.get().isJsonObject()) return false;
            var result = encoded.get().getAsJsonObject().get("result");
            if (result == null) return false;
            if (result.isJsonPrimitive()) return "minecraft:enchanted_golden_apple".equals(result.getAsString());
            if (!result.isJsonObject()) return false;
            var object = result.getAsJsonObject();
            var id = object.has("id") ? object.get("id") : object.get("item");
            return id != null && id.isJsonPrimitive() && "minecraft:enchanted_golden_apple".equals(id.getAsString())
                    && (!object.has("count") || object.get("count").getAsInt() > 0);
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
