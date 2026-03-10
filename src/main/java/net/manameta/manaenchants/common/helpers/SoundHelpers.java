package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.manameta.api.core.services.ManaServices;
import net.manameta.api.core.settings.enums.SettingKey;
import net.manameta.manaenchants.common.config.ConfigData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class SoundHelpers {
    /** Prevent instantiation */
    private SoundHelpers() {}

    public static void errorSound(@NotNull Audience sender) {
        UUID userID = sender.get(Identity.UUID).orElse(new UUID(0, 0));
        if (!ManaServices.getSettingsService().getSettings(userID).getBoolean(SettingKey.SOUND_ERROR)) return;

        sender.playSound(ConfigData.get().getErrorSound());
    }

    public static void clickSound(@NotNull Audience sender) {
        UUID userID = sender.get(Identity.UUID).orElse(new UUID(0, 0));
        if (!ManaServices.getSettingsService().getSettings(userID).getBoolean(SettingKey.SOUND_CLICK)) return;

        sender.playSound(ConfigData.get().getClickSound());
    }

    public static void successSound(@NotNull Audience sender) {
        UUID userID = sender.get(Identity.UUID).orElse(new UUID(0, 0));
        if (!ManaServices.getSettingsService().getSettings(userID).getBoolean(SettingKey.SOUND_SUCCESS)) return;

        sender.playSound(ConfigData.get().getSuccessSound());
    }
}