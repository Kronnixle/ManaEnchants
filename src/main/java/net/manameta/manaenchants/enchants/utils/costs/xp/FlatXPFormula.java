package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class FlatXPFormula implements XPFormula {

    private final int base;

    public FlatXPFormula(int base) {
        this.base = base;
    }

    @Override
    public int compute(int level) {
        return Math.max(0, base);
    }
}