package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class ExponentialXPFormula implements XPFormula {

    private final double base;
    private final double multiplier;

    public ExponentialXPFormula(double base, double multiplier) {
        this.base = base;
        this.multiplier = multiplier;
    }

    @Override
    public int compute(int level) {
        double value = base * Math.pow(multiplier, level - 1);
        return (int) Math.max(0, Math.round(value));
    }
}