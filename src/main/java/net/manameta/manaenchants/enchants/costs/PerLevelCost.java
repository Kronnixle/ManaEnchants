package net.manameta.manaenchants.enchants.costs;

import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;

import javax.annotation.Nonnull;

public record PerLevelCost(int xpBase, CatalystCost catalyst, int xp, int bookshelf) {
    @Override
    public @Nonnull CatalystCost catalyst() {
        return catalyst;
    }
}