package com.koutakutenn.overpoweredagain.test.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koutakutenn.overpoweredagain.test.OverpoweredAgainIntegrationTests;

import net.minecraft.server.MinecraftServer;

/** Runs the integration checks once, on the first server tick, then shuts the server down. */
@Mixin(MinecraftServer.class)
public abstract class TestServerMixin {

    @Unique
    private boolean overpoweredAgain$ran;

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void overpoweredAgain$runTests(BooleanSupplier haveTime, CallbackInfo callbackInfo) {
        if (overpoweredAgain$ran) {
            return;
        }
        overpoweredAgain$ran = true;
        MinecraftServer server = (MinecraftServer) (Object) this;
        OverpoweredAgainIntegrationTests.run(server);
        // Return to the server loop before shutdown. System.exit on this thread deadlocks with
        // the JVM shutdown hook waiting for that same server thread to finish saving the world.
        System.out.println("[overpowered-again-test] requesting normal server shutdown");
        server.halt(false);
    }
}
