package net.manameta.manaenchants.common.utils;

import org.bukkit.Material;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum ItemGroup {
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    SHEARS,
    HELMET,
    CHEST,
    LEGGINGS,
    BOOTS,
    BOWS,
    RODS,
    MACE,
    TRIDENT,
    SHIELD,
    ELYTRA,
    BRUSH,
    SPEAR,
    SWORDS,
    FLINT_AND_STEEL,
    OTHER;

    private static @NonNull ItemGroup resolveToolGroup(@NonNull Material material) {
        String name = material.name();

        if (material == Material.ELYTRA) return ELYTRA;
        if (material == Material.SHEARS) return SHEARS;
        if (material == Material.BRUSH) return BRUSH;
        if (material == Material.MACE) return MACE;
        if (material == Material.TRIDENT) return TRIDENT;
        if (material == Material.SHIELD) return SHIELD;
        if (material == Material.FLINT_AND_STEEL) return FLINT_AND_STEEL;

        if (name.endsWith("_SWORD")) return SWORDS;
        if (name.endsWith("_SPEAR")) return SPEAR;
        if (name.endsWith("_PICKAXE")) return PICKAXE;
        if (name.endsWith("_AXE")) return AXE;
        if (name.endsWith("_SHOVEL")) return SHOVEL;
        if (name.endsWith("_HOE")) return HOE;
        if (name.endsWith("_HELMET")) return HELMET;
        if (name.endsWith("_CHESTPLATE")) return CHEST;
        if (name.endsWith("_LEGGINGS")) return LEGGINGS;
        if (name.endsWith("_BOOTS")) return BOOTS;
        if (name.endsWith("BOW")) return BOWS;
        if (name.endsWith("ROD") || name.endsWith("_STICK")) return RODS;

        return OTHER;
    }

    public static Map<ItemGroup, List<Material>> groupMaterials(Iterable<Material> materials) {

        Map<ItemGroup, List<Material>> map = new LinkedHashMap<>();

        for (Material material : materials) {

            ItemGroup group = resolveToolGroup(material);

            map.computeIfAbsent(group, g -> new ArrayList<>()).add(material);
        }

        return map;
    }
}