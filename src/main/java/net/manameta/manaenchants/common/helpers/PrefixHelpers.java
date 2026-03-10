package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public final class PrefixHelpers {
    /** Prevent instantization */
    private PrefixHelpers() {}

    private static @NotNull Component wrapper(Component component) {
        return Component.text("[", NamedTextColor.GRAY).append(component).append(Component.text("] ", NamedTextColor.GRAY));
    }

    public static final Component CORE_PREFIX = wrapper(Component.text("ManaEnchants", NamedTextColor.GOLD));
    public static final Component ENCHANT_PREFIX = wrapper(Component.text("Enchant", NamedTextColor.GOLD));
    public static final Component XP_PREFIX = wrapper(Component.text("XP", NamedTextColor.GOLD));

}
