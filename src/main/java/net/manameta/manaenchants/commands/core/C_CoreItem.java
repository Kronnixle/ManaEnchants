package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.items.SavedItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

final class C_CoreItem {
    /** Prevent instantization */
    private C_CoreItem() {}

    static int execute(@Nonnull Audience sender, @Nonnull String arg, @Nullable String itemName) {

        switch (arg.toLowerCase()) {
            case "add" -> {
                if (itemName == null) {
                    MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.missing.name");
                    return 0;
                }

                return addItem(sender, itemName);
            }
            case "remove" -> {
                if (itemName == null) {
                    MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.missing.name");
                    return 0;
                }

                return removeItem(sender, itemName);
            }
            case "list" -> {
                listItems(sender);
                return 1;
            }
            case "replace", "modify" -> {
                if (itemName == null) {
                    MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.missing.name");
                    return 0;
                }

                return replaceItem(sender, itemName);
            }
            case "info", "i", "show" -> {
                if (itemName == null) {
                    MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.missing.name");
                    return 0;
                }

                itemInfo(sender, itemName);
            }
            case "give" -> {
                if (itemName == null) {
                    MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.missing.name");
                    return 0;
                }

                return giveItem(sender, itemName);
            }
            case "clear" -> {
                return clear(sender);
            }
        }
        return 1;
    }

    static int execute(@Nonnull Audience sender) {
        MessageHelpers.formatError(sender, ParentCommand.MANAENCHANTS, HelpID.ITEM);
        return 0;
    }

    private static int addItem(@Nonnull Audience sender, @Nonnull String itemName) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();
        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender,  ConfigData.get().getCorePrefix(), "commands.core.item.missing");
            return 0;
        }

        if (SavedItems.get().get(itemName) != null) {
            MessageHelpers.error(sender,  ConfigData.get().getCorePrefix(), "commands.core.item.exists",
                    Component.text(itemName, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        SavedItems.get().add(itemName, item);
        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.added",
                Component.text(itemName, ConfigData.get().getSuccessHighlightColour())
                .clickEvent(ClickEvent.callback(audience -> itemInfo(sender, itemName)))
                .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));

        return 1;
    }

    private static int removeItem(@Nonnull Audience sender, @Nonnull String itemName) {
        ItemStack item = SavedItems.get().get(itemName);
        if (item == null) {
            MessageHelpers.error(sender,  ConfigData.get().getCorePrefix(), "commands.core.item.doesnt.exist",
                    Component.text(itemName, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        SavedItems.get().remove(itemName);
        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.removed",
                Component.text(itemName, ConfigData.get().getSuccessHighlightColour())
                        .clickEvent(ClickEvent.callback(audience -> itemInfo(sender, itemName)))
                        .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));

        return 1;
    }

    private static int replaceItem(@Nonnull Audience sender, @Nonnull String itemName) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();
        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender,  ConfigData.get().getCorePrefix(), "commands.core.item.missing");
            return 0;
        }

        if (SavedItems.get().get(itemName) == null) {
            MessageHelpers.error(sender,  ConfigData.get().getCorePrefix(), "commands.core.item.doesnt.exist",
                    Component.text(itemName, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        SavedItems.get().add(itemName, item);
        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.replaced",
                Component.text(itemName, ConfigData.get().getSuccessHighlightColour())
                        .clickEvent(ClickEvent.callback(audience -> itemInfo(sender, itemName)))
                        .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));

        return 1;
    }

    private static void listItems(@NotNull Audience sender) {
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("commands.core.item.list.header", ConfigData.get().getHeaderColour()));
        sender.sendMessage(Component.empty());

        Map<String, ItemStack> items = SavedItems.get().getItems();

        for (var entry : items.entrySet()) {
            String itemName = entry.getKey();
            ItemStack item = entry.getValue();

            Component click = Component.translatable("text.click.here", ConfigData.get().getDescriptionColour())
                    .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value()))
                    .clickEvent(ClickEvent.callback(audience -> itemInfo(sender, itemName)));

            sender.sendMessage(Component.translatable("commands.core.item.entry", ConfigData.get().getDescriptionHighlightColour(),
                    Component.text(itemName, ConfigData.get().getDescriptionColour()),
                    click));
        }
    }

    private static void itemInfo(@Nonnull Audience sender, @Nonnull String itemName) {
        ItemStack item = SavedItems.get().get(itemName);
        if (item == null) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.doesnt.exist",
                    Component.text(itemName, ConfigData.get().getErrorHighlightColour()));
            return;
        }

        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.info",
                Component.text(itemName, ConfigData.get().getSuccessHighlightColour())
                .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));
    }

    private static int giveItem(@Nonnull Audience sender, @Nonnull String itemName) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.error.player.command");
            return 0;
        }

        ItemStack item = SavedItems.get().get(itemName);
        if (item == null || item.getAmount() == 0) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.doesnt.exist",
                    Component.text(itemName, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        int empty = player.getInventory().firstEmpty();
        if (empty == -1) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.no.empty");
            return 0;
        }

        player.give(item);

        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.received",
                Component.text(itemName, ConfigData.get().getSuccessHighlightColour())
                        .clickEvent(ClickEvent.callback(audience -> itemInfo(sender, itemName)))
                        .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value())));

        return 1;
    }

    private static int clear(@Nonnull Audience sender) {
        if (SavedItems.get().getItems().isEmpty()) {
            MessageHelpers.error(sender, ConfigData.get().getCorePrefix(), "commands.core.item.already.empty");
            return 0;
        }

        SavedItems.get().clear();

        MessageHelpers.success(sender, ConfigData.get().getCorePrefix(), "commands.core.item.cleared");
        return 1;
    }
}