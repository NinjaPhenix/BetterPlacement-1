package ninjaphenix.betterplacement;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ninjaphenix.betterplacement.config.JanksonConfigParser;
import org.apache.logging.log4j.MarkerManager;

public class BetterPlacement implements ClientModInitializer {

    public static final BetterPlacement INSTANCE = new BetterPlacement();
    private BetterPlacement() {}

    public static final String MOD_ID = "ninjaphenix_betterplacement";
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private Config _config;

    public void onClientTick(MinecraftClient client)
    {
        if(!_config.creativeOnly || client.player.isCreative()) {
            int timer = client.itemUseCooldown;
            HitResult hover = client.crosshairTarget;
            if(hover != null && hover.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) hover;
                Direction side = hit.getSide();
                BlockPos pos = hit.getBlockPos();
                if (timer > 0 && !pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.offset(lastTargetSide)))) {
                    client.itemUseCooldown = 0;
                } else if (_config.forceNewLoc && timer == 0 && pos.equals(lastTargetPos) && side == lastTargetSide) {
                    client.itemUseCooldown = 4;
                }
                lastTargetPos = pos.toImmutable();
                lastTargetSide = side;
            }
        }
    }

    @Override
    public void onInitializeClient() {
        _config = new JanksonConfigParser().load(Config.class, Config::new,
                FabricLoader.getInstance().getConfigDir().resolve(MOD_ID+".json"), new MarkerManager.Log4jMarker(MOD_ID));
    }
}