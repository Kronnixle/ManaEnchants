package net.manameta.manaenchants.commands.enchant;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.manameta.manaenchants.common.config.ConfigData;
import net.manameta.manaenchants.common.helpers.MessageHelpers;
import net.manameta.manaenchants.common.helpers.PrefixHelpers;
import net.manameta.manaenchants.common.helpers.SoundHelpers;
import net.manameta.manaenchants.common.locale.LocaleManager;
import net.manameta.manaenchants.common.utils.ItemGroup;
import net.manameta.manaenchants.enchants.TieredEnchantsConfig;
import net.manameta.manaenchants.enchants.model.Rarity;
import net.manameta.manaenchants.enchants.model.TieredEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

final class C_EnchantInfo {
    /** Prevent instantization */
    private C_EnchantInfo() {}

    static int execute(@Nonnull Audience sender, @Nonnull String name) {

        TieredEnchant tieredEnchant = TieredEnchantsConfig.get().getEnchant(name);

        if (tieredEnchant == null) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.enchant.not.found", Component.text(name, NamedTextColor.GRAY));
            return 0;
        }

        List<Component> description = buildEnchantmentDescription(sender.getOrDefault(Identity.LOCALE, ConfigData.get().getDefaultLocale()), tieredEnchant);

        for (Component component : description) {
            sender.sendMessage(component);
        }

        SoundHelpers.successSound(sender);

        return 1;
    }

    static int execute(@Nonnull Audience sender) {
        if (!(sender instanceof Player player)) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.error.player.command");
            return 0;
        }

        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.isEmpty() || item.getType() == Material.AIR) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.core.item.missing");
            return 0;
        }


        if (item.getEnchantments().isEmpty()) {
            MessageHelpers.error(sender, PrefixHelpers.ENCHANT_PREFIX, "commands.enchant.empty");
            return 0;
        }

        SoundHelpers.successSound(sender);

        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.translatable("enchantments.display.item.header", NamedTextColor.GOLD,
                item.displayName()));
        sender.sendMessage(Component.empty());

        for (var entry : item.getEnchantments().entrySet()) {
            TieredEnchant tieredEnchant = TieredEnchantsConfig.get().getEnchant(entry.getKey().key().asMinimalString());

            if (tieredEnchant == null) continue;

            sender.sendMessage(buildItemEnchantLine(tieredEnchant, entry.getValue()));
        }
        sender.sendMessage(Component.empty());

        return 1;
    }

    private static @NonNull List<Component> buildEnchantmentDescription(@Nonnull Locale locale, @NonNull TieredEnchant tieredEnchant) {

        Enchantment enchantment = tieredEnchant.resolveEnchantment();
        if (enchantment == null) return Collections.emptyList();

        int maxLevel = tieredEnchant.maxLevel();
        Rarity rarity = tieredEnchant.rarity();
        Set<Material> appliesTo = tieredEnchant.appliesTo();

        Component name = enchantment.description().color(NamedTextColor.YELLOW);

        Component rarityValue = Component.translatable("enchantments.rarity." + rarity.name().toLowerCase(), rarity.getColor())
                .hoverEvent(buildRarityHover(rarity));

        Component rarityLine = Component.translatable("enchantments.display.rarity", NamedTextColor.YELLOW, rarityValue);

        Component maxLevelLine = Component.translatable("enchantments.display.max_level", NamedTextColor.YELLOW,
                Component.text(maxLevel, NamedTextColor.WHITE));

        Component appliesToLine = Component.translatable("enchantments.display.applies_to", NamedTextColor.YELLOW,
                buildAppliesToComponent(appliesTo));

        String mm = LocaleManager.resolve(locale, "enchantments.display." + tieredEnchant.key() + ".description");

        Component description = MiniMessage.miniMessage().deserialize(mm).colorIfAbsent(NamedTextColor.GRAY);

        return List.of(
                Component.empty(),
                name,
                Component.empty(),
                rarityLine,
                maxLevelLine,
                appliesToLine,
                Component.empty(),
                description
        );
    }

    private static @NonNull Component buildAppliesToComponent(@NonNull Iterable<Material> materials) {

        Map<ItemGroup, List<Material>> grouped = ItemGroup.groupMaterials(materials);

        Collection<Component> components = new ArrayList<>();

        for (Map.Entry<ItemGroup, List<Material>> entry : grouped.entrySet()) {

            ItemGroup group = entry.getKey();
            List<Material> groupMaterials = entry.getValue();

            Component groupComponent = Component.text(formatGroupName(group), NamedTextColor.WHITE, TextDecoration.ITALIC)
                    .hoverEvent(buildMaterialHover(groupMaterials));

            components.add(groupComponent);
        }

        return Component.join(JoinConfiguration.commas(true), components);
    }

    @Contract(pure = true)
    private static @NonNull String formatGroupName(@NonNull ItemGroup group) {
        return switch (group) {
            case PICKAXE -> "Pickaxes";
            case AXE -> "Axes";
            case SHOVEL -> "Shovels";
            case HOE -> "Hoes";
            case SHEARS -> "Shears";
            case HELMET -> "Helmet";
            case CHEST -> "Chest";
            case LEGGINGS -> "Leggings";
            case BOOTS -> "Boots";
            default -> "Items";
        };
    }

    private static @NonNull Component buildMaterialHover(@NonNull Iterable<Material> materials) {

        Collection<Component> lines = new ArrayList<>();

        for (Material material : materials) {
            lines.add(Component.translatable(material.translationKey())
                    .color(NamedTextColor.WHITE));
        }

        return Component.join(JoinConfiguration.separator(Component.newline()), lines);
    }

    private static @NonNull Component buildItemEnchantLine(@NonNull TieredEnchant tieredEnchant, int level) {
        Enchantment enchantment = tieredEnchant.resolveEnchantment();
        if (enchantment == null) return Component.empty();

        Component enchantName = enchantment.description().color(NamedTextColor.YELLOW);


        Component levelComponent = Component.translatable("enchantments.display.level", NamedTextColor.GRAY,
                Component.text(level, NamedTextColor.YELLOW),
                Component.text(tieredEnchant.maxLevel(), NamedTextColor.YELLOW));

        Component description = Component.translatable("enchantments.display." + tieredEnchant.key() + ".short", NamedTextColor.GRAY);

        return Component.translatable("enchantments.display.item.format", NamedTextColor.GRAY,
                enchantName,
                levelComponent,
                description)
                .clickEvent(ClickEvent.callback(audience -> execute(audience, tieredEnchant.key())))
                .hoverEvent(Component.translatable("text.click.details", NamedTextColor.GRAY));
    }

    private static @NonNull Component buildRarityHover(@NonNull Rarity current) {
        Collection<Component> lines = new ArrayList<>();

        lines.add(Component.translatable("enchantments.rarity", NamedTextColor.GOLD));
        lines.add(Component.empty());

        for (Rarity rarity : Rarity.values()) {
            boolean selected = rarity == current;

            TextColor color = rarity.getColor();

            Component line = Component.translatable("enchantments.rarity." + rarity.name().toLowerCase(), color);

            if (selected) {
                line = line.append(Component.text(" ←", NamedTextColor.GREEN));
            }

            lines.add(line);
        }

        return Component.join(JoinConfiguration.separator(Component.newline()), lines);
    }
}