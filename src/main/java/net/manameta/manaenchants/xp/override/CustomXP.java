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

public final class CustomXP {
    /** Prevent instantization */
    private CustomXP() {}

    private static final NamespacedKey XP_KEY = new NamespacedKey(ManaEnchants.getInstance(), "total_xp");

    static boolean hasKey(@NonNull PersistentDataViewHolder holder) {
        return holder.getPersistentDataContainer().has(XP_KEY);
    }

    static int getTotalXP(@NonNull PersistentDataHolder holder) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.getOrDefault(XP_KEY, PersistentDataType.INTEGER, 0);
    }

    static void setTotalXP(@NonNull PersistentDataHolder holder, int xp) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(XP_KEY, PersistentDataType.INTEGER, xp);
    }

    static int getXPToNextLevel(@NonNull PersistentDataHolder holder) {
        int totalXP = getTotalXP(holder);

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

    static void updateLevel(@NonNull Player player) {
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

    static int getTotalXPForLevel(int level) {
        int total = 0;

        for (int i = 0; i < level; i++) {
            total += XPFormula.calculate(i);
        }

        return total;
    }
}