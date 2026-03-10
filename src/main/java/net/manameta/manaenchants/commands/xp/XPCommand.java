package net.manameta.manaenchants.commands.xp;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.manameta.api.core.commands.HelpID;
import net.manameta.api.core.commands.ParentCommand;
import net.manameta.manaenchants.commands.core.C_Help;
import net.manameta.manaenchants.common.config.CommandConfig;
import net.manameta.manaenchants.common.helpers.PermissionHelpers;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class XPCommand {
    /** Prevent instantization */
    private XPCommand() {}

    public static void register(Commands commands) {
        CommandConfig commandConfig = CommandConfig.get();

        List<String> aliases = commandConfig.getEntry(ParentCommand.XP, HelpID.ROOT).aliases();
        for (String alias : aliases) {

            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(alias);

            buildDefault(root);
            buildHelpCommand(root);
            buildAddCommand(root);
            buildClearCommand(root);
            buildInfoCommand(root);
            buildRemoveCommand(root);
            buildSetCommand(root);

            commands.register(root.build());
        }
    }

    private static void buildDefault(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        root.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_INFO));

        root.executes(ctx -> C_XPInfo.execute(ctx.getSource().getSender()));

        root.then(Commands.argument("player", StringArgumentType.word())
            .requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_INFO_OTHERS))
            .suggests((ctx, builder) -> {
                String input = builder.getRemainingLowerCase();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                }

                return builder.buildFuture();
            })
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                String playerResolver = StringArgumentType.getString(ctx, "player");

                return C_XPInfo.execute(sender, playerResolver);
            }));
    }

    private static void buildHelpCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.HELP).aliases()) {
            LiteralArgumentBuilder<CommandSourceStack> helpRoot = LiteralArgumentBuilder.literal(command);

            helpRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_HELP));

            helpRoot.executes(ctx -> C_Help.execute(ctx.getSource().getSender(), ParentCommand.XP, 1));

            helpRoot.then(Commands.argument("query", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (var entry : CommandConfig.get().getParent(ParentCommand.XP).values()) {
                            for (String alias : entry.aliases()) if (alias.startsWith(input)) builder.suggest(alias);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String query = StringArgumentType.getString(ctx, "query");

                        return C_Help.execute(sender, ParentCommand.XP, query);
                    }));

            root.then(helpRoot);
        }
    }

    private static void buildAddCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.ADD).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> addRoot = LiteralArgumentBuilder.literal(command);

            addRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_ADD));

            addRoot.executes(ctx -> C_XPAdd.execute(ctx.getSource().getSender()));

            // /xp add (player) (amount)
            addRoot.then(Commands.argument("player", StringArgumentType.word())
                    .then(Commands.argument("amount", StringArgumentType.word())
                            .executes(ctx -> {
                                CommandSender sender = ctx.getSource().getSender();
                                String playerResolver = StringArgumentType.getString(ctx, "player");
                                String amountResolver = StringArgumentType.getString(ctx, "amount");

                                return C_XPAdd.execute(sender, playerResolver, amountResolver);
                            }))
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String playerResolver = StringArgumentType.getString(ctx, "player");

                        return C_XPAdd.execute(sender, playerResolver, "1L");
                    }));

            root.then(addRoot);
        }
    }

    private static void buildClearCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.CLEAR).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> clearRoot = LiteralArgumentBuilder.literal(command);

            clearRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_CLEAR));

            clearRoot.executes(ctx -> C_XPClear.execute(ctx.getSource().getSender()));

            // /xp clear (player)
            clearRoot.then(Commands.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String playerResolver = StringArgumentType.getString(ctx, "player");

                        return C_XPClear.execute(sender, playerResolver);
                    }));

            root.then(clearRoot);
        }
    }

    private static void buildInfoCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.INFO).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> infoRoot = LiteralArgumentBuilder.literal(command);

            infoRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_INFO));

            infoRoot.executes(ctx -> C_XPInfo.execute(ctx.getSource().getSender()));

            // /xp info (player)
            infoRoot.then(Commands.argument("player", StringArgumentType.word())
                    .requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_INFO_OTHERS))
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String playerResolver = StringArgumentType.getString(ctx, "player");

                        return C_XPInfo.execute(sender, playerResolver);
                    }));

            root.then(infoRoot);
        }
    }

    private static void buildRemoveCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.REMOVE).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> removeRoot = LiteralArgumentBuilder.literal(command);

            removeRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_REMOVE));

            removeRoot.executes(ctx -> C_XPRemove.execute(ctx.getSource().getSender()));

            // /xp remove (player) (amount)
            removeRoot.then(Commands.argument("player", StringArgumentType.word())
                    .then(Commands.argument("amount", StringArgumentType.word())
                            .executes(ctx -> {
                                CommandSender sender = ctx.getSource().getSender();
                                String playerResolver = StringArgumentType.getString(ctx, "player");
                                String amountResolver = StringArgumentType.getString(ctx, "amount");

                                return C_XPRemove.execute(sender, playerResolver, amountResolver);
                            }))
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String playerResolver = StringArgumentType.getString(ctx, "player");

                        return C_XPRemove.execute(sender, playerResolver, "1L");
                    }));

            root.then(removeRoot);
        }
    }

    private static void buildSetCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.XP, HelpID.SET).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> setRoot = LiteralArgumentBuilder.literal(command);

            setRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.XP_SET));

            setRoot.executes(ctx -> C_XPSet.execute(ctx.getSource().getSender()));

            // /xp set (player) (amount)
            setRoot.then(Commands.argument("player", StringArgumentType.word())
                .then(Commands.argument("amount", StringArgumentType.word())
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String playerResolver = StringArgumentType.getString(ctx, "player");
                        String amountResolver = StringArgumentType.getString(ctx, "amount");

                        return C_XPSet.execute(sender, playerResolver, amountResolver);
                    }))
                .suggests((ctx, builder) -> {
                    String input = builder.getRemainingLowerCase();

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(input)) builder.suggest(p.getName());
                    }

                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();

                    return C_XPSet.execute(sender);
                }));

            root.then(setRoot);
        }
    }

}
