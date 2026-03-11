package net.manameta.manaenchants.enchants.model;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.costs.CostModel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @param key                 "efficiency"
 * @param appliesTo           Your own abstraction, not raw Material
 * @param maxLevel            null = infinite
 * @param incompatibilityKeys linked post-load
 */
public record TieredEnchant(String key, boolean enabled, Rarity rarity, Visibility visibility, Set<Material> appliesTo, Integer maxLevel,
                            Set<String> incompatibilityKeys, CostModel costModel) {

    public TieredEnchant(@Nonnull String key, boolean enabled, @Nonnull Rarity rarity, @Nonnull Visibility visibility,
                         @Nonnull Set<Material> appliesTo, @Nonnull Integer maxLevel, @Nonnull Set<String> incompatibilityKeys, @Nonnull CostModel costModel) {
        this.key = Objects.requireNonNull(key);
        this.enabled = enabled;
        this.rarity = Objects.requireNonNull(rarity);
        this.visibility = Objects.requireNonNull(visibility);
        this.appliesTo = Collections.unmodifiableSet(appliesTo);
        this.maxLevel = maxLevel;
        this.incompatibilityKeys = Collections.unmodifiableSet(incompatibilityKeys);
        this.costModel = Objects.requireNonNull(costModel);
    }

    @Nullable
    public MerchantRecipe toMerchantRecipe(@Nonnull ItemStack item, int bookshelves, @Nonnull Player player) {
        Enchantment enchantment = resolveEnchantment();

        if (enchantment == null) {
            Bukkit.getLogger().warning("Could not find the enchantment with the key " + key);
            return null;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);

        if (currentLevel >= maxLevel) return null;

        if (!isCompatible(item)) return null;

        int nextLevel = currentLevel + 1;

        // Get both values from cost model
        int xpRequired = costModel.getXPRequirementCost(nextLevel);     // levels required to unlock
        int xpCost = costModel.getXpCost(nextLevel);                    // levels consumed to apply

        int playerLevel = player.getLevel();
        int bookshelfRequirement = costModel.getBookshelfRequirement(currentLevel);
        CatalystCost catalystCost = costModel.getCatalyst(nextLevel);

        // Clone and enchant result
        ItemStack resultItem = item.clone();
        resultItem.addUnsafeEnchantment(enchantment, nextLevel);

        // If not enough bookshelves, show a recipe with crossed-out ingredient
        if (bookshelves < bookshelfRequirement) {
            MerchantRecipe recipe = new MerchantRecipe(resultItem.clone(), 0);
            recipe.addIngredient(ItemStack.of(Material.BOOKSHELF, bookshelfRequirement));
            return recipe;
        }

        // Determine if player meets XP requirements
        int maxUses = (playerLevel >= xpRequired && playerLevel >= xpCost) ? 1 : 0;

        MerchantRecipe recipe = new MerchantRecipe(resultItem.clone(), maxUses);

        // Pass both XP values into experienceItem so we can show them in lore
        ItemStack xpItem = experienceItem(player.locale(), xpRequired, xpCost, playerLevel);

        recipe.setIngredients(List.of(xpItem, catalystCost.getItem()));

        return recipe;
    }

    public @Nonnull EnchantmentOffer toEnchantment(@Nonnull ItemStack item) {

        Enchantment enchantment = resolveEnchantment();
        if (enchantment == null) {
            throw new RuntimeException("Could not find enchantment registry key for: " + key);
        }

        int level = item.getEnchantmentLevel(enchantment);
        int xpCost = costModel.getXpCost(level + 1);

        return new EnchantmentOffer(enchantment, level + 1, xpCost);
    }

    public @Nullable Enchantment resolveEnchantment() {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(Key.key(key));
    }

    private boolean isCompatible(@Nonnull ItemStack item) {
        for (Enchantment enchantment : item.getEnchantments().keySet()) {
            if (!isCompatibleWith(enchantment.key().asMinimalString())) {
                return false;
            }
        }
        return true;
    }

    private boolean isCompatibleWith(@Nonnull String other) {
        for (String incompatible : incompatibilityKeys) {
            if (incompatible.equalsIgnoreCase(other)) return false;
        }
        return true;
    }

    private @NonNull ItemStack experienceItem(@Nonnull Locale locale, int xpRequired, int xpCost, int playerLevel) {

        ItemStack item;
        TextColor color;

        if (xpRequired > playerLevel) {
            // Player doesn't meet the unlock requirement
            item = ItemStack.of(Material.GLASS_BOTTLE, xpRequired);
            color = ConfigData.get().getErrorColour(); // always red
        } else {
            if (xpCost > playerLevel) {
                // Not enough XP to pay the cost
                item = ItemStack.of(Material.GLASS_BOTTLE, xpCost);
                color = ConfigData.get().getErrorColour();
            } else {
                // Enough XP, normal experience bottle
                item = ItemStack.of(Material.EXPERIENCE_BOTTLE, xpCost);
                color = ConfigData.get().getSuccessColour();
            }
        }

        TextColor reqColor = xpRequired <= playerLevel ? ConfigData.get().getSuccessColour() : ConfigData.get().getErrorColour();

        item.editMeta(meta -> {
            // Always show XP cost in the name if player meets unlock requirement, else just show required
            if (xpRequired > playerLevel) {
                meta.itemName(LocaleManager.get(locale, "ui.xp.required.level", ConfigData.get().getDescriptionHighlightColour(),
                        Component.text(xpRequired, reqColor)));
            } else {
                meta.itemName(LocaleManager.get(locale, "ui.xp.level.cost", ConfigData.get().getDescriptionHighlightColour(),
                        Component.text(xpCost, color)));
            }

            // Lore always shows player level and required level
            meta.lore(List.of(
                    Component.empty(),
                    LocaleManager.get(locale, "ui.xp.player.level", ConfigData.get().getDescriptionHighlightColour(),
                            Component.text(playerLevel, ConfigData.get().getDescriptionColour()))
            ));
        });

        return item;
    }

    // ------------------------------------------------
    // Cost Delegation
    // ------------------------------------------------

    public int getXpCost(int level) {
        return costModel.getXpCost(level);
    }

    public CatalystCost getCatalystCost(int level) {
        return costModel.getCatalyst(level);
    }

    // ------------------------------------------------
    // Equality
    // ------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TieredEnchant that)) return false;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}