package net.manameta.manaenchants.enchants.override.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class EnchantTableOpenEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    private final Player player;
    private final Block enchantTable;

    public EnchantTableOpenEvent(@NonNull Player player, @NonNull Block enchantTable) {
        this.player = player;
        this.enchantTable = enchantTable;
    }

    public @NonNull Player getPlayer() {
        return player;
    }
    public @NonNull Block getEnchantTable() { return enchantTable; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { cancelled = cancel; }
}
