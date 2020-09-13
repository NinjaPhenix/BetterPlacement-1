package ninjaphenix.betterplacement.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class BetterPlacement implements ClientModInitializer {

    public static final BetterPlacement INSTANCE = new BetterPlacement();
    private BetterPlacement() {}

    public static final String MOD_ID = "betterplacementupdated";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;
    private Properties _config;

    public void onClientTick(MinecraftClient client)
    {
        if(!creativeOnly() || client.player.isCreative()) {
            int timer = client.itemUseCooldown;
            HitResult hover = client.crosshairTarget;
            if(hover != null && hover.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) hover;
                Direction side = hit.getSide();
                BlockPos pos = hit.getBlockPos();
                if (timer > 0 && !pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.offset(lastTargetSide)))) {
                    client.itemUseCooldown = 0;
                } else if (forceNewLoc() && timer == 0 && pos.equals(lastTargetPos) && side == lastTargetSide) {
                    client.itemUseCooldown = 4;
                }
                lastTargetPos = pos.toImmutable();
                lastTargetSide = side;
            }
        }
    }

    @Override
    public void onInitializeClient() {
        final Properties config = new Properties();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID+".properties");
        if(Files.exists(configPath)) {
            try {
                config.load(Files.newInputStream(configPath, StandardOpenOption.READ));
            } catch (IOException e) {
                loadDefaultConfig(config);
                LOGGER.warn("Failed to read config, using default values:", e);
            }
        } else {
            loadDefaultConfig(config);
            try {
                config.store(Files.newOutputStream(configPath, StandardOpenOption.CREATE),
                        "Better Placement Config\n" +
                        "creativeOnly - If true, the modifications will only apply in creative mode.\n" +
                        "forceNewLoc - When true, a held right click will never place two blocks in a row, the player must move the cursor to a new location.");
            } catch (IOException e) {
                LOGGER.warn("Failed to save default config.", e);
            }
        }
        _config = config;
    }

    private void loadDefaultConfig(Properties config) {
        config.put("creativeOnly", false);
        config.put("forceNewLoc", true);
    }

    private Boolean creativeOnly() { return (Boolean) _config.get("creativeOnly"); }
    private Boolean forceNewLoc() { return (Boolean) _config.get("forceNewLoc"); }
}