package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.manameta.api.core.commands.HelpID;
import net.manameta.api.core.commands.ParentCommand;
import net.manameta.api.core.services.ManaServices;
import net.manameta.api.core.settings.enums.SettingKey;
import net.manameta.manaenchants.commands.core.C_Help;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.locale.LocaleManager;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public final class MessageHelpers {
    public static void error(@NotNull Audience sender, @NotNull Component prefix, @NotNull String translateKey, Component... components) {
        Locale locale = sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale());
        sender.sendMessage(prefix.append(LocaleManager.get(locale, translateKey, NamedTextColor.RED, components)));

        SoundHelpers.errorSound(sender);
    }

    public static void success(@NotNull Audience sender, @NotNull Component prefix, @NotNull String translateKey, Component... components) {
        Locale locale = sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale());
        sender.sendMessage(prefix.append(LocaleManager.get(locale, translateKey, NamedTextColor.GREEN, components)));

        SoundHelpers.successSound(sender);
    }

    public static void formatError(@NotNull Audience sender, @NotNull Component prefix, @NotNull ParentCommand parentCommand, @NotNull HelpID helpID) {
        sender.sendMessage(prefix.append(Component.translatable("commands.error.format", NamedTextColor.RED)));
        C_Help.showEntry(sender, parentCommand, helpID);

        SoundHelpers.errorSound(sender);
    }

    public static void sendPaginationFooter(@NotNull Audience sender, int currentPage, int maxPage,
                                            @NotNull Consumer<? super Audience> onClickPrevious,
                                            @NotNull Consumer<? super Audience> onClickNext) {
        if (maxPage <= 1) return;

        Locale locale = sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale());


        UUID userID = sender.get(Identity.UUID).orElse(new UUID(0,0));
        Component footer = Component.empty();

        // Previous page
        if (currentPage > 1) {
            Component prev = LocaleManager.get(locale, "previous", NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(LocaleManager.get(locale, "previous.hover", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        boolean clickSound = ManaServices.getSettingsService().getSettings(userID).getBoolean(SettingKey.SOUND_CLICK);
                        if (clickSound) SoundHelpers.clickSound(sender);
                        onClickPrevious.accept(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()));

            footer = footer.append(prev);
        } else {
            footer = footer.append(LocaleManager.get(locale, "spacer", NamedTextColor.WHITE));
        }

        // Current page
        footer = footer.append(LocaleManager.get(locale, "paginator", NamedTextColor.GRAY, Component.text(currentPage), Component.text(maxPage)));

        // Next page
        if (currentPage < maxPage) {
            Component next = LocaleManager.get(locale, "next", NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(LocaleManager.get(locale, "next.hover", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        boolean clickSound = ManaServices.getSettingsService().getSettings(userID).getBoolean(SettingKey.SOUND_CLICK);
                        if (clickSound) SoundHelpers.clickSound(sender);
                        onClickNext.accept(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()));

            footer = footer.append(next);
        }

        sender.sendMessage(footer);
    }
}
