package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.text.Component;
import net.manameta.manaenchants.common.gui.InventoryButton;
import net.manameta.manaenchants.common.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ButtonHelpers {
    public static InventoryButton createPageButton(@NotNull Player player, @NotNull PageType pageType, int page, Consumer<? super Integer> consumer) {
        Locale locale = player.locale();

/*        ItemStack item = switch (pageType) {
            case NEXT -> CommonItems.nextPageItem(locale);
            case PREVIOUS -> CommonItems.previousPageItem(locale);
            default -> CommonItems.pageItem(locale, page);
        };*/

        return new InventoryButton()
            .creator(i -> ItemCreator.create(Material.PAPER, 1, Component.text("Page")))
            .consumer(e -> {
                switch (pageType) {
                    case NEXT -> consumer.accept(page + 1);
                    case PREVIOUS -> consumer.accept(page - 1);
                    case BOTH -> {
                        if (e.isRightClick() && page > 1) consumer.accept(page - 1);
                        else consumer.accept(page + 1);
                    }
                }
            });
    }
}