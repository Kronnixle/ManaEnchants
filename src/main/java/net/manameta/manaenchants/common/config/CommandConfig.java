package net.manameta.manaenchants.common.config;

import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.HelpEntry;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.ManaEnchants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.File;
import java.util.*;

@Singleton
public final class CommandConfig {
    private final Map<ParentCommand, EnumMap<HelpID, HelpEntry>> helpEntries = new EnumMap<>(ParentCommand.class);
    private final EnumMap<ParentCommand, EnumMap<HelpID, CommandConfigEntry>> commandConfig = new EnumMap<>(ParentCommand.class);

    private CommandConfig() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "command_config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        registerManaEnchants(config);
        registerEnchant(config);
        registerXP(config);

        for (ParentCommand parentCommand : commandConfig.keySet()) {
            List<HelpID> helpIDs = getHelpIDs(parentCommand);

            buildHelpMap(parentCommand, helpIDs);
        }
    }

    public Map<HelpID, HelpEntry> getParent(@NotNull ParentCommand parent) { return helpEntries.get(parent); }

    public HelpEntry getEntry(@NotNull ParentCommand parent, @NotNull HelpID id) {
        return Objects.requireNonNull(helpEntries.get(parent), "No help entries registered for " + parent + " " + id).get(id);
    }

    private void buildHelpMap(@NotNull ParentCommand parent, @NotNull Iterable<HelpID> ids) {
        EnumMap<HelpID, HelpEntry> map = new EnumMap<>(HelpID.class);

        for (HelpID helpID : ids) {
            List<String> aliases = getCommandAliases(parent, helpID);
            String shortDescKey = "commands." + parent.name().toLowerCase() + "." + helpID.name().toLowerCase() + ".short";

            int descAmount = getCommandDescAmount(parent, helpID);
            List<String> descKeys = new ArrayList<>(descAmount);
            for (int i = 1; i <= descAmount; i++) {
                descKeys.add("commands." + parent.name().toLowerCase() + "." + helpID.name().toLowerCase() + ".desc." + i);
            }

            String formatKey = "commands." + parent.name().toLowerCase() + "." + helpID.name().toLowerCase() + ".format";

            map.put(helpID, new HelpEntry(helpID, formatKey, aliases, shortDescKey, descKeys));
        }

        helpEntries.put(parent, map);
    }

    /** Loads the ManaEnchants commands from config (or defaults if missing) */
    private void registerManaEnchants(@NotNull ConfigurationSection config) {
        loadParentCommandsFromFile(config, ParentCommand.MANAENCHANTS, EnumSet.of(
                HelpID.ROOT, HelpID.HELP, HelpID.CONFIG, HelpID.ITEM, HelpID.RELOAD, HelpID.VERSION
        ));
    }

    /** Loads the Enchant commands from config (or defaults if missing) */
    private void registerEnchant(@NotNull ConfigurationSection config) {
        loadParentCommandsFromFile(config, ParentCommand.ENCHANT, EnumSet.of(
                HelpID.ROOT, HelpID.HELP, HelpID.ADD, HelpID.CLEAR, HelpID.INFO, HelpID.LIST,
                HelpID.REMOVE, HelpID.SET
        ));
    }

    private void registerXP(@NotNull ConfigurationSection config) {
        loadParentCommandsFromFile(config, ParentCommand.XP, EnumSet.of(
                HelpID.ROOT, HelpID.HELP, HelpID.ADD, HelpID.CLEAR, HelpID.INFO, HelpID.REMOVE,
                HelpID.SET
        ));
    }

    private void loadParentCommandsFromFile(@NotNull ConfigurationSection config, @NotNull ParentCommand parent, @NotNull Iterable<HelpID> helpIDs) {
        EnumMap<HelpID, CommandConfigEntry> map = new EnumMap<>(HelpID.class);

        for (HelpID helpID : helpIDs) {
            String basePath = parent.name().toLowerCase() + "." + helpID.name().toLowerCase();

            // Default aliases = just the lowercased helpID
            List<String> aliases = config.getStringList(basePath + ".aliases");

            // Default description amount = 1
            int descAmount = config.getInt(basePath + ".description_amount", 1);

            map.put(helpID, new CommandConfigEntry(aliases, descAmount));
        }

        commandConfig.put(parent, map);
    }

    @NotNull
    private AbstractMap<HelpID, CommandConfigEntry> getParentConfig(ParentCommand parent) {
        return commandConfig.getOrDefault(parent, new EnumMap<>(HelpID.class));
    }

    @NotNull
    private List<HelpID> getHelpIDs(ParentCommand parent) {
        return new ArrayList<>(getParentConfig(parent).keySet());
    }

    @NotNull
    private List<String> getCommandAliases(ParentCommand parent, HelpID helpID) {
        return commandConfig.getOrDefault(parent, new EnumMap<>(HelpID.class))
                .getOrDefault(helpID, new CommandConfigEntry(List.of(), 0))
                .aliases();
    }

    private int getCommandDescAmount(ParentCommand parent, HelpID helpID) {
        return commandConfig.getOrDefault(parent, new EnumMap<>(HelpID.class))
                .getOrDefault(helpID, new CommandConfigEntry(List.of(), 0))
                .descriptionAmount();
    }

    // ===== SINGLETON =====
    private static class InstanceHolder {  private static final CommandConfig instance = new CommandConfig(); }
    public static CommandConfig get() {return InstanceHolder.instance; }
}