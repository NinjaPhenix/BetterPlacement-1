package ninjaphenix.betterplacement.fabric;

import blue.endless.jankson.Comment;
import blue.endless.jankson.annotation.SerializedName;

public class Config {
    @Comment("If true, the modifications will only apply in creative mode.")
    @SerializedName("creativeOnly")
    public Boolean creativeOnly = false;

    @Comment("When true, a held right click will never place two blocks in a row, the player must move the cursor to a new location.")
    @SerializedName("forceNewLoc")
    public Boolean forceNewLoc = true;
}