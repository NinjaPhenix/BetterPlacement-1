package ninjaphenix.preciseblockplacing.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(value = PreciseBlockPlacing.MOD_ID)
public class PreciseBlockPlacing {
    static final String MOD_ID = "preciseblockplacing";

    public PreciseBlockPlacing() {
        if (FMLLoader.getDist().isClient()) {
            Client.getInstance().initialize();
        }
    }
}
