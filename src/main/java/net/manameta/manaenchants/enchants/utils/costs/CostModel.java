package net.manameta.manaenchants.enchants.utils.costs;

import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystCost;

public interface CostModel {

    int getXpCost(int level);

    CatalystCost getCatalyst(int level);

    int getBookshelfRequirement(int level);
}