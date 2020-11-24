package ninjaphenix.preciseblockplacing.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(value = PreciseBlockPlacing.MOD_ID)
public class PreciseBlockPlacing {

    public static final String MOD_ID = "preciseblockplacing";
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private KeyBinding toggleForceKeyBinding;
    private Vector3d lastPlayerPos;

    public PreciseBlockPlacing() {
        Configs.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(toggleForceKeyBinding = new KeyBinding("key." + MOD_ID + ".togglenewloc", GLFW.GLFW_KEY_UNKNOWN, "category." + MOD_ID));
    }

    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START)
        {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) { return; }
            if (toggleForceKeyBinding.isPressed()) {
                final boolean forceNewLoc = !Configs.CLIENT.forceNewLoc.get();
                Configs.CLIENT.forceNewLoc.set(forceNewLoc);
                // TranslationTextComponent doesn't honor § formatting => StringTextComponent(I18n.translate)
                client.player.sendStatusMessage(new StringTextComponent(I18n.format(MOD_ID + ".togglenewloc." + forceNewLoc)), true);
            }

            if(!Configs.CLIENT.creativeOnly.get() || client.player.isCreative()) {
                int timer = client.rightClickDelayTimer;
                RayTraceResult hover = client.objectMouseOver;
                if (hover != null && hover.getType() == Type.BLOCK) {
                    BlockRayTraceResult hit = (BlockRayTraceResult) hover;
                    Direction face = hit.getFace();
                    BlockPos pos = hit.getPos();
                    Vector3d playerPos = client.player.getPositionVec();
                    if (timer > 0) {
                        if (!pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.offset(lastTargetSide)))) {
                            client.rightClickDelayTimer = 0;
                        }
                    }
                    else {
                        BlockPos playerBlockPos = client.player.getPosition();
                        if (face == Direction.UP && !playerPos.equals(lastPlayerPos) && playerBlockPos.getX() == pos.getX() && playerBlockPos.getZ() == pos.getZ()) {
                            client.rightClickDelayTimer = 0;
                        } else {
                            if (Configs.CLIENT.forceNewLoc.get() && pos.equals(lastTargetPos) && face == lastTargetSide) {
                                client.rightClickDelayTimer = 4;
                            }
                        }
                    }
                    lastTargetPos = pos.toImmutable();
                    lastTargetSide = face;
                    lastPlayerPos = playerPos;
                }
            }
        }
    }
}