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
        boolean passed = OverpoweredAgainIntegrationTests.run(server);
        server.halt(!passed);
    }
}
