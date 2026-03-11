package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.manameta.manaenchants.commands.core.C_Help;
import net.manameta.manaenchants.common.config.ConfigData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Consumer;

public final class MessageHelpers {
    public static void error(@NotNull Audience sender, @NotNull Component prefix, @NotNull String translateKey, Component... components) {
        sender.sendMessage(prefix.append(Component.translatable(translateKey, ConfigData.get().getErrorColour(), components)));
        SoundHelpers.errorSound(sender);
    }

    public static void success(@NotNull Audience sender, @NotNull Component prefix, @NotNull String translateKey, Component... components) {
        sender.sendMessage(prefix.append(Component.translatable(translateKey, ConfigData.get().getSuccessColour(), components)));
        SoundHelpers.successSound(sender);
    }

    public static void formatError(@NotNull Audience sender, @NotNull ParentCommand parentCommand, @NotNull HelpID helpID) {
        C_Help.showEntry(sender, parentCommand, helpID);
        SoundHelpers.errorSound(sender);
    }

    public static void sendPaginationFooter(@NotNull Audience sender, int currentPage, int maxPage,
                                            @NotNull Consumer<? super Audience> onClickPrevious,
                                            @NotNull Consumer<? super Audience> onClickNext) {
        if (maxPage <= 1) return;

        Component footer = Component.empty();

        ConfigData config = ConfigData.get();
        // Previous page
        if (currentPage > 1) {
            Component prev = Component.translatable("previous", config.getPageColour())
                    .hoverEvent(HoverEvent.showText(Component.translatable("previous.hover", config.getHoverColour())))
                    .clickEvent(ClickEvent.callback(audience -> {
                        SoundHelpers.clickSound(sender);
                        onClickPrevious.accept(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()));

            footer = footer.append(prev);
        } else {
            footer = footer.append(Component.translatable("spacer"));
        }

        // Current page
        footer = footer.append(Component.translatable("paginator", config.getHeaderColour(), Component.text(currentPage), Component.text(maxPage)));

        // Next page
        if (currentPage < maxPage) {
            Component next = Component.translatable("next", config.getPageColour())
                    .hoverEvent(HoverEvent.showText(Component.translatable("next.hover", config.getHoverColour())))
                    .clickEvent(ClickEvent.callback(audience -> {
                        SoundHelpers.clickSound(sender);
                        onClickNext.accept(audience);
                    }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofHours(1)).build()));

            footer = footer.append(next);
        }

        sender.sendMessage(footer);
    }
}