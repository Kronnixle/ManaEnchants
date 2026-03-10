package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class LogXPFormula implements XPFormula {

    private final double base;
    private final double multiplier;

    public LogXPFormula(double base, double multiplier) {
        this.base = base;
        this.multiplier = multiplier;
    }

    @Override
    public int compute(int level) {
        if (level <= 0) return 0;
        double value = base + StrictMath.log(level) * multiplier;
        return (int) Math.max(0, Math.round(value));
    }
}