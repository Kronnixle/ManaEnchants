package net.manameta.manaenchants.enchants.costs.catalyst;

import net.manameta.manaenchants.items.SavedItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public final class CatalystCost {

    private final @Nullable Material material;
    private final @Nullable String savedItem;
    private final int amount;

    public CatalystCost(@Nonnull Material material, int amount) {
        this.material = material;
        savedItem = null;
        this.amount = amount;
    }

    public CatalystCost(@Nonnull String savedItem, int amount) {
        material = null;
        this.savedItem = savedItem;
        this.amount = amount;
    }

    public @NonNull ItemStack getItem() {
        if (savedItem != null) {
            ItemStack base = SavedItems.get().get(savedItem);
            if (base == null) throw new RuntimeException("Could not find the saved item: " + savedItem);

            ItemStack clone = base.clone();
            clone.setAmount(amount);
            return clone;
        }

        assert material != null;
        return ItemStack.of(material, amount);
    }

    @Contract("_ -> new")
    @NonNull CatalystCost withAmount(int amount) {
        if (savedItem != null) {
            return new CatalystCost(savedItem, amount);
        }

        assert material != null;
        return new CatalystCost(material, amount);
    }
}