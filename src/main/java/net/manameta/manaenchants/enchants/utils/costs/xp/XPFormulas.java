package net.manameta.manaenchants.enchants.utils.costs.xp;

import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public final class XPFormulas {
    private XPFormulas() {}

    public static @NonNull XPFormula createFormula(@Nonnull XPFormulaType type, int xpBase, int perLevel, double multiplierXP, int stepAmount, int stepInterval) {
        return switch (type) {
            case FLAT -> new FlatXPFormula(xpBase);
            case LINEAR -> new LinearXPFormula(xpBase, perLevel);
            case TRIANGULAR -> new TriangularXPFormula(xpBase, perLevel);
            case EXPONENTIAL ->  new ExponentialXPFormula(xpBase, multiplierXP);
            case POWER -> new PowerXPFormula(xpBase, multiplierXP);
            case STEP -> new StepXPFormula(xpBase, stepInterval, stepAmount);
            case LOG ->  new LogXPFormula(xpBase, multiplierXP);
        };
    }
}
