package net.manameta.manaenchants.xp.override;

import io.papermc.paper.persistence.PersistentDataViewHolder;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.xp.model.XPFormula;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public final class XPManager {

    private static final NamespacedKey XP_KEY = new NamespacedKey(ManaEnchants.getInstance(), "total_xp");

    private XPManager() {}

    public static boolean hasKey(@NonNull PersistentDataViewHolder holder) {
        return holder.getPersistentDataContainer().has(XP_KEY);
    }

    public static int getTotalXP(@NonNull PersistentDataHolder holder) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.getOrDefault(XP_KEY, PersistentDataType.INTEGER, 0);
    }

    public static void setTotalXP(@NonNull Player player, int xp) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(XP_KEY, PersistentDataType.INTEGER, Math.max(0, xp));

        updateLevel(player);
    }

    public static void setXPLevels(Player player, int level) {
        if (level < 0) level = 0;

        int targetXP = getTotalXPForLevel(level);
        setTotalXP(player, targetXP);
    }

    public static void addXP(Player player, int amount) {
        if (amount <= 0) return;

        int newXP = getTotalXP(player) + amount;
        setTotalXP(player, newXP);
    }

    public static void addXPLevels(Player player, int levels) {
        if (levels <= 0) return;

        int currentLevel = player.getLevel();
        int targetLevel = currentLevel + levels;

        int currentXP = getTotalXP(player);
        int targetXP = getTotalXPForLevel(targetLevel);

        addXP(player, targetXP - currentXP);
    }

    public static void removeXP(Player player, int amount) {
        if (amount <= 0) return;

        int newXP = Math.max(0, getTotalXP(player) - amount);
        setTotalXP(player, newXP);
    }

    public static int getXPToNextLevel(@NonNull PersistentDataHolder player) {
        int totalXP = getTotalXP(player);

        int level = 0;
        int remaining = totalXP;

        while (true) {
            int cost = XPFormula.calculate(level);
            if (remaining < cost) break;

            remaining -= cost;
            level++;
        }

        int nextCost = XPFormula.calculate(level);
        return nextCost - remaining;
    }

    public static void removeXPLevels(Player player, int levels) {
        if (levels <= 0) return;

        int currentLevel = player.getLevel();
        int targetLevel = Math.max(0, currentLevel - levels);

        int currentXP = getTotalXP(player);
        int targetXP = getTotalXPForLevel(targetLevel);

        removeXP(player, currentXP - targetXP);
    }

    /**
     * Converts total XP into level + progress.
     */
    public static void updateLevel(Player player) {
        int totalXP = getTotalXP(player);

        int level = 0;
        int remaining = totalXP;

        while (true) {
            int cost = XPFormula.calculate(level);

            if (remaining < cost) break;

            remaining -= cost;
            level++;
        }

        int nextCost = XPFormula.calculate(level);

        float progress = nextCost == 0 ? 0 : (float) remaining / nextCost;

        player.setLevel(level);
        player.setExp(progress);
    }

    private static int getTotalXPForLevel(int level) {
        int total = 0;

        for (int i = 0; i < level; i++) {
            total += XPFormula.calculate(i);
        }

        return total;
    }
}