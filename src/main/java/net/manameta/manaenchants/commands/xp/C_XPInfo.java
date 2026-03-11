package net.manameta.manaenchants.commands.xp;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.xp.override.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

final class C_XPInfo {
    /** Prevent instantization */
    private C_XPInfo() {}

    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getXPPrefix(), "commands.error.player.command");
            return 0;
        }

        return execute(sender, player.getName());
    }

    static int execute(@Nonnull Audience sender, @Nonnull String playerResolver) {
        Player player = Bukkit.getPlayer(playerResolver);
        
        ConfigData config = ConfigData.get();
        if (player == null) {
            MessageHelpers.error(sender, ConfigData.get().getXPPrefix(), "commands.error.player.not.found",
                    Component.text(playerResolver, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }
        
        sender.sendMessage(Component.empty());
        sender.sendMessage(ConfigData.get().getXPPrefix().append(Component.translatable("commands.xp.info.display", config.getDescriptionColour(),
                Component.text(player.getName(), config.getHeaderColour()),
                Component.text(player.getLevel(), config.getSuccessHighlightColour()),
                Component.text(XPManager.getTotalXP(player), config.getSuccessHighlightColour()),
                Component.text(XPManager.getXPToNextLevel(player), config.getSuccessHighlightColour()))));

        return 1;
    }
}
