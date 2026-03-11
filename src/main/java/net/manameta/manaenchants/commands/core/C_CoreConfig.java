package net.manameta.manaenchants.commands.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.xp.model.LevelCost;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

final class C_CoreConfig {
    /** Prevent instantization */
    private C_CoreConfig() {}

    static int execute(@Nonnull Audience sender) {
        ConfigData config = ConfigData.get();

        TextColor headerColour = config.getHeaderColour();
        TextColor descriptionColour = config.getDescriptionColour();
        TextColor highlightColour = config.getDescriptionHighlightColour();

        Component header = Component.translatable("commands.config.header", headerColour);

        Component defaultLocale = Component.translatable("commands.config.default.locale", highlightColour,
                Component.text(config.getDefaultLocale().getDisplayName(), descriptionColour));

        Component xpCosts = Component.translatable("commands.config.xp.costs", headerColour);
        for (LevelCost levelCost : config.getXpCosts()) {
            xpCosts = xpCosts.appendNewline()
                    .append(Component.translatable("commands.config.xp.costs.format", highlightColour,
                            Component.text(levelCost.minLevel(), descriptionColour),
                            Component.text(levelCost.formula(), descriptionColour))
                    );
        }

        Component deathPenalty = Component.translatable("commands.config.death.penalty", highlightColour,
                Component.text(config.getDeathPenalty(), descriptionColour));
        Component lostXP = Component.translatable("commands.config.lost.exp", highlightColour,
                Component.text(config.getLostEXP(), descriptionColour));

        Component soundMessage = Component.translatable("commands.core.config.sound", highlightColour,
                buildSoundComponent("commands.core.sound.error", config.getErrorSound()),
                buildSoundComponent("commands.core.sound.click", config.getClickSound()),
                buildSoundComponent("commands.core.sound.success", config.getSuccessSound()),
                buildSoundComponent("commands.core.sound.level_up", config.getLevelUpSound()),
                buildSoundComponent("commands.core.sound.enchant", config.getEnchantSound()));

        sender.sendMessage(Component.empty());
        sender.sendMessage(header);
        sender.sendMessage(Component.empty());
        sender.sendMessage(defaultLocale);
        sender.sendMessage(Component.empty());
        sender.sendMessage(xpCosts);
        sender.sendMessage(Component.empty());
        sender.sendMessage(deathPenalty);
        sender.sendMessage(lostXP);
        sender.sendMessage(Component.empty());
        sender.sendMessage(soundMessage);
        sender.sendMessage(Component.empty());
        return 1;
    }
    
    private static @NonNull Component buildSoundComponent(@Nonnull String soundKey, @Nonnull Sound sound) {

        return Component.translatable(soundKey, ConfigData.get().getDescriptionColour(), TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(Component.translatable("commands.core.config.sound.hover", ConfigData.get().getHoverColour())))
                .clickEvent(ClickEvent.callback(audience -> audience.playSound(sound), ClickCallback.Options.builder().uses(-1).build()));
    }
}
