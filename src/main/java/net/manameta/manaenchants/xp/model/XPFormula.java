package net.manameta.manaenchants.xp.model;

import net.manameta.manaenchants.common.config.ConfigData;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.List;

public final class XPFormula {

    private XPFormula() {}

    public static int calculate(int playerLevel) {
        List<LevelCost> costs = ConfigData.get().getXpCosts();

        LevelCost applicable = costs.stream()
                .filter(c -> c.minLevel() <= playerLevel)
                .reduce((first, second) -> second)
                .orElse(costs.getFirst());

        String formula = applicable.formula();

        try {
            Expression expression = new ExpressionBuilder(formula)
                    .variable("level")
                    .build()
                    .setVariable("level", playerLevel);

            double result = expression.evaluate();

            return Math.max(0, (int) Math.ceil(result));

        } catch (RuntimeException e) {
            return 100;
        }
    }
}