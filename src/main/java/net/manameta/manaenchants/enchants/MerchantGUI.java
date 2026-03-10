package net.manameta.manaenchants.enchants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.gui.InventoryHandler;
import net.manameta.manaenchants.enchants.session.EnchantSession;
import net.manameta.manaenchants.enchants.session.EnchantSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.MerchantView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class MerchantGUI implements InventoryHandler {

    private final Player player;
    private final ItemStack slotOne;

    private final EnchantSession session;
    private final Villager villager;

    @ApiStatus.Experimental
    private final MerchantView merchantView;

    public MerchantGUI(@Nonnull Player player, @Nonnull Location location, @Nonnull ItemStack slotOne) {
        this.player = player;

        this.slotOne = slotOne;

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
                .title(Component.translatable("ui.enchanter.title", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                .build(player);

        session = EnchantSessionManager.create(player, location, merchantView, this);
    }

    public MerchantView getMerchantView() { return merchantView; }

    @Override
    public void onOpen(@NonNull InventoryOpenEvent event) {
        session.initialize(slotOne);
    }

    @Override
    public void onClick(@NonNull InventoryClickEvent event) {
        int rawSlot = event.getRawSlot();

        // Result slot (trade completed)
        ItemStack clickedItem = event.getCurrentItem();
        if (rawSlot == 2 && clickedItem != null) {
            Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), session::handleTradeComplete);
            return;
        }

        // Tool or catalyst changed
        if (rawSlot == 0 || rawSlot == 1) {
            session.markDirty();
        }

        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), () -> {
            if (!session.tryPreviewFromCursor()) {
                session.tryRefreshIfStable();
            }
        });
    }

    @Override
    public void onDrag(@NonNull InventoryDragEvent event) {

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

    @Override
    public void onClose(InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        EnchantSessionManager.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(ManaEnchants.getInstance(), villager::remove);
    }

    @Override
    public void onTradeSelect(@NonNull TradeSelectEvent event) {
        session.handleTradeSelect(event);
    }
}