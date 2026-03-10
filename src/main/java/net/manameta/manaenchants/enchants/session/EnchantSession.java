package net.manameta.manaenchants.enchants.session;

import com.mojang.datafixers.util.Pair;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.enchants.MerchantGUI;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import net.manameta.manaenchants.enchants.utils.helpers.EnchantMerchantHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.view.MerchantView;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantSession {

    private final Player player;
    private final Location location;
    private final MerchantView merchantView;
    private final MerchantGUI merchantGUI;

    private ItemStack toolItem;
    private ItemStack catalystItem;
    private ItemStack recipeToolSnapshot;

    private int pendingXpCost;

    private final Map<Integer, TieredEnchant> enchantMap = new HashMap<>();

    private boolean clean = true;

    EnchantSession(@Nonnull Player player, @Nonnull Location location, @Nonnull MerchantView merchantView, @Nonnull MerchantGUI merchantGUI) {
        this.player = player;
        this.location = location;
        this.merchantView = merchantView;
        this.merchantGUI = merchantGUI;
    }

    /*
     * Initialization
     */
    public void initialize(@Nonnull ItemStack toolItem) {
        this.toolItem = toolItem;
        merchantView.getTopInventory().setItem(0, toolItem);
        rebuildRecipes();
    }

    /*
     * Recipe Construction
     */
    private void rebuildRecipes() {
        enchantMap.clear();

        if (toolItem == null) {
            return;
        }

        recipeToolSnapshot = toolItem.clone();

        int bookshelves = EnchantMerchantHelper.calculateNearbyBookshelves(location);

        List<Pair<MerchantRecipe, TieredEnchant>> entries = new ArrayList<>();

        for (TieredEnchant enchant : TieredEnchantsConfig.get().getSupportedEnchants(toolItem.getType())) {
            MerchantRecipe recipe = enchant.toMerchantRecipe(toolItem.clone(), bookshelves, player);
            if (recipe == null) {
                continue;
            }

            entries.add(Pair.of(recipe, enchant));
        }

        if (!entries.isEmpty()) {
            entries.sort((a, b) -> {
                MerchantRecipe recipeA = a.getFirst();
                MerchantRecipe recipeB = b.getFirst();

                int groupA = recipeGroup(recipeA);
                int groupB = recipeGroup(recipeB);

                if (groupA != groupB) {
                    return Integer.compare(groupA, groupB);
                }

                int amountA = recipeAmount(recipeA);
                int amountB = recipeAmount(recipeB);

                return Integer.compare(amountA, amountB);
            });
        }

        List<MerchantRecipe> recipes = new ArrayList<>();

        int index = 0;
        for (Pair<MerchantRecipe, TieredEnchant> entry : entries) {
            recipes.add(entry.getFirst());
            enchantMap.put(index++, entry.getSecond());
        }

        if (recipes.isEmpty()) {
            if (TieredEnchantsConfig.get().contains(toolItem.getType())) {
                recipes.add(EnchantMerchantHelper.maxedItem(player, toolItem));
            } else {
                recipes = EnchantMerchantHelper.buildDefaultRecipes();
            }
        }

        merchantView.getMerchant().setRecipes(recipes);
    }



    /*
     * Trade Selection
     */
    public void handleTradeSelect(@NonNull TradeSelectEvent event) {
        MerchantRecipe recipe = event.getMerchant().getRecipe(event.getIndex());

        boolean isDefault = recipe.getIngredients().stream().noneMatch(i -> i.getType() == Material.EXPERIENCE_BOTTLE);

        if (isDefault) {
            Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), this::refresh);
            return;
        }

        event.setCancelled(true);

        TieredEnchant enchant = enchantMap.get(event.getIndex());
        if (enchant == null) return;

        Enchantment ench = enchant.resolveEnchantment();
        if (ench == null) {
            throw new RuntimeException("Could not resolve enchantment: " + enchant.key());
        }

        int level = toolItem.getEnchantmentLevel(ench) + 1;

        ItemStack catalystCost = enchant.getCatalystCost(level).getItem();

        EnchantMerchantHelper.moveCatalystToSlot(player, catalystCost);

        syncSlots();

        tryPreview(event.getIndex());
    }

    /*
     * Trade Completion
     */
    public void handleTradeComplete() {
        if (pendingXpCost <= 0) {
            Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), this::refresh);
            return;
        }

        int level = player.getLevel();

        if (level < pendingXpCost) return;

        player.setLevel(level - pendingXpCost);
        pendingXpCost = 0;
        syncSlots();
        markDirty();
    }

    /*
     * Preview Logic
     */
    public boolean tryPreviewFromCursor() {
        syncSlots();
        if (clean) return false;

        if (!hasValidInputs()) return false;

        rebuildRecipes();
        List<MerchantRecipe> recipes = merchantView.getMerchant().getRecipes();

        for (int i = 0; i < recipes.size(); i++) {
            if (tryPreview(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryPreview(int index) {
        if (recipeToolSnapshot == null || !recipeToolSnapshot.isSimilar(toolItem)) {
            refresh();
            return false;
        }

        TieredEnchant enchant = enchantMap.get(index);
        if (enchant == null) {
            return false;
        }

        Enchantment ench = resolveEnchantment(enchant.key());
        if (ench == null) {
            return false;
        }

        int level = toolItem.getEnchantmentLevel(ench) + 1;

        ItemStack catalystCost = enchant.getCatalystCost(level).getItem();

        if (catalystItem == null) {
            return false;
        }

        if (!catalystCost.isSimilar(catalystItem)) {
            return false;
        }

        if (catalystItem.getAmount() < catalystCost.getAmount()) {
            return false;
        }

        pendingXpCost = enchant.getXpCost(level);

        MerchantRecipe recipe = merchantView.getMerchant().getRecipe(index);

        boolean previewRecipe = recipe.getIngredients().stream().noneMatch(i -> i.getType() == Material.EXPERIENCE_BOTTLE);

        if (previewRecipe) {
            ItemStack requiredTool = recipe.getIngredients().getFirst();

            if (toolItem == null) {
                return false;
            }

            if (!requiredTool.isSimilar(toolItem)) {
                return false;
            }
        }

        ItemStack upgradedTool = recipe.getResult().clone();

        showPreview(enchant, catalystCost, upgradedTool);

        return true;
    }

    /*
     * Preview UI
     */
    private void showPreview(@Nonnull TieredEnchant enchant, @NonNull ItemStack catalyst, @NonNull ItemStack upgradedTool) {
        ItemStack playerHead = EnchantMerchantHelper.createPlayerHead(player, pendingXpCost);
        ItemStack xpItem = EnchantMerchantHelper.createGhostXPItem(pendingXpCost);
        ItemStack ghostEnchant = EnchantMerchantHelper.createGhostEnchantment(toolItem, enchant);

        List<MerchantRecipe> previewRecipes = new ArrayList<>(2);

        int maxUses = 1;
        if (player.getLevel() < pendingXpCost) maxUses = 0;

        MerchantRecipe real = new MerchantRecipe(upgradedTool, maxUses);
        real.addIngredient(toolItem.clone());
        real.addIngredient(catalyst.clone());

        MerchantRecipe info = new MerchantRecipe(ghostEnchant, maxUses);
        info.addIngredient(playerHead);
        info.addIngredient(xpItem);

        previewRecipes.add(real);
        previewRecipes.add(info);

        merchantView.getMerchant().setRecipes(previewRecipes);

        reopenPreservingSlots();
        clean = true;
    }

    /*
     * Inventory Handling
     */
    private void reopenPreservingSlots() {
        syncSlots();

        merchantView.setItem(0, null);
        merchantView.setItem(1, null);

        player.openInventory(merchantView);

        ManaEnchants.getGUIManager().reregisterInventory(merchantGUI);

        merchantView.getTopInventory().setItem(0, toolItem);
        merchantView.getTopInventory().setItem(1, catalystItem);
    }

    public void tryRefreshIfStable() {
        if (clean) return;

        ItemStack cursor = player.getItemOnCursor();

        if (cursor.getType() != Material.AIR) return;

        clean = true;

        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), this::refresh);
    }

    /*
     * Refresh Handling
     */
    private void refresh() {
        syncSlots();
        rebuildRecipes();

        reopenPreservingSlots();
        tryPreviewFromCursor();
    }

    /*
     * Enchantment Resolution
     */
    private Enchantment resolveEnchantment(String key) {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(Key.key(key));
    }

    /*
     * Helpers
     */
    public void markDirty() { clean = false; }

    private void syncSlots() {
        toolItem = merchantView.getItem(0);
        catalystItem = merchantView.getItem(1);
    }

    private boolean hasValidInputs() {
        return toolItem != null && catalystItem != null && !toolItem.getType().isAir() && !catalystItem.getType().isAir();
    }

    private int recipeGroup(MerchantRecipe recipe) {
        if (recipe.getMaxUses() == 1) return 0; // valid

        Material type = recipe.getIngredients().getFirst().getType();

        if (type == Material.EXPERIENCE_BOTTLE) return 1; // lacking XP
        if (type == Material.BOOKSHELF) return 2;         // lacking bookshelves

        return 3;
    }

    private int recipeAmount(MerchantRecipe recipe) {
        return recipe.getIngredients().getFirst().getAmount();
    }
}