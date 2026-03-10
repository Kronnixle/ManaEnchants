package net.manameta.manaenchants.commands.enchant;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class C_EnchantList {

    /** Prevent instantization */
    private C_EnchantList() {}

    static int execute(@Nonnull Audience sender, int page) {
        int perPage = ConfigData.get().getEntriesPerPage();

        Set<String> enchantments = TieredEnchantsConfig.get().getEnchants().keySet();
        int maxPage = enchantments.size() / perPage;

        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        int startIndex = (page - 1) * perPage;
        int endIndex = Math.min(startIndex + perPage, enchantments.size());

        List<String> sortedEnchants = new ArrayList<>(enchantments);
        sortedEnchants.sort(String.CASE_INSENSITIVE_ORDER);

        // Header
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("commands.enchant.list.header", NamedTextColor.GOLD,
                Component.text(page, NamedTextColor.YELLOW),
                Component.text(maxPage, NamedTextColor.YELLOW)));
        sender.sendMessage(Component.empty());

        for (int i = startIndex; i < endIndex; i++) {
            String enchantment = sortedEnchants.get(i);
            sender.sendMessage(buildEnchantLine(enchantment));
        }
        sender.sendMessage(Component.empty());
        int finalPage = page;
        MessageHelpers.sendPaginationFooter(sender, page, maxPage,
                audience -> execute(sender, finalPage - 1),
                audience -> execute(sender, finalPage + 1));

        return 1;
    }

    private static @NonNull Component buildEnchantLine(String enchantment) {
        return Component.text(" - ", NamedTextColor.GRAY).append(Component.text(enchantment, NamedTextColor.YELLOW))
                .hoverEvent(HoverEvent.showText(Component.translatable("text.click.details", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> C_EnchantInfo.execute(audience, enchantment),
                            ClickCallback.Options.builder().uses(-1).build()));
    }
}