package net.manameta.manaenchants.common.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.xp.model.LevelCost;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public final class ConfigData {
    // -----------------------------
    // General Settings
    // -----------------------------
    private Sound errorSound, clickSound, successSound, levelUpSound, enchantSound;
    private TextColor errorColour, errorHighlightColour, successColour, successHighlightColour, descriptionColour, descriptionHighlightColour,
            hoverColour, pageColour, headerColour, enchantColour;

    private final Component corePrefix, enchantPrefix, xpPrefix;

    private final Locale defaultLocale;
    private final boolean strict;
    private final int entriesPerPage;

    // -----------------------------
    // Experience Settings
    // -----------------------------
    private final boolean vanillaXP;
    private final List<LevelCost> xpCosts;
    private final int deathPenalty, lostEXP;

    // -----------------------------
    // Getter Methods
    // -----------------------------
    public Sound getErrorSound() { return errorSound; }
    public Sound getClickSound() { return clickSound; }
    public Sound getSuccessSound() { return successSound; }
    public Sound getLevelUpSound() { return levelUpSound; }
    public Sound getEnchantSound() { return enchantSound; }

    public TextColor getErrorColour() { return errorColour; }
    public TextColor getErrorHighlightColour() { return errorHighlightColour; }
    public TextColor getSuccessColour() { return successColour; }
    public TextColor getSuccessHighlightColour() { return successHighlightColour; }
    public TextColor getDescriptionColour() { return descriptionColour; }
    public TextColor getDescriptionHighlightColour() { return descriptionHighlightColour; }
    public TextColor getHeaderColour() { return headerColour; }
    public TextColor getHoverColour() { return hoverColour; }
    public TextColor getPageColour() { return pageColour; }
    public TextColor getEnchantColour() { return enchantColour; }

    public Component getCorePrefix() { return corePrefix; }
    public Component getEnchantPrefix() { return enchantPrefix; }
    public Component getXPPrefix() { return xpPrefix; }

    public Locale getDefaultLocale() { return defaultLocale; }
    public boolean getStrict() { return strict; }
    public int getEntriesPerPage() { return entriesPerPage; }

    public boolean isVanillaXP() { return vanillaXP; }
    public List<LevelCost> getXpCosts() { return xpCosts; }

    public int getDeathPenalty() { return deathPenalty; }
    public int getLostEXP() { return lostEXP; }

    private ConfigData() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        entriesPerPage = config.getInt("entries_per_page", 8);
        defaultLocale = Locale.of(config.getString("locale.default", "en"));
        strict = config.getBoolean("locale.strict", false);

        // Sounds
        for (String soundStr : Set.of("error", "click", "success", "level_up", "enchant")) {
            Sound sound = buildSound(config, soundStr);
            switch (soundStr) {
                case "error" -> errorSound = sound;
                case "click" -> clickSound = sound;
                case "success" -> successSound = sound;
                case "level_up" -> levelUpSound = sound;
                case "enchant" -> enchantSound = sound;
            }
        }

        // Colours
        for (String colourStr : Set.of("error", "error_highlight", "success", "success_highlight", "header", "description_highlight", "hover",
                "page", "description", "enchant")) {
            TextColor colour = buildColour(config, colourStr);
            switch (colourStr) {
                case "error" -> errorColour = colour;
                case "error_highlight" -> errorHighlightColour = colour;
                case "success" -> successColour = colour;
                case "success_highlight" -> successHighlightColour = colour;
                case "description" -> descriptionColour = colour;
                case "description_highlight" -> descriptionHighlightColour = colour;
                case "header" -> headerColour = colour;
                case "hover" -> hoverColour = colour;
                case "page" -> pageColour = colour;
                case "enchant" -> enchantColour = colour;
            }
        }

        corePrefix = MiniMessage.miniMessage().deserialize(config.getString("prefix.core", "<gray>[<gold>ManaEnchants<gray>]"));
        enchantPrefix = MiniMessage.miniMessage().deserialize(config.getString("prefix.enchant", "<gray>[<gold>Enchant<gray>]"));
        xpPrefix = MiniMessage.miniMessage().deserialize(config.getString("prefix.xp", "<gray>[<gold>XP<gray>]"));

        // -----------------------------
        // Load Experience Section
        // -----------------------------
        vanillaXP = config.getBoolean("experience.vanilla", true);

        // Costs
        xpCosts = new ArrayList<>();

        if (vanillaXP) {
            xpCosts.add(new LevelCost(0, "2 * level + 7"));
            xpCosts.add(new LevelCost(16, "5 * level - 38"));
            xpCosts.add(new LevelCost(31, "9 * level - 158"));
        } else {
            // Custom XP mode: load from config
            List<Map<?, ?>> costsList = config.getMapList("experience.costs");

            if (costsList.isEmpty()) {
                // Fallback flat XP
                xpCosts.add(new LevelCost(0, "100"));
            } else {
                for (Map<?, ?> entry : costsList) {
                    int minLevel = (entry.get("min_level") instanceof Number) ? ((Number) entry.get("min_level")).intValue() : 0;
                    String formula = (entry.get("formula") != null) ? entry.get("formula").toString() : "100";
                    xpCosts.add(new LevelCost(minLevel, formula));
                }
                xpCosts.sort(Comparator.comparingInt(LevelCost::minLevel));
            }
        }

        deathPenalty = config.getInt("experience.death_penalty", 100);
        lostEXP = config.getInt("experience.lost_exp", 100);

    }

    private @NotNull Sound buildSound(@NotNull FileConfiguration config, String soundStr) {
        String key = config.getString("sounds."+soundStr+".key", "minecraft:entity.villager.yes");
        float volume = (float) config.getDouble("sounds."+soundStr+".volume", 1.0f);
        float pitch = (float) config.getDouble("sounds."+soundStr+".pitch", 1.0f);
        int seed = config.getInt("sounds."+soundStr+".seed", -1);

        return Sound.sound().type(Key.key(key))
               .source(Sound.Source.UI)
               .volume(volume)
               .pitch(pitch)
               .seed(seed)
               .build();
    }

    private @NotNull TextColor buildColour(@NotNull ConfigurationSection config, String colourStr) {
        String key = config.getString("colours."+colourStr, "#AAAAAA");

        TextColor color = TextColor.fromHexString(key);
        return Objects.requireNonNullElse(color, NamedTextColor.GRAY);
    }

    private static class InstanceHolder { private static ConfigData instance = new ConfigData(); }
    public static ConfigData get() { return InstanceHolder.instance; }
    public static void reload() {
        InstanceHolder.instance = new ConfigData();
    }
}