package net.manameta.manaenchants.common.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class GUIListener implements Listener {
    private final GUIManager guiManager;

    public GUIListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        guiManager.handleClick(event);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent inventoryOpenEvent) {
        guiManager.handleOpen(inventoryOpenEvent);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        guiManager.handleClose(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        guiManager.handleDrag(event);
    }

    @EventHandler
    public void onTradeSelect(TradeSelectEvent event) { guiManager.handleTradeSelect(event); }
}