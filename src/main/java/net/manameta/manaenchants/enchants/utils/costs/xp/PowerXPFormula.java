package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class PowerXPFormula implements XPFormula {

    private final double base;
    private final double exponent;

    public PowerXPFormula(double base, double exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public int compute(int level) {
        double value = base * Math.pow(level, exponent);
        return (int) Math.max(0, Math.round(value));
    }
}