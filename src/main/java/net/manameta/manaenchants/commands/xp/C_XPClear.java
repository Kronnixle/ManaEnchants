package net.manameta.manaenchants.commands.xp;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import net.manameta.manaenchants.xp.override.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

final class C_XPClear {
    /** Prevent instantization */
    private C_XPClear() {}

    static int execute(@Nonnull Audience sender, @Nonnull String playerResolver) {
        Player player = Bukkit.getPlayer(playerResolver);
        if (player == null) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.player.not.found",
                    Component.text(playerResolver, NamedTextColor.GRAY));
            return 0;
        }

        XPManager.setTotalXP(player, 0);
        MessageHelpers.success(sender, PrefixHelpers.XP_PREFIX, "commands.xp.clear.success",
                Component.text(player.getName(), NamedTextColor.YELLOW));

        return 1;
    }


    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.player.command");
            return 0;
        }

        XPManager.setTotalXP(player, 0);
        MessageHelpers.success(sender, PrefixHelpers.XP_PREFIX, "commands.xp.clear.success",
                Component.text(player.getName(), NamedTextColor.YELLOW));
        return 1;
    }
}
