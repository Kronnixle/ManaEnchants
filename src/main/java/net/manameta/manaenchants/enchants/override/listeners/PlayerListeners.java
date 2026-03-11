package net.manameta.manaenchants.enchants.override.listeners;

import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.gui.MerchantGUI;
import net.manameta.manaenchants.enchants.override.events.EnchantTableOpenEvent;
import net.manameta.manaenchants.xp.override.CustomXP;
import net.manameta.manaenchants.xp.override.VanillaXP;
import net.manameta.manaenchants.xp.override.XPManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.jspecify.annotations.NonNull;

public class PlayerListeners implements Listener {
    /**
     * Our listener for determining if the user right-clicked the enchantment table to create our
     * EnchantTableOpenEvent
     *
     * @param event the player interact event
     */
    @EventHandler
    public void onPlayerInteract(@NonNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!event.getAction().isRightClick()) return;

        if (block.getType() != Material.ENCHANTING_TABLE) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();

        event.setCancelled(true);

        ManaEnchants.getInstance().getServer().getPluginManager().callEvent(new EnchantTableOpenEvent(player, block));
    }

    @EventHandler
    public void onPlayerOpenTable(@NonNull EnchantTableOpenEvent event) {
        Player player = event.getPlayer();
        Block enchantTable = event.getEnchantTable();

        ItemStack heldItem;

        if (Tag.ITEMS_ENCHANTABLE_VANISHING.isTagged(player.getInventory().getItemInMainHand().getType())) {
            heldItem = player.getInventory().getItemInMainHand().clone();
            player.getInventory().getItemInMainHand().setAmount(0);
        } else {
            heldItem = ItemStack.of(Material.AIR);
        }

        MerchantGUI merchantGUI = new MerchantGUI(player, enchantTable.getLocation(), heldItem);
        ManaEnchants.getGUIManager().openMerchant(player, merchantGUI);
    }

    @EventHandler
    public void onPlayerDeath(@NonNull PlayerDeathEvent event) {

        Player player = event.getEntity();

        int penaltyPercent = ConfigData.get().getDeathPenalty();
        int lostPercent = ConfigData.get().getLostEXP();

        event.setKeepLevel(true);

        if (penaltyPercent <= 0) {
            event.setShouldDropExperience(false);
            return;
        }

        int totalXP = XPManager.getTotalXP(player);

        int penaltyXP = (int) Math.floor(totalXP * (penaltyPercent / 100.0));

        int lostXP = (int) Math.floor(penaltyXP * (lostPercent / 100.0));
        int droppedXP = penaltyXP - lostXP;

        int newTotalXP = Math.max(0, totalXP - penaltyXP);

        XPManager.setTotalXP(player, newTotalXP);

        /*
         Set drop amount
         */
        event.setDroppedExp(droppedXP);
    }

    @EventHandler
    public void onPlayerJoin(@NonNull PlayerJoinEvent event) {
        XPManager.updatePlayer(event.getPlayer());
    }
}