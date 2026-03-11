package net.manameta.manaenchants.enchants.costs.bookshelves;

@FunctionalInterface
public interface BookshelfRequirement {
    int compute(int level);
}