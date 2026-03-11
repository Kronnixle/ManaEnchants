package net.manameta.manaenchants.enchants.costs.catalyst;

@FunctionalInterface
public interface CatalystFormula {
    CatalystCost compute(int level);
}