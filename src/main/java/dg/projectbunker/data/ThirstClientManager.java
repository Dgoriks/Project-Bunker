package dg.projectbunker.data;

import dg.projectbunker.client.gui.ThirstTracker;
import net.minecraft.world.entity.player.Player;

public class ThirstClientManager {
    public static int getThirst(Player player) {
        return ThirstTracker.getThirst();
    }

    public static void setThirst(int thirst) {
        ThirstTracker.setClientThirst(thirst);
    }
}