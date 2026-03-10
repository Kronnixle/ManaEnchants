package net.manameta.manaenchants.enchants.utils.costs.bookshelves;

public final class IgnoredBookshelf implements BookshelfRequirement {

    @Override
    public int compute(int level) {
        return 0;
    }
}