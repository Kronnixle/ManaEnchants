package net.manameta.manaenchants.enchants.utils.costs;

import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystCost;

import javax.annotation.Nonnull;

public record PerLevelCost(CatalystCost catalyst, int xp, int bookshelf) {
    @Override
    public @Nonnull CatalystCost catalyst() {
        return catalyst;
    }
}