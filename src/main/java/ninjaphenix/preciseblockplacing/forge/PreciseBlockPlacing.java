package ninjaphenix.preciseblockplacing.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(value = PreciseBlockPlacing.MOD_ID)
public class PreciseBlockPlacing {

    public static final String MOD_ID = "preciseblockplacing";
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private Vector3d lastPlayerPos;
    private boolean enabled = true;
    private boolean forceNewLocation = true;
    private boolean creativeOnly = false;
    public PreciseBlockPlacing() {
        Configs.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onClientLogout);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
    }

    public void configLoad(ModConfig.ModConfigEvent event) {
        if (MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            CommentedConfig config = event.getConfig().getConfigData();
            enabled = config.get("enabled");
            forceNewLocation = config.get("forceNewLoc");
            creativeOnly = config.get("creativeOnly");
        }
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key." + MOD_ID + ".toggle_new_location", GLFW.GLFW_KEY_UNKNOWN, "category." + MOD_ID, forceNewLocation, (value) -> {
            forceNewLocation = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(I18n.format(MOD_ID + ".toggle_new_location." + forceNewLocation)), true);
        }));
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key." + MOD_ID + ".toggle_enabled", GLFW.GLFW_KEY_UNKNOWN, "category." + MOD_ID, enabled, (value) -> {
            enabled = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(I18n.format(MOD_ID + ".enabled." + enabled)), true);
        }));
    }

    public void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        Configs.CLIENT.enabled.set(enabled);
        Configs.CLIENT.forceNewLocation.set(forceNewLocation);
        Configs.CLIENT.creativeOnly.set(creativeOnly);
        Configs.CLIENT_SPEC.save();
    }

    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            Minecraft client = Minecraft.getInstance();
            if (client.world == null || client.player == null) {
                return;
            }
            if (!creativeOnly || client.player.isCreative()) {
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
                    } else {
                        BlockPos playerBlockPos = client.player.getPosition();
                        if (face == Direction.UP && !playerPos.equals(lastPlayerPos) && playerBlockPos.getX() == pos.getX() && playerBlockPos.getZ() == pos.getZ()) {
                            client.rightClickDelayTimer = 0;
                        } else if (forceNewLocation && pos.equals(lastTargetPos) && face == lastTargetSide) {
                            client.rightClickDelayTimer = 4;
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