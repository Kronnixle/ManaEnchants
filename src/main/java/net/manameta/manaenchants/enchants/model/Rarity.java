package net.manameta.manaenchants.enchants.model;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.NonNull;

public enum Rarity {
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.GRAY),
    RARE(NamedTextColor.LIGHT_PURPLE),
    LEGENDARY(NamedTextColor.GOLD);

    private final @NonNull NamedTextColor color;

    Rarity(@NonNull NamedTextColor color) {
        this.color = color;
    }

    public @NonNull NamedTextColor getColor() {
        return color;
    }
}