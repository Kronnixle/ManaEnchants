package net.manameta.manaenchants.common.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.ManaEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemCreator {
    private static @NotNull ItemStack create(@NotNull Material material, int amount, @NotNull Component itemName) {
        ItemStack item = new ItemStack(material, amount);
        item.editMeta(meta -> meta.displayName(itemName.decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    public static @NotNull ItemStack create(@NotNull Material material, int amount, @NotNull Component itemName,
                                            @NotNull Collection<? extends Component> lore) {
        ItemStack item = create(material, amount, itemName);
        item.editMeta(meta -> {
            List<Component> newLore = new ArrayList<>(lore.size());
            for (Component l : lore) {
                newLore.add(l.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(newLore);
        });
        return item;
    }

    public static @NotNull ItemStack createHead(@NotNull OfflinePlayer player, int amount,
                @NotNull Component itemName, @NotNull Collection<? extends Component> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount);

        head.editMeta(SkullMeta.class, meta -> {
            meta.setPlayerProfile(player.getPlayerProfile());
            meta.displayName(itemName.decoration(TextDecoration.ITALIC, false));
            List<Component> newLore = new ArrayList<>(lore.size());
            for (Component l : lore) newLore.add(l.decoration(TextDecoration.ITALIC, false));
            meta.lore(newLore);
        });

        // Async texture fetch + late update
        PlayerProfile profile = player.getPlayerProfile();
        profile.update().whenComplete((updatedProfile, ex) ->
                Bukkit.getScheduler().runTask(ManaEnchants.getInstance(),
                () -> head.editMeta(SkullMeta.class, skullMeta -> skullMeta.setPlayerProfile(updatedProfile))));

        return head;
    }
}