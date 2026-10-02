package com.koutakutenn.overpoweredagain.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.koutakutenn.overpoweredagain.OverpoweredEffects;
import com.koutakutenn.overpoweredagain.OverpoweredAgain;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.RecipeCraftedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeMap;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.koutakutenn.overpoweredagain.RecipeAvailability;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.advancements.triggers.ConsumeItemTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Server-side integration checks for Overpowered Again.
 *
 * <p>These run inside a real dedicated server in the development environment, so they cover what a
 * compile cannot: that the mixin applies at runtime, that eating an enchanted golden apple grants
 * exactly the documented effects, that the vanilla values really were replaced, and that the custom
 * advancement datapack loads.
 *
 * <p>All expected numbers are written out here by hand instead of being read from
 * {@link OverpoweredEffects}, so a mistaken change to the mod cannot silently redefine "correct".
 */
public final class OverpoweredAgainIntegrationTests {

    private static final Identifier ADVANCEMENT_ID =
            Identifier.fromNamespaceAndPath("minecraft", "overpowered_again/overpowered_again");

    private static final List<String> FAILURES = new ArrayList<>();
    private static final List<String> NOTES = new ArrayList<>();
    private static int checks;

    private OverpoweredAgainIntegrationTests() {
    }

    public static boolean run(MinecraftServer server) {
        ServerLevel level = server.overworld();
        boolean externalRecipe = Boolean.getBoolean("overpowered_again.test.externalRecipe");
        boolean hasAppleRecipe = OverpoweredAgain.CONFIG.legacyRecipe || externalRecipe;
        check(RecipeAvailability.hasEnchantedGoldenAppleRecipe(server.getRecipeManager(), server.registryAccess())
                        == hasAppleRecipe, "recipe detection includes external recipes");
        check(RecipeDisplayGuard.DETECTION_CALLS.get() >= 2,
                "initial world resource loading and runtime detection both avoid ingredient display");
        check(Files.exists(com.koutakutenn.overpoweredagain.OverpoweredConfig.path()),
                "default or explicit server config exists on disk");
        Consumable vanilla = Consumables.ENCHANTED_GOLDEN_APPLE;
        List<MobEffectInstance> vanillaEffects = statusEffects(vanilla);
        NOTES.add("vanilla consumable status effects: " + describe(vanillaEffects));

        EffectTickingCow target = new EffectTickingCow(level);
        target.setPos(0.5, 0.0, 0.5);

        // Exactly what eating the apple does on the server: run the apple's consumable on the eater.
        ItemStack apple = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        ItemStack returned = vanilla.onConsume(level, target, apple);

        // 0. The consumption path really went through the method this mod hooks into, and the
        //    vanilla eating behaviour (item consumption, returned stack) survived the redirect.
        check(CapturedVanillaEffects.wasObserved(), "onConsume was reached on the server");
        check(returned == apple, "onConsume returned the same stack it was given");
        check(apple.isEmpty(), "eating consumed one apple (count is now " + apple.getCount() + ")");

        // 1. What the eater ends up with: the effects eating an enchanted golden apple now grants.
        List<MobEffectInstance> expected = List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 3),
                new MobEffectInstance(MobEffects.ABSORPTION, 2 * 60 * 20, 3),
                new MobEffectInstance(MobEffects.RESISTANCE, 5 * 60 * 20, 0),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 5 * 60 * 20, 0));
        checkEffectList("eaten effects", expected, List.copyOf(target.getActiveEffects()));

        // 2. What the mod itself would apply, so a bug in the mixin cannot hide behind the apple.
        checkEffectList("mod effect list", expected, OverpoweredEffects.enchantedGoldenAppleEffects());

        // 3. The vanilla values this mod replaces, read straight from the item component. The
        //    vanilla Regeneration (level II for 20s) must not be what the eater received.
        checkEffectList("vanilla effects", List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1),
                new MobEffectInstance(MobEffects.ABSORPTION, 2 * 60 * 20, 3),
                new MobEffectInstance(MobEffects.RESISTANCE, 5 * 60 * 20, 0),
                new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 5 * 60 * 20, 0)), vanillaEffects);

        // Regression: wait until regeneration and absorption expire while the two five-minute
        // effects still remain, then eat again. Use the actual entity effect tick/removal path.
        for (int tick = 0; tick < 2401; tick++) target.tickStatusEffects();
        check(!target.hasEffect(MobEffects.REGENERATION) && !target.hasEffect(MobEffects.ABSORPTION),
                "regeneration and absorption expired before the second apple");
        check(target.hasEffect(MobEffects.RESISTANCE) && target.hasEffect(MobEffects.FIRE_RESISTANCE),
                "resistance and fire resistance still active before the second apple");
        vanilla.onConsume(level, target, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        checkEffectList("second apple after short effects expired", expected, List.copyOf(target.getActiveEffects()));
        target.tickStatusEffects();
        check(target.hasEffect(MobEffects.REGENERATION) && target.hasEffect(MobEffects.ABSORPTION),
                "second apple regeneration and absorption survive the next tick");
        check(target.getAbsorptionAmount() == 16.0F, "second apple restores eight absorption hearts");

        EffectTickingCow other = new EffectTickingCow(level);
        other.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 7000, 0));
        other.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 7000, 0));
        vanilla.onConsume(level, other, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        check(duration(other, MobEffects.REGENERATION) == 600,
                "another eater with active resistance gets fresh regeneration");
        check(duration(other, MobEffects.ABSORPTION) == 2400,
                "another eater with active fire resistance gets fresh absorption");
        check(duration(other, MobEffects.RESISTANCE) == 7000
                        && duration(other, MobEffects.FIRE_RESISTANCE) == 7000,
                "apple does not shorten longer existing resistance effects");
        other.tickStatusEffects();
        check(duration(target, MobEffects.REGENERATION) == 599,
                "another eater's ticking does not shorten the first eater's regeneration");
        check(duration(target, MobEffects.ABSORPTION) == 2399,
                "another eater's ticking does not shorten the first eater's absorption");
        checkEffectList("fresh factory effects after elapsed game time", expected,
                OverpoweredEffects.enchantedGoldenAppleEffects());

        // 4. The custom advancement has to be present in the datapack-backed advancement manager.
        var advancement = server.getAdvancements().get(ADVANCEMENT_ID);
        check((advancement != null) == !hasAppleRecipe,
                "eating advancement is enabled only when no enchanted apple recipe exists");
        if (advancement != null) {
            var criterion = advancement.value().criteria().get("eat_enchanted_golden_apple");
            check(criterion != null && criterion.triggerInstance() instanceof ConsumeItemTrigger.TriggerInstance,
                    "advancement uses the consume_item trigger");
            if (criterion != null && criterion.triggerInstance() instanceof ConsumeItemTrigger.TriggerInstance consume) {
                check(consume.matches(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)),
                        "advancement predicate accepts enchanted golden apples");
                check(!consume.matches(new ItemStack(Items.GOLDEN_APPLE)),
                        "advancement predicate rejects regular golden apples");
                check(!consume.matches(new ItemStack(Items.APPLE)),
                        "advancement predicate rejects regular apples");
            }
        }
        // The same real resource reload must switch recipes and achievements together.
        var craftingId = Identifier.fromNamespaceAndPath("overpowered_again", "overpowered");
        var craftingAdvancement = server.getAdvancements().get(craftingId);
        check((craftingAdvancement != null) == hasAppleRecipe,
                "crafting advancement is enabled whenever an enchanted apple recipe exists");
        var recipeKey = ResourceKey.create(Registries.RECIPE, OverpoweredAgain.RECIPE_ID);
        if (craftingAdvancement != null) {
            check(craftingAdvancement.value().display().orElseThrow().getType() == AdvancementType.CHALLENGE,
                    "Overpowered uses the challenge frame");
            check(craftingAdvancement.value().parent().isEmpty(), "crafting advancement has no disabled parent");
            var criterion = craftingAdvancement.value().criteria().get("craft_enchanted_golden_apple");
            check(criterion != null && criterion.triggerInstance() instanceof RecipeCraftedTrigger.TriggerInstance,
                    "Overpowered uses recipe_crafted rather than consumption or inventory triggers");
            if (criterion != null && criterion.triggerInstance() instanceof RecipeCraftedTrigger.TriggerInstance crafted) {
                check(crafted.recipeId().equals(recipeKey), "crafting trigger targets the enchanted apple recipe");
            }
        }
        var unlock = server.getAdvancements().get(Identifier.fromNamespaceAndPath(
                "overpowered_again", "recipes/enchanted_golden_apple"));
        boolean ownRecipe = OverpoweredAgain.CONFIG.legacyRecipe && !externalRecipe;
        check((unlock != null) == ownRecipe, "recipe book unlock is absent when our duplicate recipe is suppressed");
        var recipe = server.getRecipeManager().byKey(recipeKey);
        check(recipe.isPresent() == ownRecipe, "our recipe is loaded only when enabled and no duplicate exists");
        List<ItemStack> ingredients = new ArrayList<>();
        for (int i = 0; i < 9; i++) ingredients.add(new ItemStack(i == 4 ? Items.APPLE : Items.GOLD_BLOCK));
        var input = CraftingInput.of(3, 3, ingredients);
        var matched = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        long matchingRecipeCount = server.getRecipeManager().getRecipes().stream()
                .filter(holder -> holder.value() instanceof CraftingRecipe crafting && crafting.matches(input, level)).count();
        check(matchingRecipeCount == (hasAppleRecipe ? 1 : 0), "legacy apple grid has exactly one recipe, never duplicates");
        var externalKey = ResourceKey.create(Registries.RECIPE,
                Identifier.fromNamespaceAndPath("craftable_enchanted_golden_apple", "enchanted_golden_apple"));
        check(server.getRecipeManager().byKey(externalKey).isPresent() == externalRecipe,
                "other provider's recipe is preserved");
        check(matched.isPresent() == hasAppleRecipe, "eight gold blocks around an apple match if either mod provides the recipe");
        if (matched.isPresent()) {
            ItemStack result = matched.get().value().assemble(input);
            check(result.is(Items.ENCHANTED_GOLDEN_APPLE) && result.getCount() == 1,
                    "recipe produces exactly one enchanted golden apple");
            boolean before = OverpoweredAgain.CONFIG.legacyRecipe;
            try {
                OverpoweredAgain.CONFIG.legacyRecipe = false;
                var filtered = OverpoweredAgain.configuredRecipes(RecipeMap.create(server.getRecipeManager().getRecipes()));
                check(filtered.byKey(recipeKey) == null, "disabled recipe is removed from the recipe map");
                check(filtered.values().size() == server.getRecipeManager().getRecipes().size()
                                - (ownRecipe ? 1 : 0),
                        "disabling preserves all other recipes");
            } finally {
                OverpoweredAgain.CONFIG.legacyRecipe = before;
            }
        }
        ingredients.set(0, new ItemStack(Items.GOLD_INGOT));
        check(server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, ingredients), level).isEmpty(), "mixed gold blocks and ingots do not match");
        for (int i = 0; i < 9; i++) ingredients.set(i, new ItemStack(i == 4 ? Items.APPLE : Items.GOLD_INGOT));
        var regularInput = CraftingInput.of(3, 3, ingredients);
        var regular = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, regularInput, level);
        check(regular.isPresent() && regular.get().value().assemble(regularInput).is(Items.GOLDEN_APPLE),
                "ordinary golden apple crafting is preserved");
        checkPlayerAchievements(server, hasAppleRecipe, externalRecipe);
        var matching = server.getAdvancements().getAllAdvancements().stream()
                .filter(holder -> holder.id().toString().contains("overpowered"))
                .map(holder -> holder.id().toString())
                .toList();
        NOTES.add("advancements whose id contains 'overpowered': " + matching);
        NOTES.add("sample vanilla advancement ids: " + server.getAdvancements().getAllAdvancements().stream()
                .limit(3).map(holder -> holder.id().toString()).toList());

        // Diagnostics: which data packs the server discovered and enabled at all.
        var packs = server.getPackRepository();
        NOTES.add("available data packs: " + packs.getAvailableIds());
        NOTES.add("selected data packs: " + packs.getSelectedIds());
        NOTES.add("available pack titles: " + packs.getAvailablePacks().stream()
                .map(pack -> pack.getId() + "=" + pack.getTitle().getString()).toList());

        // Control: the same JSON shipped by the test mod under its own namespace. If this one also
        // fails to load, the problem is the mod data pack mechanism, not the advancement contents.
        boolean probe = server.getAdvancements()
                .get(Identifier.fromNamespaceAndPath("overpowered_again_test", "probe")) != null;
        NOTES.add("test-mod probe advancement loaded: " + probe);
        check(probe, "test-mod probe advancement is loaded");

        NOTES.add("advancement " + ADVANCEMENT_ID + " gets its title and description from "
                + "advancements.overpowered_again.overpowered_again.title/.description");

        return report(FabricLoader.getInstance().getGameDir());
    }

    private static int duration(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        var active = entity.getEffect(effect);
        return active == null ? -1 : active.getDuration();
    }

    private static void checkPlayerAchievements(MinecraftServer server, boolean hasAppleRecipe, boolean externalRecipe) {
        var profile = new GameProfile(UUID.randomUUID(), "AppleTest");
        var player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        player.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND),
                player, CommonListenerCookie.createInitial(profile, false));
        var eating = server.getAdvancements().get(ADVANCEMENT_ID);
        var crafting = server.getAdvancements().get(Identifier.fromNamespaceAndPath("overpowered_again", "overpowered"));
        if (!hasAppleRecipe) {
            check(!player.getAdvancements().getOrStartProgress(eating).isDone(), "eating advancement starts incomplete");
            Consumables.ENCHANTED_GOLDEN_APPLE.onConsume(server.overworld(), player,
                    new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
            check(player.getAdvancements().getOrStartProgress(eating).isDone(),
                    "actually eating an enchanted apple awards Overpowered Again when no recipe exists");
            return;
        }
        check(!player.getAdvancements().getOrStartProgress(crafting).isDone(), "crafting advancement starts incomplete");
        player.getInventory().add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        check(!player.getAdvancements().getOrStartProgress(crafting).isDone(), "receiving an apple does not award the crafting achievement");
        Consumables.ENCHANTED_GOLDEN_APPLE.onConsume(server.overworld(), player,
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
        new ItemStack(Items.GOLDEN_APPLE).onCraftedBy(player, 1);
        check(!player.getAdvancements().getOrStartProgress(crafting).isDone(),
                "eating an enchanted apple or crafting an ordinary golden apple does not award Overpowered");
        var recipeId = externalRecipe ? Identifier.fromNamespaceAndPath("craftable_enchanted_golden_apple", "enchanted_golden_apple")
                : OverpoweredAgain.RECIPE_ID;
        var holder = server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, recipeId)).orElseThrow();
        check(holder.value() instanceof CraftingRecipe, "selected compatibility fixture is a crafting recipe");
        @SuppressWarnings("unchecked")
        var recipe = (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) holder;
        var menu = new CraftingMenu(1, player.getInventory());
        player.containerMenu = menu;
        fillAppleRecipe(menu, server.overworld(), recipe, 1);
        check(!player.getAdvancements().getOrStartProgress(crafting).isDone(), "recipe preview does not award the crafting achievement");
        var output = menu.getResultSlot();
        var taken = output.remove(1);
        output.onTake(player, taken);
        check(taken.is(Items.ENCHANTED_GOLDEN_APPLE), "normal result extraction produces an enchanted apple");
        check(player.getAdvancements().getOrStartProgress(crafting).isDone(),
                "normal crafting awards Overpowered for " + recipeId);
        player.getAdvancements().revoke(crafting, "craft_enchanted_golden_apple");
        fillAppleRecipe(menu, server.overworld(), recipe, 3);
        menu.quickMoveStack(player, 0);
        check(player.getAdvancements().getOrStartProgress(crafting).isDone(),
                "shift-click crafting awards Overpowered for " + recipeId);
        check(menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().getCount() == 2),
                "shift-click consumes one item from each ingredient stack");
        check(eating == null, "eating advancement remains disabled while any apple recipe exists");
    }

    private static void fillAppleRecipe(CraftingMenu menu, ServerLevel level,
            RecipeHolder<CraftingRecipe> recipe, int count) {
        menu.beginPlacingRecipe();
        for (int i = 0; i < 9; i++) {
            menu.getInputGridSlots().get(i).set(new ItemStack(i == 4 ? Items.APPLE : Items.GOLD_BLOCK, count));
        }
        // Force the actual external recipe in this test, regardless of duplicate recipe order.
        menu.finishPlacingRecipe(level, recipe);
    }

    /** Flattens every "apply status effects" consume effect of a consumable. */
    private static final class EffectTickingCow extends Cow {
        EffectTickingCow(ServerLevel level) {
            super(EntityTypes.COW, level);
        }

        void tickStatusEffects() {
            tickEffects();
        }
    }

    private static List<MobEffectInstance> statusEffects(Consumable consumable) {
        List<MobEffectInstance> result = new ArrayList<>();
        for (var effect : consumable.onConsumeEffects()) {
            if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
                result.addAll(apply.effects());
            }
        }
        return List.copyOf(result);
    }

    /** Compares two effect lists by effect, amplifier and duration, ignoring order. */
    private static void checkEffectList(String label, List<MobEffectInstance> expected,
            List<MobEffectInstance> actual) {
        check(expected.size() == actual.size(),
                label + ": expected " + expected.size() + " effects, got " + actual.size() + " " + describe(actual));
        for (MobEffectInstance wanted : expected) {
            MobEffectInstance found = actual.stream().filter(candidate -> candidate.is(wanted.getEffect())).findFirst()
                    .orElse(null);
            if (found == null) {
                fail(label + ": missing " + nameOf(wanted));
                continue;
            }
            check(found.getAmplifier() == wanted.getAmplifier(),
                    label + ": " + nameOf(wanted) + " amplifier should be " + wanted.getAmplifier() + ", got "
                            + found.getAmplifier());
            check(found.getDuration() == wanted.getDuration(),
                    label + ": " + nameOf(wanted) + " duration should be " + wanted.getDuration() + " ticks, got "
                            + found.getDuration());
        }
    }

    private static String nameOf(MobEffectInstance effect) {
        return effect.getEffect().unwrapKey().map(key -> key.identifier().toString()).orElse("?");
    }

    private static String describe(List<MobEffectInstance> effects) {
        StringBuilder builder = new StringBuilder("[");
        for (MobEffectInstance effect : effects) {
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append(nameOf(effect)).append(" level ").append(effect.getAmplifier() + 1)
                    .append(" for ").append(effect.getDuration()).append("t");
        }
        return builder.append(']').toString();
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) {
            FAILURES.add(description);
        }
    }

    private static void fail(String description) {
        FAILURES.add(description);
    }

    private static boolean report(Path gameDir) {
        String status = FAILURES.isEmpty() ? "PASS" : "FAIL";
        StringBuilder report = new StringBuilder();
        report.append(status).append('\n');
        report.append("checks: ").append(checks).append('\n');
        for (String note : NOTES) {
            report.append("note: ").append(note).append('\n');
        }
        for (String failure : FAILURES) {
            report.append("failure: ").append(failure).append('\n');
        }
        try {
            Files.writeString(gameDir.resolve("test-result.txt"), report.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("[overpowered-again-test] " + status + " (" + checks + " checks)");
        for (String note : NOTES) {
            System.out.println("[overpowered-again-test] note: " + note);
        }
        for (String failure : FAILURES) {
            System.out.println("[overpowered-again-test] FAILURE: " + failure);
        }
        return FAILURES.isEmpty();
    }
}
