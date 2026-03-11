package net.manameta.manaenchants.commands.xp;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
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
            MessageHelpers.error(sender, ConfigData.get().getXPPrefix(), "commands.error.player.not.found",
                    Component.text(playerResolver, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        char last = amountResolver.charAt(amountResolver.length() - 1);

        try {
            // LEVELS
            if (last == 'l' || last == 'L') {
                int amount = Integer.parseInt(amountResolver.substring(0, amountResolver.length() - 1));

                XPManager.removeXPLevels(player, amount);

                sendSuccess(sender, player.getName(), Component.translatable("commands.xp.levels.of", ConfigData.get().getSuccessHighlightColour(),
                        Component.text(amount, ConfigData.get().getSuccessHighlightColour())));
                return 1;
            }

            // RAW XP
            int amount = Integer.parseInt(amountResolver);

            XPManager.removeXP(player, amount);

            sendSuccess(sender, player.getName(), Component.text(amount, ConfigData.get().getSuccessHighlightColour()));
            return 1;

        } catch (NumberFormatException ignored) {
            MessageHelpers.error(sender, ConfigData.get().getXPPrefix(), "commands.error.unknown.input",
                    Component.text(amountResolver, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }
    }

    private static void sendSuccess(@Nonnull Audience sender, String playerName, Component amountComponent) {
        MessageHelpers.success(sender, ConfigData.get().getXPPrefix(), "commands.xp.remove.success",
                Component.text(playerName, ConfigData.get().getSuccessHighlightColour()),
                amountComponent);
    }

    static int execute(@Nonnull Audience sender) {
        MessageHelpers.formatError(sender, ParentCommand.XP, HelpID.REMOVE);
        return 1;
    }
}