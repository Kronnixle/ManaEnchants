package net.manameta.manaenchants.enchants.utils.costs.xp;

public final class StepXPFormula implements XPFormula {

    private final int base;
    private final int stepInterval;
    private final int stepAmount;

    public StepXPFormula(int base, int stepInterval, int stepAmount) {
        this.base = base;
        this.stepInterval = stepInterval;
        this.stepAmount = stepAmount;
    }

    @Override
    public int compute(int level) {
        int steps = level / stepInterval;
        return Math.max(0, base + (steps * stepAmount));
    }
}