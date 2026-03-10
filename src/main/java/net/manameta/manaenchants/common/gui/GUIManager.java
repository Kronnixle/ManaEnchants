package net.manameta.manaenchants.common.gui;

import net.manameta.manaenchants.enchants.MerchantGUI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {
    private final Map<Inventory, InventoryHandler> activeInventories = new HashMap<>();
    private final Map<UUID, GUIContext> contexts = new HashMap<>();

    /**
     * Opens a GUI as a ROOT (proxy / command entry point).
     * Clears any existing navigation stack.
     */
    private void open(@NotNull HumanEntity player, @NotNull InventoryGUI gui) {
        GUIContext context = new GUIContext();
        context.push(gui);

        contexts.put(player.getUniqueId(), context);
        openInternal(player, gui);
    }
    /**
     * Opens a GUI as a CHILD (navigated from another GUI).
     */
    private void openChild(@NotNull HumanEntity player, @NotNull InventoryGUI gui) {
        GUIContext context = contexts.computeIfAbsent(player.getUniqueId(), k -> new GUIContext());

        context.push(gui);
        openInternal(player, gui);
    }
    /**
     * Opens the previous GUI if possible.
     * @return true if navigated back, false if no back exists
     */
    public boolean openBack(@NotNull HumanEntity player) {
        GUIContext context = contexts.get(player.getUniqueId());
        if (context == null || context.size() < 2) return false;

        InventoryGUI current = context.pop();
        InventoryGUI back = context.peek();

        if (back == null) {
            context.push(current);
            return false;
        }

        // Temporarily mark navigating back to ignore handleClose clean-up
        context.setNavigatingBack(true);
        openInternal(player, back);
        back.refresh();
        context.setNavigatingBack(false);

        return true;
    }

    public void openMerchant(@NotNull HumanEntity player, @NotNull MerchantGUI gui) {
        registerHandledInventory(gui.getMerchantView().getTopInventory(), gui);
        player.openInventory(gui.getMerchantView());
    }
    /**
     * We pop and replace with the current GUI page, meaning that if the user
     * wanted to go back to the page they were on, then it can easily load.
     *
     * @param player the player
     * @param gui the gui
     */
    public void openReplace(@NotNull HumanEntity player, @NotNull InventoryGUI gui) {
        GUIContext context = contexts.computeIfAbsent(player.getUniqueId(), k -> new GUIContext());

        if (!context.isEmpty()) {
            context.pop();
        }

        context.push(gui);
        openInternal(player, gui);
    }
    /**
     * Opens the inventory for the player and registers it.
     */
    private void openInternal(@NotNull HumanEntity player, @NotNull InventoryGUI gui) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        registerHandledInventory(gui.getInventory(), gui);
        player.openInventory(gui.getInventory());
    }

    public void reregisterInventory(@NotNull MerchantGUI gui) {
        registerHandledInventory(gui.getMerchantView().getTopInventory(), gui);
    }

    public void clearHistory(@NotNull UUID userID) {
        contexts.remove(userID);
    }

    public void removeBack(@NotNull Entity player) {
        GUIContext context = contexts.get(player.getUniqueId());
        if (context == null) return;
        context.pop();
    }

    private void registerHandledInventory(@Nonnull Inventory inventory, @Nonnull InventoryHandler handler) {
        activeInventories.put(inventory, handler);
    }

    private void unregisterInventory(Inventory inventory) {
        activeInventories.remove(inventory);
    }

    void handleDrag(@NotNull InventoryDragEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onDrag(event);
    }

    void handleClick(@NotNull InventoryClickEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onClick(event);
    }

    void handleOpen(@NotNull InventoryOpenEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onOpen(event);
    }

    void handleClose(@NotNull InventoryCloseEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        InventoryCloseEvent.Reason reason = event.getReason();

        Inventory inventory = event.getInventory();
        InventoryHandler handler = activeInventories.get(inventory);

        GUIContext context = contexts.get(playerId);

        // Ignore clean-up if the player is navigating back
        if (context != null && context.isNavigatingBack()) return;

        if (handler != null) {
            handler.onClose(event);
            unregisterInventory(inventory);
        }

        // Only fully clear session/context if not navigating back
        if (reason != InventoryCloseEvent.Reason.OPEN_NEW) {
            contexts.remove(playerId);
        }
    }

    void handleTradeSelect(@NonNull TradeSelectEvent event) {
        InventoryHandler handler = activeInventories.get(event.getInventory());
        if (handler != null) handler.onTradeSelect(event);
    }
}