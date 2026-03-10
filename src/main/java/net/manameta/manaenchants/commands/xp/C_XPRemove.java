package net.manameta.manaenchants.commands.xp;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.api.core.commands.HelpID;
import net.manameta.api.core.commands.ParentCommand;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import net.manameta.manaenchants.xp.override.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

final class C_XPRemove {
    /** Prevent instantization */
    private C_XPRemove() {}

    static int execute(@Nonnull Audience sender, @Nonnull String playerResolver, String amountResolver) {
        Player player = Bukkit.getPlayer(playerResolver);
        if (player == null) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.player.not.found",
                    Component.text(playerResolver, NamedTextColor.GRAY));
            return 0;
        }

        char last = amountResolver.charAt(amountResolver.length() - 1);

        try {
            // LEVELS
            if (last == 'l' || last == 'L') {
                int amount = Integer.parseInt(amountResolver.substring(0, amountResolver.length() - 1));

                XPManager.removeXPLevels(player, amount);

                sendSuccess(sender, player.getName(), Component.translatable("commands.xp.levels.of", NamedTextColor.YELLOW,
                        Component.text(amount, NamedTextColor.YELLOW)));
                return 1;
            }

            // RAW XP
            int amount = Integer.parseInt(amountResolver);

            XPManager.removeXP(player, amount);

            sendSuccess(sender, player.getName(), Component.text(amount, NamedTextColor.YELLOW));
            return 1;

        } catch (NumberFormatException ignored) {
            MessageHelpers.error(sender, PrefixHelpers.XP_PREFIX, "commands.error.unknown.input",
                    Component.text(amountResolver, NamedTextColor.GRAY));
            return 0;
        }
    }

    private static void sendSuccess(@Nonnull Audience sender, String playerName, Component amountComponent) {
        MessageHelpers.success(sender, PrefixHelpers.XP_PREFIX, "commands.xp.remove.success",
                Component.text(playerName, NamedTextColor.YELLOW),
                amountComponent);
    }

    static int execute(@Nonnull Audience sender) {
        MessageHelpers.formatError(sender, PrefixHelpers.XP_PREFIX, ParentCommand.XP, HelpID.REMOVE);
        return 1;
    }
}