package net.manameta.manaenchants.common.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.xp.model.LevelCost;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

@Singleton
public final class ConfigData {
    // -----------------------------
    // General Settings
    // -----------------------------
    private Sound errorSound, clickSound, chatNotifySound, successSound, levelUpSound;
    private final Locale defaultLocale;
    private final boolean strict;
    private final Level logLevel;
    private final int entriesPerPage;


    // -----------------------------
    // Experience Settings
    // -----------------------------
    private final boolean xpEnabled;
    private final List<LevelCost> xpCosts;
    private final int deathPenalty, lostEXP;

    // -----------------------------
    // Getter Methods
    // -----------------------------
    public Sound getErrorSound() { return errorSound; }
    public Sound getClickSound() { return clickSound; }
    public Sound getChatNotifySound() { return chatNotifySound; }
    public Sound getSuccessSound() { return successSound; }
    public Sound getLevelUpSound() { return levelUpSound; }

    public Locale getDefaultLocale() { return defaultLocale; }
    public boolean getStrict() { return strict; }
    public Level getLogLevel() { return logLevel; }
    public int getEntriesPerPage() { return entriesPerPage; }

    public boolean isXpEnabled() { return xpEnabled; }
    public List<LevelCost> getXpCosts() { return xpCosts; }

    public int getDeathPenalty() { return deathPenalty; }
    public int getLostEXP() { return lostEXP; }

    private ConfigData() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        entriesPerPage = config.getInt("entries_per_page", 8);
        defaultLocale = Locale.of(config.getString("locale.default", "en"));
        strict = config.getBoolean("locale.strict", false);
        logLevel = Level.parse(config.getString("debug.log_level", "WARNING").toUpperCase());

        // Sounds
        for (String soundStr : Set.of("error", "click", "chat_notify", "success", "level_up")) {
            Sound sound = buildSound(config, soundStr);
            switch (soundStr) {
                case "error" -> errorSound = sound;
                case "click" -> clickSound = sound;
                case "chat_notify" -> chatNotifySound = sound;
                case "success" -> successSound = sound;
                case "level_up" -> levelUpSound = sound;
            }
        }

        // -----------------------------
        // Load Experience Section
        // -----------------------------
        xpEnabled = config.getBoolean("experience.enabled", true);

        // Costs
        xpCosts = new ArrayList<>();

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

    private static class InstanceHolder { private static ConfigData instance = new ConfigData(); }
    public static ConfigData get() { return InstanceHolder.instance; }
    public static void reload() {
        InstanceHolder.instance = new ConfigData();
    }
}