package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class TriangularXPFormula implements XPFormula {

    private final int base;
    private final int perLevel;

    public TriangularXPFormula(int base, int perLevel) {
        this.base = base;
        this.perLevel = perLevel;
    }

    @Override
    public int compute(int level) {
        int triangular = (level * (level + 1)) / 2;
        return Math.max(0, base + triangular * perLevel);
    }
}