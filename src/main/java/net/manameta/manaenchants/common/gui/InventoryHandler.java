package net.manameta.manaenchants.common.gui;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.event.inventory.*;

public interface InventoryHandler {
    void onDrag(InventoryDragEvent event);
    void onClick(InventoryClickEvent event);
    void onOpen(InventoryOpenEvent event);
    void onClose(InventoryCloseEvent event);
    void onTradeSelect(TradeSelectEvent event);
}