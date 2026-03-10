package net.manameta.manaenchants.commands.enchant;

import net.kyori.adventure.audience.Audience;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

final class C_EnchantClear {
    /** Prevent instantization */
    private C_EnchantClear() {}

    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.core.item.missing");
            return 0;
        }

        if (item.getEnchantments().isEmpty()) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.enchant.empty");
            return 0;
        }

        item.removeEnchantments();
        MessageHelpers.success(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.enchant.clear.success");

        return 0;
    }
}