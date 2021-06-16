package ninjaphenix.preciseblockplacing.forge;

import net.minecraft.client.KeyMapping;

import java.util.function.Consumer;

class ToggleKeyMapping extends KeyMapping {
    private final Consumer<Boolean> stateChangeHandler;

    ToggleKeyMapping(String name, int key, String category, boolean initialState, Consumer<Boolean> onPress) {
        super(name, key, category);
        super.setDown(initialState);
        stateChangeHandler = onPress;
    }

    @Override
    public void setDown(boolean down) {
        if (down) {
            super.setDown(!this.isDown());
            stateChangeHandler.accept(this.isDown());
        }
    }
}
