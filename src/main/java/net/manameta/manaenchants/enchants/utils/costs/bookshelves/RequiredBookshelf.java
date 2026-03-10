package net.manameta.manaenchants.enchants.utils.costs.bookshelves;

public final class RequiredBookshelf implements BookshelfRequirement {

    private final int base;
    private final int perLevel;

    public RequiredBookshelf(int base, int perLevel) {
        this.base = base;
        this.perLevel = perLevel;
    }

    @Override
    public int compute(int level) {
        return Math.max(0, base + ((level) * perLevel));
    }
}