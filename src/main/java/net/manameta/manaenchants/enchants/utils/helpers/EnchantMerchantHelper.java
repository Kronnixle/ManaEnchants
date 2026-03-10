package net.manameta.manaenchants.enchants.utils.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.common.utils.ItemCreator;
import net.manameta.manaenchants.common.utils.ItemEditor;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

public final class EnchantMerchantHelper {

    private EnchantMerchantHelper() {} // Utility class

    private static final List<Material> tools = List.of(
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
            Material.DIAMOND_SWORD, Material.DIAMOND_SPEAR,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.ELYTRA, Material.FISHING_ROD, Material.BOOK, Material.BOW, Material.CROSSBOW,
            Material.MACE, Material.TRIDENT, Material.SHIELD
    );

    @Contract(pure = true)
    public static @NonNull List<MerchantRecipe> buildDefaultRecipes() {
        List<MerchantRecipe> recipes = new ArrayList<>(tools.size());

        for (Material tool : tools) {
            ItemStack toolItem = new ItemStack(tool);
            toolItem.editMeta(meta -> meta.setEnchantmentGlintOverride(true));

            MerchantRecipe recipe = new MerchantRecipe(toolItem, 1);
            recipe.addIngredient(new ItemStack(tool));
            recipes.add(recipe);
        }

        return recipes;
    }

    /**
     * Creates a visual player head showing the player's XP level.
     */
    public static @NonNull ItemStack createPlayerHead(Player player, int cost) {
        return ItemCreator.createHead(player, 1,
                Component.text("XP Info: " + player.getName(), NamedTextColor.YELLOW),
                List.of(Component.empty(),
                        Component.text("XP Level: " + player.getLevel(), NamedTextColor.GRAY),
                        Component.text("XP Level after enchant: " + (player.getLevel() - cost), NamedTextColor.GRAY)));
    }

    /**
     * Creates a visual XP cost item for the merchant GUI.
     */
    public static @NonNull ItemStack createGhostXPItem(int xpCost) {
        return ItemCreator.create(Material.EXPERIENCE_BOTTLE, xpCost,
                Component.translatable("ui.enchanter.cost", NamedTextColor.GREEN),
                List.of(Component.empty(),
                        Component.translatable("ui.enchanter.requires.xp", NamedTextColor.GRAY,
                                Component.text(xpCost, NamedTextColor.YELLOW))));
    }

    /**
     * Creates a ghost version of a tool with the enchantment applied for visual preview.
     */
    public static @NonNull ItemStack createGhostEnchantment(@Nonnull ItemStack toolToUpgrade, @NonNull TieredEnchant enchant) {
        EnchantmentOffer offer = enchant.toEnchantment(toolToUpgrade);

        int level = toolToUpgrade.getEnchantmentLevel(offer.getEnchantment());

        ItemStack ghost = toolToUpgrade.clone();

        ghost.editMeta(meta -> meta.lore(List.of(Component.empty(),
                Component.translatable("ui.enchanter.upgraded.with", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false),
                Component.text("• ", NamedTextColor.LIGHT_PURPLE)
                        .append(offer.getEnchantment().displayName(level + 1))
                        .decoration(TextDecoration.ITALIC, false)
        )));
        return ghost;
    }

    /**
     * Counts bookshelves within a 3-block radius for enchantment power calculations.
     */
    public static int calculateNearbyBookshelves(Location location) {
        int count = 0;
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block block = location.getWorld().getBlockAt(location.clone().add(x, y, z));
                    if (block.getType() == Material.BOOKSHELF && block.getLocation().distance(location) >= 1) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Moves a catalyst from the player's inventory into the merchant slot (slot 1) without exceeding necessary catalyst amount.
     */
    public static void moveCatalystToSlot(@Nonnull HumanEntity player, @Nonnull ItemStack catalystTemplate) {
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        Inventory playerInv = player.getOpenInventory().getBottomInventory();

        ItemStack slotItem = topInventory.getItem(1);
        boolean slotHasItem = slotItem != null && !slotItem.getType().isAir();
        boolean slotCompatible = slotHasItem && slotItem.isSimilar(catalystTemplate);

        int targetAmount = catalystTemplate.getAmount();

        // If slot contains a different item, ensure we can move it before doing anything.
        if (slotHasItem && !slotCompatible) {
            if (playerInv.firstEmpty() == -1) {
                return; // abort safely
            }
        }

        // Prepare result stack
        ItemStack result;
        if (slotCompatible) {
            result = slotItem.clone();
        } else {
            result = catalystTemplate.clone();
            result.setAmount(0);
        }

        int spaceLeft = targetAmount - result.getAmount();
        if (spaceLeft <= 0) {
            return;
        }

        // Track displaced slot item if needed
        ItemStack displaced = (slotHasItem && !slotCompatible) ? slotItem.clone() : null;

        for (int i = 0; i < playerInv.getSize(); i++) {
            ItemStack invItem = playerInv.getItem(i);
            if (invItem == null || invItem.getType().isAir()) continue;
            if (!invItem.isSimilar(catalystTemplate)) continue;

            int move = Math.min(spaceLeft, invItem.getAmount());

            result.setAmount(result.getAmount() + move);

            int remaining = invItem.getAmount() - move;
            if (remaining <= 0) {
                playerInv.setItem(i, null);
            } else {
                invItem.setAmount(remaining);
                playerInv.setItem(i, invItem);
            }

            spaceLeft -= move;
            if (spaceLeft <= 0) break;
        }

        // Move displaced item into inventory
        if (displaced != null) {
            int empty = playerInv.firstEmpty();
            if (empty != -1) {
                playerInv.setItem(empty, displaced);
            }
        }

        if (result.getAmount() > 0) {
            topInventory.setItem(1, result);
        }
    }

    public static @NonNull MerchantRecipe maxedItem(@Nonnull Player player, @Nonnull ItemStack item) {
        Locale locale = player.locale();
        ItemStack clone = item.clone();

        ItemEditor.appendLore(clone,
                Component.empty(),
                LocaleManager.get(locale, "enchant.item.fully.upgraded", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        MerchantRecipe recipe = new MerchantRecipe(clone, 0);
        recipe.addIngredient(item);
        return recipe;
    }
}