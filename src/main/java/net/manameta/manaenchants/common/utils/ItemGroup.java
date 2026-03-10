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
    OTHER;

    private static @NonNull ItemGroup resolveToolGroup(@NonNull Material material) {
        String name = material.name();

        if (name.endsWith("_PICKAXE")) return PICKAXE;
        if (name.endsWith("_AXE")) return AXE;
        if (name.endsWith("_SHOVEL")) return SHOVEL;
        if (name.endsWith("_HOE")) return HOE;
        if (name.endsWith("_HELMET")) return HELMET;
        if (name.endsWith("_CHESTPLATE")) return CHEST;
        if (name.endsWith("_LEGGINGS")) return LEGGINGS;
        if (name.endsWith("_BOOTS")) return BOOTS;
        if (material == Material.SHEARS) return SHEARS;

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