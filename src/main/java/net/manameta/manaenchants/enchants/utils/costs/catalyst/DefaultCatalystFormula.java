package net.manameta.manaenchants.enchants.utils.costs.catalyst;

import javax.annotation.Nonnull;
import java.util.Map;

public final class DefaultCatalystFormula implements CatalystFormula {

    private final CatalystCost defaultCost;
    private final int base;
    private final int perLevel;
    private final double multiplier;

    private final @Nonnull Map<Integer, CatalystCost> overrides;

    public DefaultCatalystFormula(@Nonnull CatalystCost defaultCost, int base, int perLevel, double multiplier, @Nonnull Map<Integer, CatalystCost> overrides) {
        this.defaultCost = defaultCost;
        this.base = base;
        this.perLevel = perLevel;
        this.multiplier = multiplier;
        this.overrides = Map.copyOf(overrides);
    }

    @Override
    public CatalystCost compute(int level) {

        CatalystCost override = overrides.get(level);
        if (override != null) {
            return override;
        }

        double amount = (base + (level * perLevel)) * multiplier;
        int finalAmount = (int) Math.max(0, Math.round(amount));

        return defaultCost.withAmount(finalAmount);
    }
}