package net.manameta.manaenchants.common.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.config.ConfigData;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ManaLogger {
    private static final Component PREFIX =  Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text(ManaEnchants.getInstance().getName(), NamedTextColor.GOLD))
            .append(Component.text("][", NamedTextColor.DARK_GRAY));

    private ManaLogger() {}

    public static void severe(String key, Object... args) {
        log(Level.SEVERE, key, TextColor.color(255, 85, 85), args);
    }

    public static void warning(String key, Object... args) {
        log(Level.WARNING, key, TextColor.color(255, 170, 0), args);
    }

    public static void info(String key, Object... args) {
        log(Level.INFO, key, TextColor.color(170, 170, 170), args);
    }

    public static void config(String key, Object... args) {
        log(Level.CONFIG, key, TextColor.color(218, 255, 127), args);
    }

    public static void fine(String key, Object... args) { log(Level.FINE, key, TextColor.color(85, 255, 127), args); }

    public static void finer(String key, Object... args) { log(Level.FINER, key, TextColor.color(161, 200, 186), args); }

    public static void finest(String key, Object... args) {
        log(Level.FINEST, key, TextColor.color(183, 228, 238), args);
    }

    private static void log(@NonNull Level level, String key, TextColor color, Object... args) {
        int configVal = ConfigData.get().getLogLevel().intValue();
        int msgVal = level.intValue();

        if (msgVal < configVal) return;

        Logger logger = ManaEnchants.getInstance().getLogger();
        String pattern = logger.getResourceBundle().getString(key);
        String formatted = MessageFormat.format(pattern, args);

        Bukkit.getConsoleSender().sendMessage(PREFIX
                .append(Component.text(level.getName(), color))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text(formatted, color)));
    }
}