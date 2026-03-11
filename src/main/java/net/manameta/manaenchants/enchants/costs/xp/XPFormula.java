package net.manameta.manaenchants.enchants.costs.xp;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.annotation.Nonnull;

public final class XPFormula {
    private final Expression expr;

    public XPFormula(@Nonnull String formula) {
        expr = new ExpressionBuilder(formula).variables("level").build();
    }

    public int compute(int level) {
        expr.setVariable("level", level);
        return (int) Math.round(expr.evaluate());
    }
}