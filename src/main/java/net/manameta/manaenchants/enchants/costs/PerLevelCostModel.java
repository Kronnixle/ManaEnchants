package net.manameta.manaenchants.enchants.costs;

import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public final class PerLevelCostModel implements CostModel {

    private final Map<Integer, PerLevelCost> levels;

    public PerLevelCostModel(Map<Integer, PerLevelCost> levels) {
        this.levels = Collections.unmodifiableMap(levels);
    }

    @Override
    public int getXpCost(int level) {
        PerLevelCost cost = levels.get(level);
        return cost != null ? cost.xp() : 0;
    }

    @Override
    public @Nullable CatalystCost getCatalyst(int level) {
        PerLevelCost cost = levels.get(level);
        return cost != null ? cost.catalyst() : null;
    }

    @Override
    public int getXPRequirementCost(int level) {
        PerLevelCost cost = levels.get(level);
        return cost != null ? cost.xpBase() : 0;
    }

    @Override
    public int getBookshelfRequirement(int level) {
        PerLevelCost cost = levels.get(level);
        return cost != null ? cost.bookshelf() : 0;
    }
}