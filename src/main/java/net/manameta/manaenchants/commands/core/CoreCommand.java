package net.manameta.manaenchants.commands.core;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.common.config.CommandConfig;
import net.manameta.manaenchants.common.helpers.PermissionHelpers;
import net.manameta.manaenchants.items.SavedItems;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class CoreCommand {
    /** Prevent instantiation */
    private CoreCommand() {}

    public static void register(Commands commands) {
        CommandConfig commandConfig = CommandConfig.get();

        List<String> aliases = commandConfig.getEntry(ParentCommand.MANAENCHANTS, HelpID.ROOT).aliases();
        for (String alias : aliases) {

            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(alias);

            buildDefault(root);
            buildHelpCommand(root);
            buildItemCommand(root);
            buildReloadCommand(root);
            buildConfigCommand(root);
            buildVersionCommand(root);

            commands.register(root.build());
        }
    }

    private static void buildDefault(@NotNull LiteralArgumentBuilder<? extends CommandSourceStack> root) {
        root.executes(ctx -> C_CoreVersion.execute(ctx.getSource().getSender()));
    }

    private static void buildHelpCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.MANAENCHANTS, HelpID.HELP).aliases()) {
            LiteralArgumentBuilder<CommandSourceStack> helpRoot = LiteralArgumentBuilder.literal(command);

            helpRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.MANAENCHANTS_HELP));
            helpRoot.executes(ctx -> C_Help.execute(ctx.getSource().getSender(), ParentCommand.MANAENCHANTS, 1));

            helpRoot.then(Commands.argument("query", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (var entry : CommandConfig.get().getParent(ParentCommand.MANAENCHANTS).values()) {
                            for (String alias : entry.aliases()) {
                                if (alias.startsWith(input)) builder.suggest(alias);
                            }
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String query = StringArgumentType.getString(ctx, "query");

                        return C_Help.execute(sender, ParentCommand.MANAENCHANTS, query);
                    }));

            root.then(helpRoot);
        }
    }

    private static final Set<String> itemSubcommands = Set.of("add", "remove", "info", "give", "replace", "clear", "list");
    private static void buildItemCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.MANAENCHANTS, HelpID.ITEM).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> itemRoot = LiteralArgumentBuilder.literal(command);

            itemRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.MANAENCHANTS_ITEM));

            itemRoot.executes(ctx -> C_CoreItem.execute(ctx.getSource().getSender()));

            itemRoot.then(Commands.argument("arg", StringArgumentType.word())
                            .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String input = builder.getRemainingLowerCase();

                                    String arg = StringArgumentType.getString(ctx, "arg");
                                    if (arg.equalsIgnoreCase("clear") ||
                                        arg.equalsIgnoreCase("list") ||
                                        arg.equalsIgnoreCase("add")) return builder.buildFuture();

                                    for (String item : SavedItems.get().getItems().keySet()) {
                                        if (item.startsWith(input)) builder.suggest(item);
                                    }


                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    String arg = StringArgumentType.getString(ctx, "arg");
                                    String name = StringArgumentType.getString(ctx, "name");

                                    return C_CoreItem.execute(sender, arg, name);
                                })
                            )
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (String subCommand : itemSubcommands) if (subCommand.startsWith(input)) builder.suggest(subCommand);

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String arg = StringArgumentType.getString(ctx, "arg");

                        return C_CoreItem.execute(sender, arg, null);
                    })
            );

            root.then(itemRoot);
        }
    }

    private static void buildReloadCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.MANAENCHANTS, HelpID.RELOAD).aliases()) {
            LiteralArgumentBuilder<CommandSourceStack> reloadRoot = LiteralArgumentBuilder.literal(command);

            reloadRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.MANAENCHANTS_RELOAD));

            reloadRoot.executes(ctx -> C_CoreReload.execute(ctx.getSource().getSender()));

            root.then(reloadRoot);
        }
    }

    private static void buildConfigCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.MANAENCHANTS, HelpID.CONFIG).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> configRoot = LiteralArgumentBuilder.literal(command);

            configRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.MANAENCHANTS_CONFIG));

            configRoot.executes(ctx -> C_CoreConfig.execute(ctx.getSource().getSender()));

            root.then(configRoot);
        }
    }

    private static void buildVersionCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.MANAENCHANTS, HelpID.VERSION).aliases()) {
            LiteralArgumentBuilder<CommandSourceStack> versionRoot = LiteralArgumentBuilder.literal(command);

            versionRoot.executes(ctx -> C_CoreVersion.execute(ctx.getSource().getSender()));

            root.then(versionRoot);
        }
    }
}
