package net.manameta.manaenchants.xp.override;

import net.manameta.manaenchants.common.config.ConfigData;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataHolder;
import org.jspecify.annotations.NonNull;

public final class XPManager {
    private XPManager() {}

    private static boolean vanilla() {
        return ConfigData.get().isVanillaXP();
    }

    public static int getTotalXP(@NonNull Player player) {
        if (CustomXP.hasKey(player)) {
            return CustomXP.getTotalXP(player);
        }
        return VanillaXP.getTotalXP(player);
    }

    public static void setTotalXP(@NonNull Player player, int xp) {
        xp = Math.max(0, xp);

        // Store canonical value
        CustomXP.setTotalXP(player, xp);

        // Mirror to vanilla
        VanillaXP.setTotalXP(player, xp);

        // Update the display system
        updateDisplay(player);
    }

    public static void addXP(@NonNull Player player, int amount) {
        if (amount <= 0) return;
        setTotalXP(player, getTotalXP(player) + amount);
    }

    public static void removeXP(@NonNull Player player, int amount) {
        if (amount <= 0) return;
        setTotalXP(player, Math.max(0, getTotalXP(player) - amount));
    }

    public static void setXPLevels(@NonNull Player player, int level) {
        int xp = CustomXP.getTotalXPForLevel(level);
        setTotalXP(player, xp);
    }

    public static void addXPLevels(@NonNull Player player, int levels) {
        if (levels <= 0) return;

        int currentLevel = player.getLevel();
        int targetLevel = currentLevel + levels;

        int xp = CustomXP.getTotalXPForLevel(targetLevel);
        setTotalXP(player, xp);
    }

    public static void removeXPLevels(@NonNull Player player, int levels) {
        if (levels <= 0) return;

        int currentLevel = player.getLevel();
        int targetLevel = Math.max(0, currentLevel - levels);

        int xp = CustomXP.getTotalXPForLevel(targetLevel);
        setTotalXP(player, xp);
    }

    public static int getXPToNextLevel(PersistentDataHolder holder) {
        return CustomXP.getXPToNextLevel(holder);
    }

    private static void updateDisplay(@NonNull Player player) {
        if (!vanilla()) {
            CustomXP.updateLevel(player);
        }
    }

    public static void updatePlayer(@NonNull Player player) {
        int xp = getTotalXP(player);

        CustomXP.setTotalXP(player, xp);
        VanillaXP.setTotalXP(player, xp);

        updateDisplay(player);
    }
}