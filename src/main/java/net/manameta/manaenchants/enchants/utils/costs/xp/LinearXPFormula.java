package net.manameta.manaenchants.enchants.utils.costs.xp;

final class LinearXPFormula implements XPFormula {

    private final int base;
    private final int perLevel;

    LinearXPFormula(int base, int perLevel) {
        this.base = base;
        this.perLevel = perLevel;
    }

    @Override
    public int compute(int level) {
        return Math.max(0, base + (level * perLevel));
    }
}