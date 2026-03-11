package net.manameta.manaenchants.common.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class GUIManager {
    private final Map<Inventory, MerchantGUI> activeInventories = new HashMap<>();

    public void openMerchant(@NotNull HumanEntity player, @NotNull MerchantGUI gui) {
        registerHandledInventory(gui.getMerchantView().getTopInventory(), gui);
        player.openInventory(gui.getMerchantView());
    }


    public void reregisterInventory(@NotNull MerchantGUI gui) {
        registerHandledInventory(gui.getMerchantView().getTopInventory(), gui);
    }

    private void registerHandledInventory(@Nonnull Inventory inventory, @Nonnull MerchantGUI handler) {
        activeInventories.put(inventory, handler);
    }

    private void unregisterInventory(Inventory inventory) {
        activeInventories.remove(inventory);
    }

    void handleDrag(@NotNull InventoryDragEvent event) {
        MerchantGUI handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onDrag(event);
    }

    void handleClick(@NotNull InventoryClickEvent event) {
        MerchantGUI handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onClick(event);
    }

    void handleClose(@NotNull InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        MerchantGUI handler = activeInventories.get(inventory);

        if (handler != null) {
            handler.onClose(event);
            unregisterInventory(inventory);
        }
    }

    void handleTradeSelect(@NonNull TradeSelectEvent event) {
        MerchantGUI handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onTradeSelect(event);
    }
}