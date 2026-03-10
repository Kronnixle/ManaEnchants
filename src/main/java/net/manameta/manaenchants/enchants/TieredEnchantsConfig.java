package net.manameta.manaenchants.enchants;

import com.destroystokyo.paper.MaterialTags;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.utils.ManaLogger;
import net.manameta.manaenchants.enchants.model.Rarity;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import net.manameta.manaenchants.enchants.model.Visibility;
import net.manameta.manaenchants.enchants.utils.costs.CostModel;
import net.manameta.manaenchants.enchants.utils.costs.FormulaCostModel;
import net.manameta.manaenchants.enchants.utils.costs.PerLevelCost;
import net.manameta.manaenchants.enchants.utils.costs.PerLevelCostModel;
import net.manameta.manaenchants.enchants.utils.costs.bookshelves.*;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystCost;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.CatalystFormula;
import net.manameta.manaenchants.enchants.utils.costs.catalyst.DefaultCatalystFormula;
import net.manameta.manaenchants.enchants.utils.costs.xp.XPFormula;
import net.manameta.manaenchants.enchants.utils.costs.xp.XPFormulaType;
import net.manameta.manaenchants.enchants.utils.costs.xp.XPFormulas;
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
    private final @Nonnull Map<String, TieredEnchant> enchants = new HashMap<>();
    private final Map<Material, Set<TieredEnchant>> materialToEnchants = new EnumMap<>(Material.class);

    public Set<TieredEnchant> getSupportedEnchants(@Nonnull Material material) {
        return materialToEnchants.getOrDefault(material, Collections.emptySet());
    }

    public boolean contains(@Nonnull Material material) {
        return materialToEnchants.containsKey(material);
    }

    private TieredEnchantsConfig() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "tiered_enchants.yml");
        ManaLogger.config("config.enchants.file.loading", file.getPath());

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("enchants")) {
            ManaLogger.warning("config.enchants.missing", file.getPath());
            ManaEnchants.getInstance().saveResource("tiered_enchants.yml", true);
        }

        ConfigurationSection enchantsSection = config.getConfigurationSection("enchants");
        if (enchantsSection == null) {
            ManaLogger.warning("config.enchants.section.missing", file.getPath());
            return;
        }

        ManaLogger.config("config.enchants.loading.count", enchantsSection.getKeys(false).size());

        for (String key : enchantsSection.getKeys(false)) {
            ConfigurationSection enchantSection = enchantsSection.getConfigurationSection(key);
            if (enchantSection == null) {
                ManaLogger.warning("config.enchant.section.missing", key);
                continue;
            }

            boolean enabled = enchantSection.getBoolean("enabled", true);
            String rarityString = enchantSection.getString("rarity", "COMMON");
            Rarity rarity = Rarity.valueOf(rarityString.toUpperCase());

            String visibilityMode = enchantSection.getString("visibility.mode", "visible");
            Visibility visibility = Visibility.valueOf(visibilityMode.toUpperCase());

            List<String> appliesToStrList = enchantSection.getStringList("applies_to");
            int maxLevel = enchantSection.getInt("max_level", 1);
            Set<String> incompatibilities = new HashSet<>(enchantSection.getStringList("incompatibilities"));

            ManaLogger.config("config.enchant.basic", key, enabled ? "enabled" : "disabled", rarity.name(), visibility.name(), maxLevel);

            ConfigurationSection costsSection = enchantSection.getConfigurationSection("costs");
            if (costsSection == null) {
                ManaLogger.warning("config.enchant.costs.missing", key);
                continue;
            }

            CostModel costModel = parseCostModel(costsSection);

            Set<Material> appliesTo = parsedList(appliesToStrList);
            ManaLogger.finer("config.enchant.applies.to", key, appliesTo.toString());

            TieredEnchant enchant = new TieredEnchant(key, enabled, rarity, visibility, appliesTo, maxLevel, incompatibilities, costModel);

            enchants.put(key.toLowerCase(), enchant);
            ManaLogger.config("config.enchant.loaded", key);
        }

        for (TieredEnchant enchant : enchants.values()) {
            for (Material mat : enchant.appliesTo()) {
                materialToEnchants.computeIfAbsent(mat, k -> new HashSet<>()).add(enchant);
            }
        }

        ManaLogger.config("config.enchants.complete", enchants.size());
    }


    private @NonNull Set<Material> parsedList(@NonNull Iterable<String> rawList) {
        Set<Material> materials = EnumSet.noneOf(Material.class);

        for (String key : rawList) {
            switch (key.toLowerCase(Locale.ROOT)) {
                case "swords", "sword" -> {
                    materials.addAll(Tag.ITEMS_SWORDS.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_SWORDS");
                }
                case "axe", "axes" -> {
                    materials.addAll(Tag.ITEMS_AXES.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_AXES");
                }
                case "fishing_rods", "rods" -> {
                    materials.addAll(Tag.ITEMS_ENCHANTABLE_FISHING.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_ENCHANTABLE_FISHING");
                }
                case "pickaxe", "pickaxes" -> {
                    materials.addAll(Tag.ITEMS_PICKAXES.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_PICKAXES");
                }
                case "shovel", "shovels" -> {
                    materials.addAll(Tag.ITEMS_SHOVELS.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_SHOVELS");
                }
                case "bows" -> {
                    materials.addAll(MaterialTags.BOWS.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "BOWS");
                }
                case "hoe", "hoes" -> {
                    materials.addAll(Tag.ITEMS_HOES.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_HOES");
                }
                case "helmet", "head", "helmets" -> {
                    materials.addAll(Tag.ITEMS_HEAD_ARMOR.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_HEAD_ARMOR");
                }
                case "chestplate", "chestplates" -> {
                    materials.addAll(Tag.ITEMS_CHEST_ARMOR.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_CHEST_ARMOR");
                }
                case "leggings", "legs" -> {
                    materials.addAll(Tag.ITEMS_LEG_ARMOR.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_LEG_ARMOR");
                }
                case "boots", "feet" -> {
                    materials.addAll(Tag.ITEMS_FOOT_ARMOR.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_FOOT_ARMOR");
                }
                case "spear", "spears" -> {
                    materials.addAll(Tag.ITEMS_SPEARS.getValues());
                    ManaLogger.finer("config.enchant.tag.resolved", key, "ITEMS_SPEARS");
                }
                default -> {
                    Material material = Material.matchMaterial(key);
                    if (material != null) {
                        materials.add(material);
                        ManaLogger.finer("config.enchant.material.resolved", key, material.name());
                    } else {
                        ManaLogger.warning("config.enchant.material.invalid", key);
                    }
                }
            }
        }

        return materials;
    }

    @Contract("_ -> new")
    private @Nullable CostModel parseCostModel(@Nonnull ConfigurationSection costsSection) {
        String mode = costsSection.getString("mode", "PER_LEVEL").toUpperCase();
        ManaLogger.config("config.costs.mode", mode);

        // PER_LEVEL
        if (mode.equalsIgnoreCase("PER_LEVEL")) {
            Map<Integer, PerLevelCost> levels = new HashMap<>();
            ConfigurationSection levelsSection = costsSection.getConfigurationSection("levels");
            if (levelsSection == null) {
                ManaLogger.warning("config.costs.per.level.missing_levels");
                return null;
            }

            for (String levelKey : levelsSection.getKeys(false)) {
                int level = Integer.parseInt(levelKey);
                ConfigurationSection levelSection = levelsSection.getConfigurationSection(levelKey);
                if (levelSection == null) {
                    ManaLogger.warning("config.costs.per.level.missing_level_section", level);
                    continue;
                }

                ConfigurationSection catalystSection = levelSection.getConfigurationSection("catalyst");
                if (catalystSection == null) {
                    ManaLogger.warning("config.costs.per.level.missing_catalyst", level);
                    continue;
                }

                String materialKey = catalystSection.getString("material");
                String itemKey = catalystSection.getString("item");
                int amount = catalystSection.getInt("amount");

                CatalystCost catalystCost;

                if (itemKey != null) {
                    ItemStack savedItem = SavedItems.get().get(itemKey);
                    if (savedItem == null) {
                        ManaLogger.warning("config.costs.catalyst.saved_item_missing", itemKey);
                        continue;
                    }

                    if (amount > savedItem.getMaxStackSize()) {
                        ManaLogger.warning("config.costs.exceed.stack", savedItem.getType().name(), amount);
                        continue;
                    }

                    catalystCost = new CatalystCost(itemKey, amount);
                } else {

                    Material material = Material.matchMaterial(materialKey == null ? "LAPIS_LAZULI" : materialKey);

                    if (material == null) {
                        ManaLogger.warning("config.enchant.material.invalid", materialKey);
                        return null;
                    }

                    if (amount > material.getMaxStackSize()) {
                        ManaLogger.warning("config.costs.exceed.stack", material.name(), amount);
                        continue;
                    }

                    catalystCost = new CatalystCost(material, amount);
                }

                int xp = levelSection.getInt("xp");
                int bookshelf = levelSection.getInt("bookshelf");

                levels.put(level, new PerLevelCost(catalystCost, xp, bookshelf));

                ManaLogger.config("config.costs.per.level.loaded", level, catalystCost.getItem().getType().name(), amount, xp, bookshelf);
            }

            ManaLogger.config("config.costs.per.level.complete", levels.size());
            return new PerLevelCostModel(levels);
        }
        // FORMULA

        else {
            ConfigurationSection catalystSection = costsSection.getConfigurationSection("catalyst");
            if (catalystSection == null) {
                ManaLogger.warning("config.costs.formula.missing_catalyst_section");
                return null;
            }

            ConfigurationSection defaultSection = catalystSection.getConfigurationSection("default");
            if (defaultSection == null) {
                ManaLogger.warning("config.costs.formula.missing_default_catalyst");
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

                        ManaLogger.finer("config.costs.formula.override", level, amount);
                    }
                }

                catalyst = new DefaultCatalystFormula(defaultCost, base, perLevel, multiplier, overrides);

                ManaLogger.config("config.costs.formula.catalyst", base, perLevel, multiplier, overrides.size());
            }

            // XP formula
            XPFormula xpFormula;
            {
                ConfigurationSection xpSection = costsSection.getConfigurationSection("xp");
                if (xpSection == null) {
                    ManaLogger.warning("config.costs.formula.missing_xp");
                    return null;
                }

                String formulaStr = xpSection.getString("formula", "FLAT").toUpperCase();
                XPFormulaType formulaType = XPFormulaType.valueOf(formulaStr);
                int xpBase = xpSection.getInt("base", 1);
                int perLevel = xpSection.getInt("per_level", 1);
                double multiplierXp = xpSection.getDouble("multiplier", 1.0);
                int stepAmount = xpSection.getInt("step_amount", 0);
                int stepInterval = xpSection.getInt("step_interval", 1);

                xpFormula = XPFormulas.createFormula(formulaType, xpBase, perLevel, multiplierXp, stepAmount, stepInterval);

                ManaLogger.config("config.costs.formula.xp", formulaType.name(), xpBase, perLevel, multiplierXp, stepAmount, stepInterval);
            }

            // Bookshelf requirement
            BookshelfRequirement shelfReq;
            {
                ConfigurationSection shelfSection = costsSection.getConfigurationSection("bookshelf");
                if (shelfSection == null) {
                    ManaLogger.warning("config.costs.formula.missing_bookshelf");
                    return null;
                }

                String shelfModeStr = shelfSection.getString("mode", "REQUIRED").toUpperCase();
                BookshelfMode shelfMode = BookshelfMode.valueOf(shelfModeStr);

                if (shelfMode == BookshelfMode.REQUIRED) {
                    ConfigurationSection req = shelfSection.getConfigurationSection("required");
                    if (req == null) {
                        ManaLogger.warning("config.costs.formula.required_bookshelf.missing_section");
                        return null;
                    }

                    shelfReq = new RequiredBookshelf(req.getInt("base"), req.getInt("per_level"));
                    ManaLogger.config("config.costs.formula.bookshelf.required", req.getInt("base"), req.getInt("per_level"));
                }
                else if (shelfMode == BookshelfMode.TIERED) {
                    Map<Integer, Integer> tiers = new HashMap<>();
                    ConfigurationSection tierSection = shelfSection.getConfigurationSection("tiers");
                    if (tierSection == null) {
                        ManaLogger.warning("config.costs.formula.bookshelf.tiered.missing_section");
                        return null;
                    }

                    for (String lvl : tierSection.getKeys(false)) {
                        int level = Integer.parseInt(lvl);
                        int shelves = tierSection.getInt(lvl);
                        tiers.put(level, shelves);
                        ManaLogger.finer("config.costs.formula.bookshelf.tiered.level", level, shelves);
                    }
                    shelfReq = new TieredBookshelf(tiers);
                } else {
                    shelfReq = new IgnoredBookshelf();
                    ManaLogger.config("config.costs.formula.bookshelf.ignored");
                }
            }

            return new FormulaCostModel(xpFormula, catalyst, shelfReq);
        }
    }

    private @Nullable CatalystCost parseCatalyst(ConfigurationSection section, int amount) {

        String itemKey = section.getString("item");
        String materialKey = section.getString("material");

        if (itemKey != null) {

            if (SavedItems.get().get(itemKey) == null) {
                ManaLogger.warning("config.costs.catalyst.saved_item_missing", itemKey);
                return null;
            }

            return new CatalystCost(itemKey, amount);
        }

        Material material = Material.matchMaterial(materialKey == null ? "EMERALD" : materialKey);

        if (material == null) {
            ManaLogger.warning("config.enchant.material.invalid", materialKey);
            return null;
        }

        return new CatalystCost(material, amount);
    }


    @Contract(pure = true)
    public @NonNull @UnmodifiableView Map<String, TieredEnchant> getEnchants() {
        return Collections.unmodifiableMap(enchants);
    }

    public @Nullable TieredEnchant getEnchant(@Nonnull String enchantName) {
        return enchants.get(enchantName.toLowerCase(Locale.ROOT));
    }

    private static class InstanceHolder { private static TieredEnchantsConfig instance = new TieredEnchantsConfig(); }
    public static TieredEnchantsConfig get() { return InstanceHolder.instance; }
    public static void reload() { InstanceHolder.instance = new TieredEnchantsConfig(); }
}