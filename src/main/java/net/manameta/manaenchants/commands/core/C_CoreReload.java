package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.SoundHelpers;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import net.manameta.manaenchants.items.SavedItems;
import net.manameta.manaenchants.xp.override.CustomXP;
import net.manameta.manaenchants.xp.override.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

final class C_CoreReload {
    /** Prevent instantization */
    private C_CoreReload() {}

    static int execute(@Nonnull Audience sender) {
        ConfigData.reload();
        LocaleManager.reload();
        SavedItems.reload();
        TieredEnchantsConfig.reload();

        SoundHelpers.successSound(sender);
        sender.sendMessage(ConfigData.get().getCorePrefix().append(Component.translatable("commands.core.reload", ConfigData.get().getSuccessColour())));

        for (Player p : Bukkit.getOnlinePlayers()) {
            XPManager.updatePlayer(p);
        }

        return 1;
    }
}
