package ninjaphenix.betterplacement;

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

@Mod(value = BetterPlacement.MOD_ID)
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.FORGE)
public class BetterPlacement {

    public static final String MOD_ID = "ninjaphenix_betterplacement";
    private static BlockPos lastTargetPos;
    private static Direction lastTargetSide;

    public BetterPlacement() {
        Configs.register();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && (!Configs.CLIENT.creativeOnly.get() || Minecraft.getInstance().player.isCreative())) {
            int timer = Minecraft.getInstance().rightClickDelayTimer;
            RayTraceResult hover = Minecraft.getInstance().objectMouseOver;
            if (hover != null && hover.getType() == Type.BLOCK) {
                BlockRayTraceResult hit = (BlockRayTraceResult) hover;
                Direction face = hit.getFace();
                BlockPos pos = hit.getPos();
                if (timer > 0 && !pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.offset(lastTargetSide)))) {
                    Minecraft.getInstance().rightClickDelayTimer = 0;
                } else if (Configs.CLIENT.forceNewLoc.get() && timer == 0 && pos.equals(lastTargetPos) && hit.getFace() == lastTargetSide) {
                    Minecraft.getInstance().rightClickDelayTimer = 4;
                }
                lastTargetPos = pos.toImmutable();
                lastTargetSide = face;
            }
        }
    }
}