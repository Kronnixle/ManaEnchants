package net.manameta.manaenchants.common.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ItemEditor {
    /** Prevent instantization */
    private ItemEditor() {}

    public static void appendLore(@NonNull ItemStack item, Component... components) {
        item.editMeta(meta -> {
            List<Component> lore = Optional.ofNullable(meta.lore()).orElseGet(ArrayList::new);
            lore.addAll(List.of(components));
            meta.lore(lore);
        });
    }
}
