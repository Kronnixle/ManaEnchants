package net.manameta.manaenchants.enchants.model;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.common.utils.ManaLogger;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.utils.costs.CostModel;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Objects;

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

    public @Nullable MerchantRecipe toMerchantRecipe(@Nonnull ItemStack item, int bookshelves, @Nonnull Player player) {
        Enchantment enchantment = resolveEnchantment();

        if (enchantment == null) {
            ManaLogger.warning("enchants.error.key", key);
            return null;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);

        if (currentLevel >= maxLevel) return null;

        if (!isCompatible(item)) return null;


        int nextLevel = currentLevel + 1;

        int xpCost = costModel.getXpCost(nextLevel);
        int playerLevel = player.getLevel();

        ItemStack resultItem = item.clone();

        resultItem.addUnsafeEnchantment(enchantment, nextLevel);


        int bookshelfRequirement = costModel.getBookshelfRequirement(currentLevel);

        if (bookshelves < bookshelfRequirement) {
            MerchantRecipe recipe = new MerchantRecipe(resultItem.clone(), 0);

            recipe.addIngredient(ItemStack.of(Material.BOOKSHELF, bookshelfRequirement));

            return recipe;
        }

        int maxUses = playerLevel >= xpCost ? 1 : 0;

        CatalystCost catalystCost = costModel.getCatalyst(nextLevel);

        MerchantRecipe recipe = new MerchantRecipe(resultItem.clone(), maxUses);

        recipe.setIngredients(List.of(experienceItem(xpCost, playerLevel), catalystCost.getItem()));

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

    private @NonNull ItemStack experienceItem(int xpCost, int playerLevel) {

        ItemStack item = ItemStack.of(Material.EXPERIENCE_BOTTLE, xpCost);
        NamedTextColor color = xpCost <= playerLevel ? NamedTextColor.GREEN : NamedTextColor.RED;

        item.editMeta(meta -> {
            meta.itemName(Component.text("Level cost: ", NamedTextColor.YELLOW).append(Component.text(xpCost, color)));
            meta.lore(List.of(Component.empty(),
                Component.text("Player level: ", NamedTextColor.YELLOW)
                        .append(Component.text(playerLevel, NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)));
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