package com.koutakutenn.overpoweredagain.test;

/**
 * Records that the enchanted golden apple consumption path really ran through {@code onConsume} on
 * the server. The test mixin sets this flag; the main mixin's redirect is what changes the effects.
 */
public final class CapturedVanillaEffects {

    private static boolean observed;

    private CapturedVanillaEffects() {
    }

    public static void observe() {
        observed = true;
    }

    /** @return true once {@code Consumable.onConsume} was seen on the server */
    public static boolean wasObserved() {
        return observed;
    }
}
