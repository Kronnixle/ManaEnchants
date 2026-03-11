package net.manameta.manaenchants.commands.enchant;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

final class C_EnchantRemove {
    /** Prevent instantization */
    private C_EnchantRemove() {}

    static int execute(@Nonnull Audience sender, @Nonnull String name) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.core.item.missing");
            return 0;
        }

        TieredEnchant tieredEnchant = TieredEnchantsConfig.get().getEnchant(name);
        if (tieredEnchant == null) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.enchant.not.found",
                    Component.text(name, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        Enchantment enchantment = tieredEnchant.resolveEnchantment();

        if (enchantment == null) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "critical.error.enchant.not.resolved",
                    Component.text(name, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);
        if (currentLevel == 0) {
            MessageHelpers.error(sender, ConfigData.get().getEnchantPrefix(), "commands.enchant.remove.level.zero",
                    Component.text(name, ConfigData.get().getErrorHighlightColour()));
            return 0;
        }

        item.removeEnchantment(enchantment);
        MessageHelpers.success(sender, ConfigData.get().getEnchantPrefix(), "commands.enchant.remove.success",
                Component.text(name, ConfigData.get().getSuccessHighlightColour()));

        return 1;
    }

    static int execute(@Nonnull Audience sender) {
        MessageHelpers.formatError(sender, ParentCommand.ENCHANT, HelpID.REMOVE);
        return 0;
    }
}
