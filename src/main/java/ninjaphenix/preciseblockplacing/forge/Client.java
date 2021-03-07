package ninjaphenix.preciseblockplacing.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public final class Client {
    private static Client instance;
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private Vector3d lastPlayerPos;
    private boolean enabled = true;
    private boolean forceNewLocation = true;
    private boolean creativeOnly = false;

    private Client() {

    }

    static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    void initialize() {
        Configs.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::configLoad);
        modBus.addListener(this::configReload);
        MinecraftForge.EVENT_BUS.addListener(this::onClientLogout);
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
    }

    // todo: unused?
    private void configLoad(ModConfig.Loading event) {
        if (PreciseBlockPlacing.MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            enabled = Configs.CLIENT.enabled.get();
            forceNewLocation = Configs.CLIENT.forceNewLocation.get();
            creativeOnly = Configs.CLIENT.creativeOnly.get();
        }
    }

    private void configReload(ModConfig.Loading event) {
        if (PreciseBlockPlacing.MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            enabled = Configs.CLIENT.enabled.get();
            forceNewLocation = Configs.CLIENT.forceNewLocation.get();
            creativeOnly = Configs.CLIENT.creativeOnly.get();
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key.preciseblockplacing.toggle_new_location", GLFW.GLFW_KEY_UNKNOWN, "category.preciseblockplacing", forceNewLocation, (value) -> {
            forceNewLocation = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(I18n.format("preciseblockplacing.toggle_new_location." + forceNewLocation)), true);
        }));
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key.preciseblockplacing.toggle_enabled", GLFW.GLFW_KEY_UNKNOWN, "category.preciseblockplacing", enabled, (value) -> {
            enabled = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(I18n.format("preciseblockplacing.enabled." + enabled)), true);
        }));
    }

    private void onClientLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        Configs.CLIENT.enabled.set(enabled);
        Configs.CLIENT.forceNewLocation.set(forceNewLocation);
        Configs.CLIENT.creativeOnly.set(creativeOnly);
        Configs.CLIENT_SPEC.save();
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
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
                if (hover != null && hover.getType() == RayTraceResult.Type.BLOCK) {
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
