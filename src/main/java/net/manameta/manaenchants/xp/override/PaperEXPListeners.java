package net.manameta.manaenchants.xp.override;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.manameta.manaenchants.common.config.ConfigData;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jspecify.annotations.NonNull;

public class PaperEXPListeners implements Listener {

    @EventHandler
    public void onPlayerExpChange(@NonNull PlayerLevelChangeEvent e){
        e.getPlayer().playSound(ConfigData.get().getLevelUpSound());
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpChange(PlayerExpChangeEvent event) {
        if (!ConfigData.get().isXpEnabled()) return;

        Player player = event.getPlayer();
        int amount = event.getAmount();

        if (amount <= 0) return;

        // Cancel vanilla XP gain
        event.setAmount(0);

        // Add to custom XP system
        XPManager.addXP(player, amount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickupXP(PlayerPickupExperienceEvent event) {

        if (!ConfigData.get().isXpEnabled()) return;

        Player player = event.getPlayer();
        ExperienceOrb orb = event.getExperienceOrb();

        int xp = orb.getExperience();
        orb.setExperience(0);

        XPManager.addXP(player, xp);
    }
}