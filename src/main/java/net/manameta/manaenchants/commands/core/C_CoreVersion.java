package net.manameta.manaenchants.commands.core;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.api.core.commands.ParentCommand;
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
        sender.sendMessage(Component.text(pluginMeta.getDisplayName(), NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("Description:", NamedTextColor.AQUA)
                .append(Component.text(" " + pluginMeta.getDescription(), NamedTextColor.YELLOW)));

        sender.sendMessage(Component.text("Website:", NamedTextColor.AQUA)
                .append(Component.text(" " + pluginMeta.getWebsite(), NamedTextColor.YELLOW)));

        if (sender instanceof Player player && !player.hasPermission(PermissionHelpers.MANAENCHANTS_HELP)) return 1;

        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.translatable("commands.core.help", NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(Component.translatable("commands.core.help", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> C_Help.execute(sender, ParentCommand.MANAENCHANTS, 1))));

        return 1;
    }
}