package net.manameta.manaenchants.common.gui;

import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.enchants.session.EnchantSession;
import net.manameta.manaenchants.enchants.session.EnchantSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.MerchantView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class MerchantGUI {

    private final Player player;

    private final EnchantSession session;
    private final Villager villager;

    @ApiStatus.Experimental
    private final MerchantView merchantView;

    public MerchantGUI(@Nonnull Player player, @Nonnull Location location, @Nonnull ItemStack slotOne) {
        this.player = player;

        villager = location.getWorld().spawn(location.clone().add(0, 200, 0), Villager.class, v -> {
            v.setAI(false);
            v.setSilent(true);
            v.setPersistent(false);
            v.setInvisible(true);
            v.setInvulnerable(true);
        });

        merchantView = MenuType.MERCHANT.builder()
                .merchant(villager)
                .checkReachable(false)
                .title(LocaleManager.get(player.locale(), "ui.enchanter.title", ConfigData.get().getHeaderColour()).decorate(TextDecoration.BOLD))
                .build(player);

        session = EnchantSessionManager.create(player, location, merchantView, this);
        session.initialize(slotOne);
    }

    MerchantView getMerchantView() { return merchantView; }

    void onClick(@NonNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();

        // Result slot (trade completed)
        if (rawSlot == 2 && clickedItem != null && canTakeResult(event)) {
            Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), session::handleTradeComplete);
            return;
        }

        // Tool or catalyst changed
        if (rawSlot == 0 || rawSlot == 1) {
            session.markDirty();
        }

        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), () -> {
            if (player.getItemOnCursor().getType() != Material.AIR) return;
            if (!session.tryPreviewFromCursor()) {
                session.tryRefreshIfStable();
            }
        });
    }

    private boolean canTakeResult(@NonNull InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();

        boolean cursorEmpty = cursor.isEmpty();
        boolean shiftToInventory = event.isShiftClick() && player.getInventory().firstEmpty() != -1;

        return cursorEmpty || shiftToInventory;
    }

    void onDrag(@NonNull InventoryDragEvent event) {

        boolean affectsMerchantInput = false;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == 0 || rawSlot == 1) {
                affectsMerchantInput = true;
                break;
            }
        }

        if (!affectsMerchantInput) return;

        session.markDirty();

        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), () -> {
            if (!session.tryPreviewFromCursor()) {
                session.tryRefreshIfStable();
            }
        });
    }

    void onClose(@NonNull InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        EnchantSessionManager.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), villager::remove);
    }

    void onTradeSelect(@NonNull TradeSelectEvent event) {
        session.handleTradeSelect(event);
    }
}