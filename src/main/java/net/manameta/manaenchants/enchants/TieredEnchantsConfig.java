package net.manameta.manaenchants.enchants;

import com.destroystokyo.paper.MaterialTags;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.enchants.costs.bookshelves.*;
import net.manameta.manaenchants.enchants.costs.xp.XPRequirement;
import net.manameta.manaenchants.enchants.model.Rarity;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import net.manameta.manaenchants.enchants.model.Visibility;
import net.manameta.manaenchants.enchants.costs.CostModel;
import net.manameta.manaenchants.enchants.costs.FormulaCostModel;
import net.manameta.manaenchants.enchants.costs.PerLevelCost;
import net.manameta.manaenchants.enchants.costs.PerLevelCostModel;
import net.manameta.manaenchants.enchants.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.costs.catalyst.CatalystFormula;
import net.manameta.manaenchants.enchants.costs.catalyst.DefaultCatalystFormula;
import net.manameta.manaenchants.enchants.costs.xp.XPFormula;
import net.manameta.manaenchants.items.SavedItems;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public final class TieredEnchantsConfig {
    @Nonnull
    private final Map<String, TieredEnchant> enchants = new HashMap<>();
    private final Map<Material, Set<TieredEnchant>> materialToEnchants = new EnumMap<>(Material.class);

    public Set<TieredEnchant> getSupportedEnchants(@Nonnull Material material) {
        return materialToEnchants.getOrDefault(material, Collections.emptySet());
    }

    public boolean contains(@Nonnull Material material) {
        return materialToEnchants.containsKey(material);
    }

    private TieredEnchantsConfig() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "tiered_enchants.yml");

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("enchants")) {
            ManaEnchants.getInstance().saveResource("tiered_enchants.yml", true);
        }

        ConfigurationSection enchantsSection = config.getConfigurationSection("enchants");

        assert enchantsSection != null;
        for (String key : enchantsSection.getKeys(false)) {
            ConfigurationSection enchantSection = enchantsSection.getConfigurationSection(key);

            assert enchantSection != null;
            boolean enabled = enchantSection.getBoolean("enabled", true);
            String rarityString = enchantSection.getString("rarity", "COMMON");
            Rarity rarity = Rarity.valueOf(rarityString.toUpperCase());

            String visibilityMode = enchantSection.getString("visibility.mode", "visible");
            Visibility visibility = Visibility.valueOf(visibilityMode.toUpperCase());

            List<String> appliesToStrList = enchantSection.getStringList("applies_to");
            int maxLevel = enchantSection.getInt("max_level", 1);
            Set<String> incompatibilities = new HashSet<>(enchantSection.getStringList("incompatibilities"));

            ConfigurationSection costsSection = enchantSection.getConfigurationSection("costs");
            if (costsSection == null) {
                ManaEnchants.getInstance().getLogger().warning("Costs section missing for enchant " + key);
                continue;
            }

            CostModel costModel = parseCostModel(costsSection);
            if (costModel == null) continue;

            Set<Material> appliesTo = parsedList(appliesToStrList);

            TieredEnchant enchant = new TieredEnchant(key, enabled, rarity, visibility, appliesTo, maxLevel, incompatibilities, costModel);

            enchants.put(key.toLowerCase(), enchant);
        }

        for (TieredEnchant enchant : enchants.values()) {
            for (Material mat : enchant.appliesTo()) {
                materialToEnchants.computeIfAbsent(mat, k -> new HashSet<>()).add(enchant);
            }
        }
    }

    @Nonnull
    private Set<Material> parsedList(@NonNull Iterable<String> rawList) {
        Set<Material> materials = EnumSet.noneOf(Material.class);

        for (String key : rawList) {
            switch (key.toLowerCase(Locale.ROOT)) {
                case "swords", "sword" -> materials.addAll(Tag.ITEMS_SWORDS.getValues());
                case "axe", "axes" -> materials.addAll(Tag.ITEMS_AXES.getValues());
                case "fishing_rods", "rods" -> materials.addAll(Tag.ITEMS_ENCHANTABLE_FISHING.getValues());
                case "pickaxe", "pickaxes" -> materials.addAll(Tag.ITEMS_PICKAXES.getValues());
                case "shovel", "shovels" -> materials.addAll(Tag.ITEMS_SHOVELS.getValues());
                case "bows" -> materials.addAll(MaterialTags.BOWS.getValues());
                case "hoe", "hoes" -> materials.addAll(Tag.ITEMS_HOES.getValues());
                case "helmet", "head", "helmets" -> materials.addAll(Tag.ITEMS_HEAD_ARMOR.getValues());
                case "chestplate", "chestplates" -> materials.addAll(Tag.ITEMS_CHEST_ARMOR.getValues());
                case "leggings", "legs" -> materials.addAll(Tag.ITEMS_LEG_ARMOR.getValues());
                case "boots", "feet" -> materials.addAll(Tag.ITEMS_FOOT_ARMOR.getValues());
                case "spear", "spears" -> materials.addAll(Tag.ITEMS_SPEARS.getValues());
                default -> {
                    Material material = Material.matchMaterial(key);
                    if (material != null) {
                        materials.add(material);
                    } else {
                        ManaEnchants.getInstance().getLogger().warning("Invalid material: " + key);
                    }
                }
            }
        }

        return materials;
    }

    @Nullable
    private CostModel parseCostModel(@Nonnull ConfigurationSection costsSection) {
        String mode = costsSection.getString("mode", "PER_LEVEL").toUpperCase();

        // PER_LEVEL
        if (mode.equalsIgnoreCase("PER_LEVEL")) {
            Map<Integer, PerLevelCost> levels = new HashMap<>();
            ConfigurationSection levelsSection = costsSection.getConfigurationSection("levels");
            if (levelsSection == null) {
                ManaEnchants.getInstance().getLogger().warning("PER_LEVEL mode missing 'levels' section at: " + costsSection.getCurrentPath());
                return null;
            }

            for (String levelKey : levelsSection.getKeys(false)) {
                int level = Integer.parseInt(levelKey);

                ConfigurationSection levelSection = levelsSection.getConfigurationSection(levelKey);
                if (levelSection == null) {
                    ManaEnchants.getInstance().getLogger().warning("PER_LEVEL level section missing for level: " + level + " at: " + levelsSection.getCurrentPath());
                    return null;
                }

                ConfigurationSection catalystSection = levelSection.getConfigurationSection("catalyst");
                if (catalystSection == null) {
                    ManaEnchants.getInstance().getLogger().warning("PER_LEVEL catalyst section missing for level: " + level + " at: " +
                            levelsSection.getCurrentPath());
                    return null;
                }

                String materialKey = catalystSection.getString("material");
                String itemKey = catalystSection.getString("item");
                int amount = catalystSection.getInt("amount");

                CatalystCost catalystCost;

                if (itemKey != null) {
                    ItemStack savedItem = SavedItems.get().get(itemKey);
                    if (savedItem == null) {
                        ManaEnchants.getInstance().getLogger().warning("PER_LEVEL saved item missing: " + itemKey);
                        return null;
                    }

                    if (amount > savedItem.getMaxStackSize()) {
                        ManaEnchants.getInstance().getLogger().warning("Catalyst cost for " + savedItem.getType() + " (" + amount +") exceeds stack size amount!");
                        return null;
                    }

                    catalystCost = new CatalystCost(itemKey, amount);
                } else {

                    Material material = Material.matchMaterial(materialKey == null ? "LAPIS_LAZULI" : materialKey);

                    if (material == null) {
                        ManaEnchants.getInstance().getLogger().warning("Invalid material: " + materialKey + " at: " +
                                levelsSection.getCurrentPath());
                        return null;
                    }

                    if (amount > material.getMaxStackSize()) {
                        ManaEnchants.getInstance().getLogger().warning("Catalyst cost for " + material + " (" + amount +") exceeds stack size amount!");
                        return null;
                    }

                    catalystCost = new CatalystCost(material, amount);
                }

                int xpBase = levelSection.getInt("xp_base", 0);
                int xp = levelSection.getInt("xp", 1);
                int bookshelf = levelSection.getInt("bookshelf", 0);

                levels.put(level, new PerLevelCost(xpBase, catalystCost, xp, bookshelf));
            }
            return new PerLevelCostModel(levels);
        }
        // FORMULA
        else {
            ConfigurationSection catalystSection = costsSection.getConfigurationSection("catalyst");
            if (catalystSection == null) {
                ManaEnchants.getInstance().getLogger().warning("FORMULA mode missing 'catalyst' section at: " + costsSection.getCurrentPath());
                return null;
            }

            ConfigurationSection defaultSection = catalystSection.getConfigurationSection("default");
            if (defaultSection == null) {
                ManaEnchants.getInstance().getLogger().warning("FORMULA mode missing 'default' catalyst: " + catalystSection.getCurrentPath());
                return null;
            }

            // Catalyst formula
            CatalystFormula catalyst;
            {
                int base = defaultSection.getInt("base");
                int perLevel = defaultSection.getInt("per_level");
                double multiplier = defaultSection.getDouble("multiplier", 1.0);

                CatalystCost defaultCost = parseCatalyst(defaultSection, 1);
                if (defaultCost == null) return null;

                Map<Integer, CatalystCost> overrides = new HashMap<>();

                ConfigurationSection overridesSection = catalystSection.getConfigurationSection("overrides");

                if (overridesSection != null) {
                    for (String lvl : overridesSection.getKeys(false)) {

                        int level = Integer.parseInt(lvl);
                        ConfigurationSection o = overridesSection.getConfigurationSection(lvl);
                        if (o == null) continue;

                        int amount = o.getInt("amount", 1);

                        CatalystCost cost = parseCatalyst(o, amount);
                        if (cost == null) continue;

                        overrides.put(level, cost);
                    }
                }

                catalyst = new DefaultCatalystFormula(defaultCost, base, perLevel, multiplier, overrides);
            }

            // XP
            XPRequirement xpRequirement;
            XPFormula xpFormula;
            {
                ConfigurationSection xpSection = costsSection.getConfigurationSection("xp");
                if (xpSection == null) {
                    ManaEnchants.getInstance().getLogger().warning("FORMULA mode missing 'xp' section at: " + costsSection.getCurrentPath());
                    return null;
                }

                // Expression string for XP cost calculation
                String formulaExpression = xpSection.getString("formula", "2 + 2 * level");

                // Required XP levels for unlocking
                int requiredBaseLevel = xpSection.getInt("required_base_level", 0);
                int requiredPerLevel = xpSection.getInt("required_per_level", 0);

                xpRequirement = new XPRequirement(requiredBaseLevel, requiredPerLevel);

                try {
                    xpFormula = new XPFormula(formulaExpression);
                } catch (IllegalArgumentException ex) {
                    ManaEnchants.getInstance().getLogger().warning("Invalid XP formula expression '" + formulaExpression + "' at: " + xpSection.getCurrentPath());
                    return null;
                }
            }

            // Bookshelf requirement
            BookshelfRequirement shelfReq;
            {
                ConfigurationSection shelfSection = costsSection.getConfigurationSection("bookshelf");
                if (shelfSection == null) {
                    ManaEnchants.getInstance().getLogger().warning("FORMULA mode missing 'bookshelf' section at: " + costsSection.getCurrentPath());
                    return null;
                }

                String shelfModeStr = shelfSection.getString("mode", "REQUIRED").toUpperCase();
                BookshelfMode shelfMode = BookshelfMode.valueOf(shelfModeStr);

                if (shelfMode == BookshelfMode.REQUIRED) {
                    ConfigurationSection req = shelfSection.getConfigurationSection("required");
                    if (req == null) {
                        ManaEnchants.getInstance().getLogger().warning("Required bookshelf section missing at: " + shelfSection.getCurrentPath());
                        return null;
                    }

                    shelfReq = new RequiredBookshelf(req.getInt("base"), req.getInt("per_level"));
                }
                else if (shelfMode == BookshelfMode.TIERED) {
                    Map<Integer, Integer> tiers = new HashMap<>();
                    ConfigurationSection tierSection = shelfSection.getConfigurationSection("tiers");
                    if (tierSection == null) {
                        ManaEnchants.getInstance().getLogger().warning("Tiered bookshelf section missing at: " + shelfSection.getCurrentPath());
                        return null;
                    }

                    for (String lvl : tierSection.getKeys(false)) {
                        int level = Integer.parseInt(lvl);
                        int shelves = tierSection.getInt(lvl);
                        tiers.put(level, shelves);
                    }
                    shelfReq = new TieredBookshelf(tiers);
                } else {
                    shelfReq = new IgnoredBookshelf();
                }
            }

            return new FormulaCostModel(xpFormula, xpRequirement, catalyst, shelfReq);
        }
    }

    @Nullable
    private CatalystCost parseCatalyst(@NonNull ConfigurationSection section, int amount) {

        String itemKey = section.getString("item");
        String materialKey = section.getString("material");

        if (itemKey != null) {

            if (SavedItems.get().get(itemKey) == null) {
                ManaEnchants.getInstance().getLogger().warning("Saved item missing: " + itemKey);
                return null;
            }

            return new CatalystCost(itemKey, amount);
        }

        Material material = Material.matchMaterial(materialKey == null ? "EMERALD" : materialKey);

        if (material == null) {
            ManaEnchants.getInstance().getLogger().warning("Invalid material: " + materialKey);
            return null;
        }

        return new CatalystCost(material, amount);
    }

    @NonNull
    @UnmodifiableView
    @Contract(pure = true)
    public Map<String, TieredEnchant> getEnchants() {
        return Collections.unmodifiableMap(enchants);
    }

    @Nullable
    public TieredEnchant getEnchant(@Nonnull String enchantName) {
        return enchants.get(enchantName.toLowerCase(Locale.ROOT));
    }

    private static class InstanceHolder { private static TieredEnchantsConfig instance = new TieredEnchantsConfig(); }
    public static TieredEnchantsConfig get() { return InstanceHolder.instance; }
    public static void reload() { InstanceHolder.instance = new TieredEnchantsConfig(); }
}