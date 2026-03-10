package net.manameta.manaenchants.enchants.utils.costs.bookshelves;

import javax.annotation.Nonnull;
import java.util.Map;

public final class TieredBookshelf implements BookshelfRequirement {

    private final @Nonnull Map<Integer, Integer> tiers;

    public TieredBookshelf(@Nonnull Map<Integer, Integer> tiers) {
        this.tiers = Map.copyOf(tiers);
    }

    @Override
    public int compute(int level) {
        return tiers.getOrDefault(level, 0);
    }
}