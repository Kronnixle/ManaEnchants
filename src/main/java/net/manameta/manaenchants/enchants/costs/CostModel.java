package net.manameta.manaenchants.enchants.costs;

import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;

public interface CostModel {

    int getXpCost(int level);

    CatalystCost getCatalyst(int level);

    int getBookshelfRequirement(int level);

    int getXPRequirementCost(int level);
}