package net.manameta.manaenchants.xp.override;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public final class VanillaXP {
    /** Prevent instantization */
    private VanillaXP() {}

    static int getTotalXP(@NonNull Player player) {
        return player.getTotalExperience();
    }

    static void setTotalXP(@NonNull Player player, int xp) {
        xp = Math.max(0, xp);

        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.giveExp(xp);
    }
}