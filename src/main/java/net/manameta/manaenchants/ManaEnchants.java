package net.manameta.manaenchants;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.manameta.manaenchants.commands.core.CoreCommand;
import net.manameta.manaenchants.commands.enchant.EnchantCommand;
import net.manameta.manaenchants.commands.xp.XPCommand;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.config.ConfigurationManager;
import net.manameta.manaenchants.common.gui.GUIListener;
import net.manameta.manaenchants.common.gui.GUIManager;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.enchants.override.listeners.PlayerListeners;
import net.manameta.manaenchants.xp.override.PaperEXPListeners;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ManaEnchants extends JavaPlugin {

    private static ManaEnchants instance;
    public static ManaEnchants getInstance() { return instance; }

    private static GUIManager guiManager;
    public static GUIManager getGUIManager() { return guiManager; }

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationManager.initialize();

        // Load locale
        LocaleManager.loadAllLocales();

        Logger logger = getLogger();
        Locale locale = ConfigData.get().getDefaultLocale();
        logger.setResourceBundle(ResourceBundle.getBundle("locale.logger", locale));

        // Commands
        LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            CoreCommand.register(commands);
            EnchantCommand.register(commands);
            XPCommand.register(commands);
        });

        registerEvents();
    }

    @Override
    public void onDisable() {
        getLogger().info("[ManaEnchants] Disabled");
    }

    private void registerEvents() {
        guiManager = new GUIManager();
        getServer().getPluginManager().registerEvents(new GUIListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new PaperEXPListeners(), this);
    }
}