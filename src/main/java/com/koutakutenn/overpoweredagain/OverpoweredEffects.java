package com.koutakutenn.overpoweredagain;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * The status effects this mod applies in place of the vanilla enchanted golden apple effects.
 *
 * <p>Effects are expressed in the same way vanilla expresses them: "amplifier" is the
 * zero-based level (amplifier 3 == level IV) and "duration" is in ticks (20 ticks = 1 second).
 *
 * <table>
 *   <caption>Target effects</caption>
 *   <tr><th>Effect</th><th>Level</th><th>Duration</th><th>Amplifier</th><th>Ticks</th></tr>
 *   <tr><td>Regeneration</td><td>IV</td><td>30s</td><td>3</td><td>600</td></tr>
 *   <tr><td>Absorption</td><td>IV</td><td>2m</td><td>3</td><td>2400</td></tr>
 *   <tr><td>Resistance</td><td>I</td><td>5m</td><td>0</td><td>6000</td></tr>
 *   <tr><td>Fire Resistance</td><td>I</td><td>5m</td><td>0</td><td>6000</td></tr>
 * </table>
 *
 * <p>Vanilla 26.2 already grants Absorption IV for 2m, Resistance I for 5m and Fire Resistance I
 * for 5m; only Regeneration changes (II for 20s -> IV for 30s). The full list is stated explicitly
 * anyway so the mod's behaviour does not silently drift if vanilla numbers change.
 *
 * <p>This class is initialised on first use, so the {@link MobEffects} registry holders it reads
 * are resolved when the player actually eats the apple, never during class loading.
 */
public final class OverpoweredEffects {

    private OverpoweredEffects() {
    }

    /** 30 seconds. */
    private static final int THIRTY_SECONDS = 30 * 20;
    /** 2 minutes. */
    private static final int TWO_MINUTES = 2 * 60 * 20;
    /** 5 minutes. */
    private static final int FIVE_MINUTES = 5 * 60 * 20;

    /** Zero-based amplifier for level IV. */
    private static final int AMPLIFIER_IV = 3;
    /** Zero-based amplifier for level I. */
    private static final int AMPLIFIER_I = 0;

    /** The effects applied by an enchanted golden apple while this mod is installed. */
    private static final List<MobEffectInstance> ENCHANTED_GOLDEN_APPLE_EFFECTS = List.of(
            effect(MobEffects.REGENERATION, THIRTY_SECONDS, AMPLIFIER_IV),
            effect(MobEffects.ABSORPTION, TWO_MINUTES, AMPLIFIER_IV),
            effect(MobEffects.RESISTANCE, FIVE_MINUTES, AMPLIFIER_I),
            effect(MobEffects.FIRE_RESISTANCE, FIVE_MINUTES, AMPLIFIER_I));

    /**
     * @param effect    the effect to grant
     * @param duration  duration in ticks
     * @param amplifier zero-based effect level
     */
    private static MobEffectInstance effect(Holder<MobEffect> effect, int duration, int amplifier) {
        // Same shape as vanilla's own consumable effects: not ambient, particles visible.
        return new MobEffectInstance(effect, duration, amplifier);
    }

    /**
     * @return the immutable list of effects to apply, in the order they should be applied
     */
    public static List<MobEffectInstance> enchantedGoldenAppleEffects() {
        return ENCHANTED_GOLDEN_APPLE_EFFECTS;
    }
}
