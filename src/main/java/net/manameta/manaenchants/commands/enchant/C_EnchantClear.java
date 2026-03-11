package net.manameta.manaenchants.commands.enchant;

import net.kyori.adventure.audience.Audience;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

final class C_EnchantClear {
    /** Prevent instantization */
    private C_EnchantClear() {}

    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.core.item.missing");
            return 0;
        }

        if (item.getEnchantments().isEmpty()) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.enchant.empty");
            return 0;
        }

        item.removeEnchantments();
        MessageHelpers.success(sender, ConfigData.get().getEnchantPrefix(), "commands.enchant.clear.success");

        return 0;
    }
}