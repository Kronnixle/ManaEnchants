package net.manameta.manaenchants.enchants.session;

import net.manameta.manaenchants.common.gui.MerchantGUI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.view.MerchantView;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantSessionManager {
    private static final Map<UUID, EnchantSession> sessions = new HashMap<>();

    @ApiStatus.Experimental
    public static @NonNull EnchantSession create(@Nonnull Player player, @Nonnull Location enchantLocation,
                                                 @Nonnull MerchantView merchantView, @Nonnull MerchantGUI gui) {

        EnchantSession session = new EnchantSession(player, enchantLocation, merchantView, gui);
        sessions.put(player.getUniqueId(), session);

        return session;
    }

    public static EnchantSession get(@NonNull UUID uuid) {
        return sessions.get(uuid);
    }

    public static void remove(@Nonnull UUID uuid) {
        sessions.remove(uuid);
    }
}