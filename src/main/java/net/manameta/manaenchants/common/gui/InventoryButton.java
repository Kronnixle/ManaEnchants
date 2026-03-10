package net.manameta.manaenchants.common.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryButton {
    private Function<Player, ItemStack> iconCreator;
    private Consumer<InventoryClickEvent> eventConsumer;
    private boolean cancelled = true;

    public InventoryButton creator(Function<Player, ItemStack> iconCreator) {
        this.iconCreator = iconCreator;
        return this;
    }

    public InventoryButton consumer(Consumer<InventoryClickEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    InventoryButton cancel(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    boolean hasCancelled() {
        return cancelled;
    }

    Consumer<InventoryClickEvent> getEventConsumer() {
        return eventConsumer;
    }

    Function<Player, ItemStack> getIconCreator() {
        return iconCreator;
    }
}