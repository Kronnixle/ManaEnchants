package net.manameta.manaenchants.items;

import net.manameta.manaenchants.ManaEnchants;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class SavedItems {

    private final Map<String, ItemStack> items = new HashMap<>();

    private SavedItems() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "saved_items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!file.exists()) {
            ManaEnchants.getInstance().saveResource("saved_items.yml", false);
        }

        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ItemStack item = section.getItemStack(key);
            if (item != null) items.put(key.toLowerCase(), item.asQuantity(1));
        }
    }

    private void save() {
        File file = new File(ManaEnchants.getInstance().getDataFolder(), "saved_items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("items", null);

        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            config.set("items." + entry.getKey(), entry.getValue().asQuantity(1));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ItemStack> getItems() {
        return items;
    }

    public @Nullable ItemStack get(@NonNull String name) {
        ItemStack item = items.get(name.toLowerCase());
        if (item == null) return null;
        return item.asQuantity(1);
    }

    public void add(@NonNull String name, ItemStack item) {
        items.put(name.toLowerCase(), item);
        save();
    }

    public void remove(@NonNull String name) {
        items.remove(name.toLowerCase());
        save();
    }

    public void clear() {
        items.clear();
        save();
    }

    private static class InstanceHolder { private static SavedItems instance = new SavedItems(); }
    public static SavedItems get() { return InstanceHolder.instance; }
    public static void reload() { SavedItems instance = new SavedItems(); }
}