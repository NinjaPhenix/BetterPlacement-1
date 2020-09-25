package ninjaphenix.preciseblockplacing.fabric.mixins;

import net.minecraft.client.MinecraftClient;
import ninjaphenix.preciseblockplacing.fabric.PreciseBlockPlacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTick
{
    @Inject(method = "tick()V", at=@At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", ordinal = 0))
    private void preciseblockplacing_clientTick(CallbackInfo callbackInfo) {
        PreciseBlockPlacing.INSTANCE.onClientTick((MinecraftClient) (Object) this);
    }
}