package net.manameta.manaenchants.commands.core;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.helpers.PermissionHelpers;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

final class C_CoreVersion {
    /** Prevent instantization */
    private C_CoreVersion() {}

    /**
     * Display information about the plugin, optionally displaying the clickable help command
     * if the user has permission: {@link PermissionHelpers#MANAENCHANTS_HELP }
     *
     * @param sender the user who sent the command
     * @return 1 always
     */
    static int execute(@Nonnull Audience sender) {
        PluginMeta pluginMeta = ManaEnchants.getInstance().getPluginMeta();
        ConfigData config = ConfigData.get();
        sender.sendMessage(Component.text(pluginMeta.getDisplayName(), config.getHeaderColour()));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.translatable("commands.core.version.description", config.getDescriptionColour(),
                Component.text(" " + pluginMeta.getDescription(), config.getDescriptionHighlightColour())));

        sender.sendMessage(Component.translatable("commands.core.version.website", config.getDescriptionColour(),
                Component.text(" " + pluginMeta.getWebsite(), config.getDescriptionHighlightColour())));

        if (sender instanceof Player player && !player.hasPermission(PermissionHelpers.MANAENCHANTS_HELP)) return 1;

        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.translatable("commands.core.help", config.getDescriptionHighlightColour())
                .hoverEvent(HoverEvent.showText(Component.translatable("commands.core.help", config.getDescriptionColour())))
                .clickEvent(ClickEvent.callback(audience -> C_Help.execute(sender, ParentCommand.MANAENCHANTS, 1))));

        return 1;
    }
}