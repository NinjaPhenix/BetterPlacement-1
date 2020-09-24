package ninjaphenix.preciseblockplacing.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = PreciseBlockPlacing.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Configs
{
    public static final Configs.Client CLIENT;
    private static final ForgeConfigSpec clientSpec;

    static
    {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Configs.Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void register() { ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec); }

    public static class Client
    {
        public final ForgeConfigSpec.BooleanValue creativeOnly;
        public final ForgeConfigSpec.BooleanValue forceNewLoc;

        Client(final ForgeConfigSpec.Builder builder)
        {
            builder.push("client");
            creativeOnly = builder.comment("If true, the modifications will only apply in creative mode.")
                    .define("creativeOnly", false);

            forceNewLoc = builder.comment("When true, a held right click will never place two blocks in a row, the player must move the cursor to a new location.")
                    .define("forceNewLoc", true);
        }
    }
}