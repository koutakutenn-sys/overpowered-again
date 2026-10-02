package com.koutakutenn.overpoweredagain.test;

import java.util.concurrent.atomic.AtomicInteger;

/** Prevents recipe detection from entering ingredient rendering, as in the MCPitanLib failure. */
public final class RecipeDisplayGuard {
    public static final ThreadLocal<Boolean> DETECTING = ThreadLocal.withInitial(() -> false);
    public static final AtomicInteger DETECTION_CALLS = new AtomicInteger();
    private RecipeDisplayGuard() {}
}
