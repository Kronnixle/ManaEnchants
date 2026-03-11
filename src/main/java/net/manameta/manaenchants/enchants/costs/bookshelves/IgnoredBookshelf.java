package net.manameta.manaenchants.enchants.costs.bookshelves;

public final class IgnoredBookshelf implements BookshelfRequirement {

    @Override
    public int compute(int level) {
        return 0;
    }
}