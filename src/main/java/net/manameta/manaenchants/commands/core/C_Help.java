package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.HelpEntry;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.common.config.CommandConfig;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
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
        Map<HelpID, HelpEntry> entries = CommandConfig.get().getParent(parent);

        String lowered = input.toLowerCase(Locale.ROOT);

        for (HelpEntry entry : entries.values()) {
            if (entry.aliases().contains(lowered)) {
                sendDetailedHelp(sender, entry, parent);
                return;
            }
        }

        MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.error.unknown.input",
                Component.text(input, ConfigData.get().getErrorHighlightColour()));
    }

    public static void showEntry(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull HelpID helpID) {
        HelpEntry helpEntry = CommandConfig.get().getEntry(parent, helpID);

        sendDetailedHelp(sender, helpEntry, parent);
    }

    private static void showPage(@NotNull Audience sender, @NotNull ParentCommand parent, int page) {
        List<HelpEntry> entries = CommandConfig.get().getParent(parent).entrySet().stream()
                .filter(entry -> entry.getKey() != HelpID.ROOT)
                .map(Map.Entry::getValue)
                .toList();

        int entriesPerPage = ConfigData.get().getEntriesPerPage();

        int maxPage = (int) Math.ceil(entries.size() / (double) entriesPerPage);

        if (page < 1 || page > maxPage) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.error.page.range",
                    Component.text("1-" + maxPage, ConfigData.get().getErrorHighlightColour()));
            return;
        }

        sendHelpPage(sender, parent, entries, page, maxPage);
    }

    private static void sendHelpPage(@NotNull Audience sender, @NotNull ParentCommand parent, @NotNull List<HelpEntry> entries, int page, int maxPage) {
        int entriesPerPage = ConfigData.get().getEntriesPerPage();
        int start = (page - 1) * entriesPerPage;
        int end = Math.min(start + entriesPerPage, entries.size());
        List<HelpEntry> tempEntries = entries.subList(start, end);

        ConfigData config = ConfigData.get();

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("help.page.header", config.getHeaderColour(), Component.text(page), Component.text(maxPage)));
        sender.sendMessage(Component.empty());
        for (HelpEntry entry : tempEntries) {
            String command = CommandConfig.get().getEntry(parent, HelpID.ROOT).aliases().getFirst();
            String subcommand = entry.aliases().getFirst();

            Component shortDescription = Component.translatable(entry.shortDescriptionKey(), config.getDescriptionColour());

            sender.sendMessage(Component.text("🛈 ", config.getDescriptionColour())
                    .append(Component.translatable("command.format", config.getDescriptionHighlightColour(),
                            Component.text(command),
                            Component.text(subcommand)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        showEntry(audience, parent, entry.aliases().getFirst());
                        SoundHelpers.clickSound(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()))
                    .hoverEvent(HoverEvent.showText(shortDescription)));
        }
        sender.sendMessage(Component.empty());
        MessageHelpers.sendPaginationFooter(sender, page, maxPage,
                audience -> sendHelpPage(sender, parent, entries, page - 1, maxPage),
                audience -> sendHelpPage(sender, parent, entries, page + 1, maxPage));
    }

    private static void sendDetailedHelp(@NotNull Audience sender, @NotNull HelpEntry entry, @NotNull ParentCommand parent) {
        String parentCommand = parent.name().substring(0, 1).toUpperCase() + parent.name().toLowerCase().substring(1);
        String helpName = entry.helpID().name().substring(0, 1).toUpperCase() + entry.helpID().name().toLowerCase().substring(1);

        StringBuilder aliases = new StringBuilder(entry.aliases().size() * 6);
        for (String alias : entry.aliases()) {
            aliases.append(alias).append(", ");
        }

        aliases.delete(aliases.length() - 2, aliases.length());

        ConfigData config = ConfigData.get();
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("help.detailed.header", config.getHeaderColour(),
                Component.text(parentCommand),
                Component.text(helpName)));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable(entry.formatKey(), config.getDescriptionHighlightColour())
                .append(Component.text(" - ", config.getDescriptionColour()))
                .append(Component.translatable(entry.shortDescriptionKey(), config.getDescriptionColour())));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("command.aliases.format", config.getDescriptionHighlightColour(),
                Component.text(aliases.toString(), config.getDescriptionColour())));
        sender.sendMessage(Component.empty());

        for (int i = 0; i < entry.detailedHelpKeys().size(); i++) {
            Component message = Component.empty();
            if (i == 0) message = Component.translatable("help.details", config.getDescriptionHighlightColour());
            sender.sendMessage(message.append(Component.translatable(entry.detailedHelpKeys().get(i), config.getDescriptionColour())));
        }
    }
}