package com.koutakutenn.overpoweredagain.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.koutakutenn.overpoweredagain.OverpoweredEffects;

import net.fabricmc.loader.api.FabricLoader;

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
        Consumable vanilla = Consumables.ENCHANTED_GOLDEN_APPLE;
        List<MobEffectInstance> vanillaEffects = statusEffects(vanilla);
        NOTES.add("vanilla consumable status effects: " + describe(vanillaEffects));

        LivingEntity target = new Cow(EntityTypes.COW, level);
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

        // 4. The custom advancement has to be present in the datapack-backed advancement manager.
        check(server.getAdvancements().get(ADVANCEMENT_ID) != null,
                "advancement " + ADVANCEMENT_ID + " is loaded");
        NOTES.add("advancement " + ADVANCEMENT_ID + " gets its title and description from "
                + "advancements.overpowered_again.overpowered_again.title/.description");

        return report(FabricLoader.getInstance().getGameDir());
    }

    /** Flattens every "apply status effects" consume effect of a consumable. */
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
