package ninjaphenix.preciseblockplacing.forge;

import net.minecraft.client.settings.KeyBinding;

import java.util.function.Consumer;

public class ToggleKeyMapping extends KeyBinding {
    private final Consumer<Boolean> stateChangeHandler;

    public ToggleKeyMapping(String name, int key, String category, boolean initialState, Consumer<Boolean> onPress) {
        super(name, key, category);
        super.setPressed(initialState);
        stateChangeHandler = onPress;
    }

    @Override
    public void setPressed(boolean down) {
        if (down) {
            super.setPressed(!isKeyDown());
            stateChangeHandler.accept(isKeyDown());
        }
    }
}