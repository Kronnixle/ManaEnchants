package net.manameta.manaenchants.common.gui;

import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.api.core.packets.ManaPacketType;
import net.manameta.api.core.packets.ManaPullPacket;
import net.manameta.api.core.services.ManaServices;
import net.manameta.api.core.settings.enums.BorderMaterial;
import net.manameta.api.core.settings.enums.SettingKey;
import net.manameta.api.helpers.ManaChannels;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.utils.ItemCreator;
import net.manameta.manaenchants.common.locale.LocaleManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class InventoryGUI implements InventoryHandler {
    private final Inventory inventory;
    private final Map<Integer, InventoryButton> buttonMap = new HashMap<>();
    private Set<Integer> decorationSlots = Set.of();

    protected InventoryGUI(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    // ======== BUTTON MANAGEMENT ========
    /**
     * Adds a clickable button which has an event consumer, ItemStack and options to cancel or not.
     * @param slot the slot to modify
     * @param button the button
     */
    protected void addButton(int slot, InventoryButton button) {
        buttonMap.put(slot, button);
    }
    /**
     * Sets a decoration slot within the inventory, this is a cosmetic feature which allows users
     * to customize their view port.
     * @param decorationSlots the slots to set
     */
    protected void setDecorationSlots(@NotNull Set<Integer> decorationSlots) {
        this.decorationSlots = decorationSlots;
    }

    private void decorateBackground(Entity player) {
        if (decorationSlots.isEmpty()) return;

        BorderMaterial borderMaterial =
                ManaServices.getSettingsService().getSettings(player.getUniqueId()).getEnum(SettingKey.BORDER_MATERIAL, BorderMaterial.class);

        for (int slot : decorationSlots) {
            Material material = Material.GRAY_STAINED_GLASS_PANE;

                    //GUIConfig.get().pickBorderMaterial(borderMaterial, slot);

            ItemStack item = new ItemStack(material, 1);
            item.editMeta(meta -> meta.setHideTooltip(true));

            inventory.setItem(slot, item);
        }
    }

    protected abstract void fillInventory();

    private void decorate(Player player) {
        decorateBackground(player);

        buttonMap.forEach((slot, button) -> {
            Function<Player, ItemStack> creator = button.getIconCreator();
            if (creator == null) return;
            inventory.setItem(slot, creator.apply(player));
        });
    }


    @Override
    public void onDrag(InventoryDragEvent event) {
        for (int slot : event.getRawSlots()) {
            if (slot < inventory.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
            }
            return;
        }

        int slot = event.getRawSlot();
        InventoryButton button = buttonMap.get(slot);
        if (button != null) {
            event.setCancelled(button.hasCancelled());
            if (button.getEventConsumer() != null) {
                button.getEventConsumer().accept(event);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent inventoryOpenEvent) {
        decorate((Player) inventoryOpenEvent.getPlayer());
    }

    @Override
    public void onClose(InventoryCloseEvent event) { buttonMap.clear(); }

    protected InventoryButton createBackButton(@Nullable ManaChannels channel, ManaPacketType packet) {
        return new InventoryButton()
                .creator(p -> ItemCreator.create(Material.BARRIER, 1,
                        LocaleManager.get(p.locale(), "text.back", NamedTextColor.RED)))
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();

                    boolean clickSound = ManaServices.getSettingsService().getSettings(player.getUniqueId()).getBoolean(SettingKey.SOUND_GUI_EXIT);
                    if (clickSound) player.playSound(ConfigData.get().getClickSound());

                    boolean handled = ManaEnchants.getGUIManager().openBack(player);

                    if (handled) return;

                    if (channel == null) {
                        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        return;
                    }

                    player.sendPluginMessage(ManaEnchants.getInstance(), channel.getChannel(), new ManaPullPacket(packet).encode());
                });
    }

    protected void refresh() {
        inventory.clear();
        buttonMap.clear();
        fillInventory();
        decorate((Player) inventory.getViewers().getFirst());
    }
}