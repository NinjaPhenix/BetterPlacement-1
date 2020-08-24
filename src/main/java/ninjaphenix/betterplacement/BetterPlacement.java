package ninjaphenix.betterplacement;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod(value = BetterPlacement.MOD_ID)
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public class BetterPlacement {

    public static final String MOD_ID = "ninjaphenix_betterplacement";

    //@EventHandler
    //public void preInit(FMLPreInitializationEvent event) {
    //    Configs.load(event.getSuggestedConfigurationFile());
    //}

    private static BlockPos lastTargetPos;
    private static Direction lastTargetSide;

    // todo: replace with at
    private static final Field _rightClickDelayTimer = ObfuscationReflectionHelper.findField(Minecraft.class, "field_71467_ac");

    private static final MethodHandle getDelayTimer, setDelayTimer;
    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            getDelayTimer = lookup.unreflectGetter(_rightClickDelayTimer);
            setDelayTimer = lookup.unreflectSetter(_rightClickDelayTimer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) throws Throwable {
        if (event.phase == TickEvent.Phase.START /*&& (!Configs.creativeOnly || Minecraft.getMinecraft().player.isCreative())*/) {
            int timer = (int) getDelayTimer.invoke(Minecraft.getInstance());
            RayTraceResult hover = Minecraft.getInstance().objectMouseOver;
            if (hover != null && hover.getType() == Type.BLOCK) {
                BlockRayTraceResult hit = (BlockRayTraceResult) hover;
                Direction face = hit.getFace();
                BlockPos pos = hit.getPos();
                if (timer > 0 && !pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.offset(lastTargetSide)))) {
                    setDelayTimer.invoke(Minecraft.getInstance(), 0);
                } /*else if (Configs.forceNewLoc && timer == 0 && pos.equals(lastTargetPos) && hit.getFace() == lastTargetSide) {
                    setDelayTimer.invoke(Minecraft.getInstance(), 4);
                } */
                lastTargetPos = pos.toImmutable();
                lastTargetSide = face;
            }
        }
    }
}