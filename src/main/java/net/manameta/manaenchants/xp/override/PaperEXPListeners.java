package net.manameta.manaenchants.xp.override;

import net.manameta.manaenchants.common.config.ConfigData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jspecify.annotations.NonNull;

public class PaperEXPListeners implements Listener {

    @EventHandler
    public void onPlayerExpChange(@NonNull PlayerLevelChangeEvent e){
        if (ConfigData.get().isVanillaXP()) return;

        if (e.getNewLevel() <= e.getOldLevel()) return;

        e.getPlayer().playSound(ConfigData.get().getLevelUpSound());
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpChange(@NonNull PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();

        if (amount <= 0) return;

        XPManager.addXP(player, amount);

        event.setAmount(0);
    }
}