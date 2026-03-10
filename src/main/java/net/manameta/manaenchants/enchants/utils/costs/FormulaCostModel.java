package net.manameta.manaenchants.enchants.utils.costs;

import net.manameta.manaenchants.enchants.utils.costs.bookshelves.BookshelfRequirement;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystFormula;
import net.manameta.manaenchants.enchants.utils.costs.xp.XPFormula;

import javax.annotation.Nonnull;

public final class FormulaCostModel implements CostModel {

    private final XPFormula xpFormula;
    private final CatalystFormula catalystFormula;
    private final BookshelfRequirement bookshelfRequirement;

    public FormulaCostModel(@Nonnull XPFormula xpFormula, @Nonnull CatalystFormula catalystFormula, @Nonnull BookshelfRequirement bookshelfRequirement) {
        this.xpFormula = xpFormula;
        this.catalystFormula = catalystFormula;
        this.bookshelfRequirement = bookshelfRequirement;
    }

    @Override
    public int getXpCost(int level) {
        return xpFormula.compute(level);
    }

    @Override
    public @Nonnull CatalystCost getCatalyst(int level) {
        return catalystFormula.compute(level);
    }

    @Override
    public int getBookshelfRequirement(int level) {
        return bookshelfRequirement.compute(level);
    }
}