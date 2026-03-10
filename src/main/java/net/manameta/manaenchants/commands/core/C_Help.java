package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.api.core.commands.HelpID;
import net.manameta.api.core.commands.PaperHelpEntry;
import net.manameta.api.core.commands.ParentCommand;
import net.manameta.api.social.utils.SocialPrefixes;
import net.manameta.manaenchants.common.config.CommandConfig;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import net.manameta.manaenchants.common.helpers.SoundHelpers;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Views the help commands, including pagination support & sub-arg support
 *
 * @since 1.0.0
 * @author _Kron
 * @version 1.0.0
 */
public final class C_Help {

    /** Prevent instantization */
    private C_Help() { }
    /**
     *
     * @param sender the sender
     * @param parent the parent command
     * @param page the page
     * @return 1 always
     */
    public static int execute(@NotNull Audience sender, @NotNull ParentCommand parent, int page) {
        showPage(sender, parent, page);

        return 1;
    }
    /**
     * @param sender the user sending the command
     * @param parent parent command (root)
     * @param query page number or sub-command
     * @return always 1 since command will always execute.
     */
    public static int execute(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull String query) {
        try {
            showPage(sender, parent, Integer.parseInt(query));
            return 1;
        } catch (NumberFormatException ignored) {}

        showEntry(sender, parent, query);

        return 1;
    }

    private static void showEntry(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull String input) {
        Map<HelpID, PaperHelpEntry> entries = CommandConfig.get().getParent(parent);

        String lowered = input.toLowerCase(Locale.ROOT);

        for (PaperHelpEntry entry : entries.values()) {
            if (entry.aliases().contains(lowered)) {
                sendDetailedHelp(sender, entry, parent);
                return;
            }
        }

        MessageHelpers.error(sender, SocialPrefixes.SOCIAL_PREFIX, "error.command.unknown",
                Component.text(": " + input, NamedTextColor.GRAY));
    }

    public static void showEntry(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull HelpID helpID) {
        PaperHelpEntry helpEntry = CommandConfig.get().getEntry(parent, helpID);

        sendDetailedHelp(sender, helpEntry, parent);
    }

    private static void showPage(@NotNull Audience sender, @NotNull ParentCommand parent, int page) {
        List<PaperHelpEntry> entries = CommandConfig.get().getParent(parent).entrySet().stream()
                .filter(entry -> entry.getKey() != HelpID.ROOT)
                .map(Map.Entry::getValue)
                .toList();

        int entriesPerPage = ConfigData.get().getEntriesPerPage();

        int maxPage = (int) Math.ceil(entries.size() / (double) entriesPerPage);

        if (page < 1 || page > maxPage) {
            MessageHelpers.error(sender, PrefixHelpers.CORE_PREFIX, "error.command.page.range",
                    Component.text("1-" + maxPage, NamedTextColor.GRAY));
            return;
        }

        sendHelpPage(sender, parent, entries, page, maxPage);
    }

    private static void sendHelpPage(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull List<PaperHelpEntry> entries, int page, int maxPage) {
        Locale locale = sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale());

        int entriesPerPage = ConfigData.get().getEntriesPerPage();
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, entries.size());
        List<PaperHelpEntry> tempEntries = entries.subList(start, end);

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("help.page.header", NamedTextColor.GOLD, Component.text(page), Component.text(maxPage)));
        sender.sendMessage(Component.empty());
        for (PaperHelpEntry entry : tempEntries) {
            String command = CommandConfig.get().getEntry(parent, HelpID.ROOT).aliases().getFirst();
            String subcommand = entry.aliases().getFirst();

            Component shortDescription = Component.translatable(entry.shortDescriptionKey(), NamedTextColor.GRAY);

            sender.sendMessage(Component.text("🛈 ", NamedTextColor.GRAY)
                    .append(Component.translatable("command.format", NamedTextColor.YELLOW,
                            Component.text(command),
                            Component.text(subcommand)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        showEntry(audience, parent, entry.aliases().getFirst());
                        SoundHelpers.clickSound(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()))
                    .hoverEvent(HoverEvent.showText(shortDescription.color(NamedTextColor.GRAY))));
        }
        sender.sendMessage(Component.empty());
        MessageHelpers.sendPaginationFooter(sender, page, maxPage,
                audience -> sendHelpPage(sender, parent, entries, page - 1, maxPage),
                audience -> sendHelpPage(sender, parent, entries, page + 1, maxPage));
    }

    private static void sendDetailedHelp(@NotNull Audience sender, @NotNull PaperHelpEntry entry, @NotNull ParentCommand parent) {
        Locale locale = sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale());

        String parentCommand = parent.name().substring(0, 1).toUpperCase() + parent.name().toLowerCase().substring(1);
        String helpName = entry.helpID().name().substring(0, 1).toUpperCase() + entry.helpID().name().toLowerCase().substring(1);

        StringBuilder aliases = new StringBuilder(entry.aliases().size() * 6);
        for (String alias : entry.aliases()) {
            aliases.append(alias).append(", ");
        }

        aliases.delete(aliases.length() - 2, aliases.length());

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("help.detailed.header", NamedTextColor.GOLD,
                Component.text(parentCommand),
                Component.text(helpName)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable(entry.formatKey(), NamedTextColor.YELLOW)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.translatable(entry.shortDescriptionKey(), NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("command.aliases.format", NamedTextColor.YELLOW,
                Component.text(aliases.toString(), NamedTextColor.GRAY)));
        sender.sendMessage(Component.empty());

        for (int i = 0; i < entry.detailedHelpKeys().size(); i++) {
            Component message = Component.empty();
            if (i == 0) message = Component.translatable("help.details", NamedTextColor.YELLOW);
            sender.sendMessage(message.append(Component.translatable(entry.detailedHelpKeys().get(i), NamedTextColor.GRAY)));
        }
    }
}