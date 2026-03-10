package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import net.manameta.manaenchants.common.helpers.SoundHelpers;
import net.manameta.manaenchants.common.locale.LocaleManager;
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

        SoundHelpers.successSound(sender);
        sender.sendMessage(PrefixHelpers.CORE_PREFIX.append(Component.translatable("commands.core.reload", NamedTextColor.GREEN)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            XPManager.updateLevel(p);
        }
        return 1;
    }
}
