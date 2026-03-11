package net.manameta.manaenchants.common.config;

import net.manameta.manaenchants.ManaEnchants;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigurationManager {
    /** Prevent instantization */
    private ConfigurationManager() {}

    public static void initialize() {
        ensureRootStructure();
        loadCoreConfigs();
        loadInfrastructureConfigs();
    }

    /**
     * Ensures our default root structure directories are set.
     */
    private static void ensureRootStructure() {
        createDirectory("locale");
    }

    private static void loadCoreConfigs() {
        ensureVersioned("config.yml", 1);
        ensureVersioned("tiered_enchants.yml", 1);
        ensureVersioned("command_config.yml", 1);
    }

    private static void loadInfrastructureConfigs() {
        ensureExists("locale/en.properties");
        ensureExists("saved_items.yml");
    }

    private static void createDirectory(String relativePath) {
        Path path = ManaEnchants.getInstance().getDataFolder().toPath().resolve(relativePath);

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + relativePath, e);
        }

    }

    @SuppressWarnings("SameParameterValue")
    private static void ensureExists(String resourcePath) {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), resourcePath);
        if (!file.exists() || file.length() == 0) {
            ManaEnchants.getInstance().saveResource(resourcePath, true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void ensureVersioned(@NotNull String resourcePath, int expectedVersion) {

        File file = new File(ManaEnchants.getInstance().getDataFolder(), resourcePath);

        if (!file.exists() || file.length() == 0) {
            ManaEnchants.getInstance().saveResource(resourcePath, false);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        int version = config.getInt("config-version", -1);

        if (version != expectedVersion) {
            backup(file);
            ManaEnchants.getInstance().saveResource(resourcePath, true);
        }
    }

    private static void backup(@NotNull File file) {
        File backup = new File(file.getParent(), file.getName() + ".backup");

        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to backup file: " + file.getName(), e);
        }
    }
}
