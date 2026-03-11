package net.manameta.manaenchants.enchants.costs;

import net.manameta.manaenchants.enchants.costs.bookshelves.BookshelfRequirement;
import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.costs.catalyst.CatalystFormula;
import net.manameta.manaenchants.enchants.costs.xp.XPFormula;
import net.manameta.manaenchants.enchants.costs.xp.XPRequirement;

import javax.annotation.Nonnull;

public final class FormulaCostModel implements CostModel {

    private final XPFormula xpFormula;
    private final XPRequirement xpRequirement;
    private final CatalystFormula catalystFormula;
    private final BookshelfRequirement bookshelfRequirement;

    public FormulaCostModel(@Nonnull XPFormula xpFormula, @Nonnull XPRequirement xpRequirement,
                            @Nonnull CatalystFormula catalystFormula, @Nonnull BookshelfRequirement bookshelfRequirement) {
        this.xpFormula = xpFormula;
        this.xpRequirement = xpRequirement;
        this.catalystFormula = catalystFormula;
        this.bookshelfRequirement = bookshelfRequirement;
    }

    @Override
    public int getXpCost(int level) {
        return xpFormula.compute(level);
    }

    @Override
    @Nonnull
    public CatalystCost getCatalyst(int level) {
        return catalystFormula.compute(level);
    }

    @Override
    public int getXPRequirementCost(int level) { return xpRequirement.getRequiredLevel(level); }

    @Override
    public int getBookshelfRequirement(int level) {
        return bookshelfRequirement.compute(level);
    }
}