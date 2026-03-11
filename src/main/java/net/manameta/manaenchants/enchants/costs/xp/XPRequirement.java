package net.manameta.manaenchants.enchants.costs.xp;

public record XPRequirement(int baseLevel, int perLevel) {
    public int getRequiredLevel(int enchantLevel) {
        return baseLevel + (perLevel * enchantLevel);
    }
}