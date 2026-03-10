package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.xp.model.LevelCost;

import javax.annotation.Nonnull;

final class C_CoreConfig {
    /** Prevent instantization */
    private C_CoreConfig() {}

    static int execute(@Nonnull Audience sender) {
        ConfigData config = ConfigData.get();

        Component header = Component.translatable("commands.config.header", NamedTextColor.AQUA);

        Component defaultLocale = Component.translatable("commands.config.default.locale", NamedTextColor.YELLOW,
                Component.text(config.getDefaultLocale().getDisplayName(), NamedTextColor.WHITE));

        Component logLevel = Component.translatable("commands.config.log.level", NamedTextColor.YELLOW,
                Component.text(config.getLogLevel().toString(), NamedTextColor.WHITE));

        Component xpCosts = Component.translatable("commands.config.xp.costs", NamedTextColor.GOLD);
        for (LevelCost levelCost : config.getXpCosts()) {
            xpCosts = xpCosts.appendNewline()
                    .append(Component.translatable("commands.config.xp.costs.format", NamedTextColor.YELLOW,
                            Component.text(levelCost.minLevel(), NamedTextColor.WHITE),
                            Component.text(levelCost.formula(), NamedTextColor.WHITE)));
        }

        Component deathPenalty = Component.translatable("commands.config.death.penalty", NamedTextColor.YELLOW,
                Component.text(config.getDeathPenalty(), NamedTextColor.WHITE));

        Component soundMessage = Component.translatable("commands.core.config.sound", NamedTextColor.YELLOW,
                buildSoundComponent("Error Sound", config.getErrorSound()),
                buildSoundComponent("Click Sound", config.getClickSound()),
                buildSoundComponent("Chat Notify Sound", config.getChatNotifySound()),
                buildSoundComponent("Success Sound", config.getSuccessSound()),
                buildSoundComponent("Level Up Sound", config.getLevelUpSound()));

        sender.sendMessage(Component.empty());
        sender.sendMessage(header);
        sender.sendMessage(Component.empty());
        sender.sendMessage(defaultLocale);
        sender.sendMessage(logLevel);
        sender.sendMessage(Component.empty());
        sender.sendMessage(xpCosts);
        sender.sendMessage(Component.empty());
        sender.sendMessage(deathPenalty);
        sender.sendMessage(Component.empty());
        sender.sendMessage(soundMessage);
        sender.sendMessage(Component.empty());
        return 1;
    }
    
    private static Component buildSoundComponent(@Nonnull String soundName, @Nonnull Sound sound) {
        return Component.text(soundName, NamedTextColor.WHITE, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(Component.translatable("commands.core.config.sound.hover", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> audience.playSound(sound), ClickCallback.Options.builder().uses(-1).build()));
    }
}
