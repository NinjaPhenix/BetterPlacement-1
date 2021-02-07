package ninjaphenix.preciseblockplacing.fabric.mixins;

import net.minecraft.client.Minecraft;
import ninjaphenix.preciseblockplacing.fabric.PreciseBlockPlacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Minecraft.class)
public class ShutdownHook {

    @Inject(method = "Lnet/minecraft/client/Minecraft;run()V", at = @At("TAIL"))
    private void normalShutdown(CallbackInfo info) {
        try {
            PreciseBlockPlacing.INSTANCE.saveCurrentConfig();
        } catch (IOException e) {
            PreciseBlockPlacing.INSTANCE.LOGGER.warn("Failed to save config whilst shutting down.");
        }
    }

    @Inject(method = "Lnet/minecraft/client/Minecraft;emergencySave()V", at = @At("TAIL"))
    private void emergencyShutdown(CallbackInfo info) {
        try {
            PreciseBlockPlacing.INSTANCE.saveCurrentConfig();
        } catch (IOException e) {
            PreciseBlockPlacing.INSTANCE.LOGGER.warn("Failed to save config whilst shutting down.");
        }
    }

}
