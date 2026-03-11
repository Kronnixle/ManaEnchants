package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.audience.Audience;
import net.manameta.manaenchants.common.config.ConfigData;
import org.jetbrains.annotations.NotNull;

public final class SoundHelpers {
    /** Prevent instantiation */
    private SoundHelpers() {}

    static void errorSound(@NotNull Audience sender) {
        sender.playSound(ConfigData.get().getErrorSound());
    }

    public static void clickSound(@NotNull Audience sender) { sender.playSound(ConfigData.get().getClickSound()); }

    public static void successSound(@NotNull Audience sender) {
        sender.playSound(ConfigData.get().getSuccessSound());
    }

    public static void enchantSound(@NotNull Audience sender) { sender.playSound(ConfigData.get().getEnchantSound());}
}