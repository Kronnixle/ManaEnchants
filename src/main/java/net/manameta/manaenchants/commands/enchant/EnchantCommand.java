package net.manameta.manaenchants.commands.enchant;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.manameta.manaenchants.common.helpers.HelpID;
import net.manameta.manaenchants.common.helpers.ParentCommand;
import net.manameta.manaenchants.commands.core.C_Help;
import net.manameta.manaenchants.common.config.CommandConfig;
import net.manameta.manaenchants.common.helpers.PermissionHelpers;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnchantCommand {

    private EnchantCommand() {}

    public static void register(Commands commands) {
        CommandConfig commandConfig = CommandConfig.get();

        List<String> aliases = commandConfig.getEntry(ParentCommand.ENCHANT, HelpID.ROOT).aliases();
        for (String alias : aliases) {

            LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(alias);

            buildDefault(root);
            buildHelpCommand(root);
            buildAddCommand(root);
            buildClearCommand(root);
            buildListCommand(root);
            buildRemoveCommand(root);
            buildSetCommand(root);
            buildInfoCommand(root);

            commands.register(root.build());
        }
    }

    private static void buildDefault(@NotNull LiteralArgumentBuilder<? extends CommandSourceStack> root) {
        root.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_HELP));

        root.executes(ctx -> C_Help.execute(ctx.getSource().getSender(), ParentCommand.ENCHANT, 1));
    }

    private static void buildHelpCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.HELP).aliases()) {
            LiteralArgumentBuilder<CommandSourceStack> helpRoot = LiteralArgumentBuilder.literal(command);

            helpRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_HELP));

            helpRoot.executes(ctx -> C_Help.execute(ctx.getSource().getSender(), ParentCommand.ENCHANT, 1));

            helpRoot.then(Commands.argument("query", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (var entry : CommandConfig.get().getParent(ParentCommand.ENCHANT).values()) {
                            for (String alias : entry.aliases()) if (alias.startsWith(input)) builder.suggest(alias);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String query = StringArgumentType.getString(ctx, "query");

                        return C_Help.execute(sender, ParentCommand.ENCHANT, query);
                    }));

            root.then(helpRoot);
        }
    }

    private static void buildAddCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.ADD).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> addRoot = LiteralArgumentBuilder.literal(command);

            addRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_ADD));

            addRoot.executes(ctx -> C_EnchantAdd.execute(ctx.getSource().getSender()));

            addRoot.then(Commands.argument("name", StringArgumentType.word())
                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String name = StringArgumentType.getString(ctx, "name");
                        int level = IntegerArgumentType.getInteger(ctx, "level");

                        return C_EnchantAdd.execute(sender, name, level);
                    }))
            .suggests((ctx, builder) -> {
                String input = builder.getRemainingLowerCase();

                for (String enchantName : TieredEnchantsConfig.get().getEnchants().keySet()) {
                    if (enchantName.startsWith(input)) builder.suggest(enchantName);
                }

                return builder.buildFuture();
            })
            .executes(ctx -> {
                CommandSender sender = ctx.getSource().getSender();
                String name = StringArgumentType.getString(ctx, "name");

                return C_EnchantAdd.execute(sender, name, 1);
            }));

            root.then(addRoot);
        }
    }

    private static void buildClearCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.CLEAR).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> clearRoot = LiteralArgumentBuilder.literal(command);

            clearRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_CLEAR));

            clearRoot.executes(ctx -> C_EnchantClear.execute(ctx.getSource().getSender()));

            root.then(clearRoot);
        }
    }

    private static void buildInfoCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.INFO).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> addRoot = LiteralArgumentBuilder.literal(command);

            addRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_INFO));

            addRoot.executes(ctx -> C_EnchantInfo.execute(ctx.getSource().getSender()));

            addRoot.then(Commands.argument("name", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (String enchantName : TieredEnchantsConfig.get().getEnchants().keySet()) {
                            if (enchantName.startsWith(input)) builder.suggest(enchantName);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String name = StringArgumentType.getString(ctx, "name");

                        return C_EnchantInfo.execute(sender, name);
                    }));

            root.then(addRoot);
        }
    }

    private static void buildListCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.LIST).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> listRoot = LiteralArgumentBuilder.literal(command);

            listRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_LIST));

            listRoot.executes(ctx -> C_EnchantList.execute(ctx.getSource().getSender(), 1));

            listRoot.then(Commands.argument("page", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    int page = IntegerArgumentType.getInteger(ctx, "page");

                    return C_EnchantList.execute(sender, page);
                }));

            root.then(listRoot);
        }
    }

    private static void buildRemoveCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.REMOVE).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> removeRoot = LiteralArgumentBuilder.literal(command);

            removeRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_REMOVE));

            removeRoot.executes(ctx -> C_EnchantRemove.execute(ctx.getSource().getSender()));

            removeRoot.then(Commands.argument("name", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    String input = builder.getRemainingLowerCase();

                    if (!(ctx.getSource().getSender() instanceof Player player)) return builder.buildFuture();

                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    Set<String> enchantNames = mainHand.getEnchantments().keySet().stream()
                            .map(e -> e.key().asMinimalString())
                            .collect(Collectors.toSet());

                    for (String enchantName : enchantNames) {
                        if (enchantName.startsWith(input)) builder.suggest(enchantName);
                    }

                    return builder.buildFuture();
                })
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    String name = StringArgumentType.getString(ctx, "name");

                    return C_EnchantRemove.execute(sender, name);
                }));

            root.then(removeRoot);
        }
    }

    private static void buildSetCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> root) {
        for (String command : CommandConfig.get().getEntry(ParentCommand.ENCHANT, HelpID.SET).aliases()) {

            LiteralArgumentBuilder<CommandSourceStack> setRoot = LiteralArgumentBuilder.literal(command);

            setRoot.requires(ctx -> ctx.getSender().hasPermission(PermissionHelpers.ENCHANT_SET));

            setRoot.executes(ctx -> C_EnchantSet.execute(ctx.getSource().getSender()));

            setRoot.then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                                CommandSender sender = ctx.getSource().getSender();
                                String name = StringArgumentType.getString(ctx, "name");
                                int level = IntegerArgumentType.getInteger(ctx, "level");

                                return C_EnchantSet.execute(sender, name, level);
                            }))
                    .suggests((ctx, builder) -> {
                        String input = builder.getRemainingLowerCase();

                        for (String enchantName : TieredEnchantsConfig.get().getEnchants().keySet()) {
                            if (enchantName.startsWith(input)) builder.suggest(enchantName);
                        }

                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        String name = StringArgumentType.getString(ctx, "name");

                        return C_EnchantSet.execute(sender, name, 1);
                    }));

            root.then(setRoot);
        }
    }
}