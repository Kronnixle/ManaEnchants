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

final class C_XPInfo {
    /** Prevent instantization */
    private C_XPInfo() {}

    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.player.command");
            return 0;
        }

        return execute(sender, player.getName());
    }

    static int execute(@Nonnull Audience sender, @Nonnull String playerResolver) {
        Player player = Bukkit.getPlayer(playerResolver);
        if (player == null) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.player.not.found",
                    Component.text(playerResolver, NamedTextColor.GRAY));
            return 0;
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(PrefixHelpers.XP_PREFIX.append(Component.translatable("commands.xp.info.display", NamedTextColor.GRAY,
                Component.text(player.getName(), NamedTextColor.GOLD),
                Component.text(player.getLevel(), NamedTextColor.YELLOW),
                Component.text(XPManager.getTotalXP(player), NamedTextColor.YELLOW),
                Component.text(XPManager.getXPToNextLevel(player), NamedTextColor.YELLOW))));

        return 1;
    }
}
