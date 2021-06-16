package ninjaphenix.preciseblockplacing.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class PreciseBlockPlacing implements ClientModInitializer {
    public static final PreciseBlockPlacing INSTANCE = new PreciseBlockPlacing();
    private final String MOD_ID = "preciseblockplacing";
    public final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".properties");
    private BlockPos lastTargetPos;
    private Vec3 lastPlayerPos;
    private Direction lastTargetSide;
    private boolean CREATIVE_ONLY, FORCE_NEW_LOCATION, ENABLED;
    private PreciseBlockPlacing() {
    }

    public void onClientTick(Minecraft client) {
        if (!ENABLED || client.level == null || client.player == null) {
            return;
        }
        if (!CREATIVE_ONLY || client.player.isCreative()) {
            int timer = client.rightClickDelay;
            HitResult hover = client.hitResult;
            if (hover != null && hover.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) hover;
                Direction side = hit.getDirection();
                BlockPos pos = hit.getBlockPos();
                Vec3 playerPos = client.player.position();
                if (timer > 0) {
                    if (!pos.equals(lastTargetPos) && (lastTargetPos == null || !pos.equals(lastTargetPos.relative(lastTargetSide)))) {
                        client.rightClickDelay = 0;
                    }
                } else {
                    BlockPos playerBlockPos = client.player.blockPosition();
                    if (side == Direction.UP && !playerPos.equals(lastPlayerPos) && playerBlockPos.getX() == pos.getX() && playerBlockPos.getZ() == pos.getZ()) {
                        client.rightClickDelay = 0;
                    } else if (FORCE_NEW_LOCATION && pos.equals(lastTargetPos) && side == lastTargetSide) {
                        client.rightClickDelay = 4;
                    }
                }
                lastTargetPos = pos.immutable();
                lastPlayerPos = playerPos;
                lastTargetSide = side;
            }
        }
    }

    @Override
    public void onInitializeClient() {
        Properties config = new Properties();
        Properties defaultConfig = this.loadDefaultConfig();
        if (Files.exists(CONFIG_PATH)) {
            try {
                config.load(Files.newInputStream(CONFIG_PATH, StandardOpenOption.READ));
            } catch (IOException e) {
                config = defaultConfig;
                LOGGER.warn("Failed to read config, using default values.", e);
            }
        } else {
            config = defaultConfig;
            try {
                this.saveConfig(config);
            } catch (IOException e) {
                LOGGER.warn("Failed to save default config.", e);
            }
        }
        boolean needsSaving = false;
        String creativeOnly = config.getProperty("creativeOnly");
        if (creativeOnly == null) {
            creativeOnly = defaultConfig.getProperty("creativeOnly");
            config.setProperty("creativeOnly", creativeOnly);
            needsSaving = true;
        }
        String forceNewLocation = config.getProperty("forceNewLoc");
        if (forceNewLocation == null) {
            forceNewLocation = defaultConfig.getProperty("forceNewLoc");
            config.setProperty("forceNewLoc", forceNewLocation);
            needsSaving = true;
        }
        String enabled = config.getProperty("enabled");
        if (enabled == null) {
            enabled = defaultConfig.getProperty("enabled");
            config.setProperty("enabled", enabled);
            needsSaving = true;
        }
        CREATIVE_ONLY = Boolean.parseBoolean(creativeOnly);
        FORCE_NEW_LOCATION = Boolean.parseBoolean(forceNewLocation);
        ENABLED = Boolean.parseBoolean(enabled);
        if (needsSaving) {
            try {
                this.saveConfig(config);
            } catch (IOException e) {
                LOGGER.warn("Failed to save config, new config keys will be missing.", e);
            }
        }
        KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key." + MOD_ID + ".toggle_new_location", GLFW.GLFW_KEY_UNKNOWN, "category." + MOD_ID, FORCE_NEW_LOCATION, (value) -> {
            FORCE_NEW_LOCATION = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.displayClientMessage(new TextComponent(I18n.get(MOD_ID + ".toggle_new_location." + FORCE_NEW_LOCATION)), true);
        }));
        KeyBindingHelper.registerKeyBinding(new ToggleKeyMapping("key." + MOD_ID + ".toggle_enabled", GLFW.GLFW_KEY_UNKNOWN, "category." + MOD_ID, ENABLED, (value) -> {
            ENABLED = value;
            // TranslatableText doesn't honor § formatting => LiteralText(I18n.translate)
            Minecraft.getInstance().player.displayClientMessage(new TextComponent(I18n.get(MOD_ID + ".toggle_enabled." + ENABLED)), true);
        }));
    }

    private void saveConfig(Properties config) throws IOException {
        config.store(Files.newOutputStream(CONFIG_PATH, StandardOpenOption.CREATE),
                "Precise Block Placing Config\n" +
                        "creativeOnly - If true, the modifications will only apply in creative mode.\n" +
                        "forceNewLoc - When true, a held right click will never place two blocks in a row, the player must move the cursor to a new location.\n" +
                        "enabled - When false block placing behaves exactly like vanilla.");
    }

    private Properties loadDefaultConfig() {
        Properties config = new Properties();
        config.setProperty("creativeOnly", Boolean.FALSE.toString());
        config.setProperty("forceNewLoc", Boolean.TRUE.toString());
        config.setProperty("enabled", Boolean.TRUE.toString());
        return config;
    }

    public void saveCurrentConfig() throws IOException {
        Properties config = new Properties();
        config.setProperty("creativeOnly", String.valueOf(CREATIVE_ONLY));
        config.setProperty("forceNewLoc", String.valueOf(FORCE_NEW_LOCATION));
        config.setProperty("enabled", String.valueOf(ENABLED));
        this.saveConfig(config);
    }
}
