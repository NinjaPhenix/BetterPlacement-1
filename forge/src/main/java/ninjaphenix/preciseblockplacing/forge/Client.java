package ninjaphenix.preciseblockplacing.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

final class Client {
    private static Client instance;
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private Vec3 lastPlayerPos;
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

    private void configLoad(ModConfig.Loading event) {
        if (PreciseBlockPlacing.MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            enabled = Configs.CLIENT.enabled.get();
            forceNewLocation = Configs.CLIENT.forceNewLocation.get();
            creativeOnly = Configs.CLIENT.creativeOnly.get();
        }
    }

    private void configReload(ModConfig.Reloading event) {
        if (PreciseBlockPlacing.MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            enabled = Configs.CLIENT.enabled.get();
            forceNewLocation = Configs.CLIENT.forceNewLocation.get();
            creativeOnly = Configs.CLIENT.creativeOnly.get();
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key.preciseblockplacing.toggle_new_location", GLFW.GLFW_KEY_UNKNOWN, "category.preciseblockplacing", forceNewLocation, (value) -> {
            forceNewLocation = value;
            // § formatting not honored using formatted components
            Minecraft.getInstance().player.displayClientMessage(new TextComponent(I18n.get("preciseblockplacing.toggle_new_location." + forceNewLocation)), true);
        }));
        ClientRegistry.registerKeyBinding(new ToggleKeyMapping("key.preciseblockplacing.toggle_enabled", GLFW.GLFW_KEY_UNKNOWN, "category.preciseblockplacing", enabled, (value) -> {
            enabled = value;
            // § formatting not honored using formatted components
            Minecraft.getInstance().player.displayClientMessage(new TextComponent(I18n.get("preciseblockplacing.toggle_enabled." + enabled)), true);
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
            if (client.level == null || client.player == null) {
                return;
            }
            if (!creativeOnly || client.player.isCreative()) {
                int timer = client.rightClickDelay;
                HitResult hover = client.hitResult;
                if (hover != null && hover.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hit = (BlockHitResult) hover;
                    Direction face = hit.getDirection();
                    BlockPos pos = hit.getBlockPos();
                    Vec3 playerPos = client.player.position();
                    if (timer > 0) {
                        if (!pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.relative(lastTargetSide)))) {
                            client.rightClickDelay = 0;
                        }
                    } else {
                        BlockPos playerBlockPos = client.player.blockPosition();
                        if (face == Direction.UP && !playerPos.equals(lastPlayerPos) && playerBlockPos.getX() == pos.getX() && playerBlockPos.getZ() == pos.getZ()) {
                            client.rightClickDelay = 0;
                        } else if (forceNewLocation && pos.equals(lastTargetPos) && face == lastTargetSide) {
                            client.rightClickDelay = 4;
                        }
                    }
                    lastTargetPos = pos.immutable();
                    lastTargetSide = face;
                    lastPlayerPos = playerPos;
                }
            }
        }
    }
}
