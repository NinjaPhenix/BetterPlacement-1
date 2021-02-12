package ninjaphenix.preciseblockplacing.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ToggleKeyMapping extends FabricKeyBinding {
    private final Consumer<Boolean> stateChangeHandler;

    public ToggleKeyMapping(ResourceLocation name, InputConstants.Type type, int key, String category,
                            boolean initialState, Consumer<Boolean> onPress) {
        super(name, type, key, category);
        super.setDown(initialState);
        stateChangeHandler = onPress;
    }

    @Override
    public void setDown(boolean down) {
        if (down) {
            super.setDown(!isDown());
            stateChangeHandler.accept(isDown());
        }
    }
}