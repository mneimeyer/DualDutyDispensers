package org.neimeyer.dualdutydispensers.utils;

import org.bukkit.Material;
import java.util.EnumSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class SoilHelper {

    private static final Set<Material> VALID_SOILS = EnumSet.of(
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.COARSE_DIRT,
            Material.PODZOL,
            Material.ROOTED_DIRT,
            Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.MOSS_BLOCK,
            Material.PALE_MOSS_BLOCK,
            Material.MYCELIUM,
            Material.CRIMSON_NYLIUM,
            Material.WARPED_NYLIUM,
            Material.END_STONE
    );

    private static final Map<Material, Set<Material>> SAPLING_SOIL_COMPATIBILITY = new HashMap<>();
    static {
        // Most saplings can grow on most soils
        Set<Material> commonSoils = EnumSet.of(
                Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, 
                Material.ROOTED_DIRT, Material.MUD, Material.MUDDY_MANGROVE_ROOTS, 
                Material.MOSS_BLOCK, Material.PALE_MOSS_BLOCK
        );

        SAPLING_SOIL_COMPATIBILITY.put(Material.OAK_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.BIRCH_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.SPRUCE_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.JUNGLE_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.ACACIA_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.DARK_OAK_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.CHERRY_SAPLING, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.PALE_OAK_SAPLING, commonSoils);

        // Mangrove has special requirements
        SAPLING_SOIL_COMPATIBILITY.put(Material.MANGROVE_PROPAGULE, EnumSet.of(
                Material.DIRT, Material.MUD, Material.MUDDY_MANGROVE_ROOTS, Material.GRASS_BLOCK
        ));

        // Azaleas can grow on most soils
        SAPLING_SOIL_COMPATIBILITY.put(Material.AZALEA, commonSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.FLOWERING_AZALEA, commonSoils);
        
        // Nether fungi (only nylium)
        SAPLING_SOIL_COMPATIBILITY.put(Material.CRIMSON_FUNGUS, EnumSet.of(Material.CRIMSON_NYLIUM));
        SAPLING_SOIL_COMPATIBILITY.put(Material.WARPED_FUNGUS, EnumSet.of(Material.WARPED_NYLIUM));
        
        // Mushrooms (wide variety of soils including nylium)
        Set<Material> mushroomSoils = EnumSet.of(
            Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT, 
            Material.MUD, Material.MUDDY_MANGROVE_ROOTS, Material.MOSS_BLOCK, Material.PALE_MOSS_BLOCK,
            Material.PODZOL, Material.MYCELIUM, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM
        );
        SAPLING_SOIL_COMPATIBILITY.put(Material.RED_MUSHROOM, mushroomSoils);
        SAPLING_SOIL_COMPATIBILITY.put(Material.BROWN_MUSHROOM, mushroomSoils);
        
        // Chorus (only end stone)
        SAPLING_SOIL_COMPATIBILITY.put(Material.CHORUS_FLOWER, EnumSet.of(Material.END_STONE));
    }

    // Only specific tree types convert soil
    private static final Map<Material, Material> SOIL_CONVERSION = new HashMap<>();
    static {
        // Only 2x2 Spruce trees convert soil to podzol
        SOIL_CONVERSION.put(Material.SPRUCE_SAPLING, Material.PODZOL);

        // Azaleas convert soil to rooted dirt
        SOIL_CONVERSION.put(Material.AZALEA, Material.ROOTED_DIRT);
        SOIL_CONVERSION.put(Material.FLOWERING_AZALEA, Material.ROOTED_DIRT);

        // Note: Other trees like oak, birch, etc. do NOT convert soil
    }

    // Plants that require bonemeal to grow
    private static final Set<Material> REQUIRES_BONEMEAL = Set.of(
        Material.CRIMSON_FUNGUS,
        Material.WARPED_FUNGUS,
        Material.RED_MUSHROOM,
        Material.BROWN_MUSHROOM
    );

    public static boolean isSoil(Material material) {
        return VALID_SOILS.contains(material);
    }

    public static boolean isCompatible(Material sapling, Material soil) {
        Set<Material> compatibleSoils = SAPLING_SOIL_COMPATIBILITY.get(sapling);
        return compatibleSoils != null && compatibleSoils.contains(soil);
    }

    public static Material getConvertedSoil(Material sapling) {
        return SOIL_CONVERSION.get(sapling); // Returns null if no conversion
    }

    public static boolean convertsSoil(Material sapling) {
        return SOIL_CONVERSION.containsKey(sapling);
    }
    
    /**
     * Check if a sapling/fungus requires bonemeal to grow
     */
    public static boolean requiresBonemeal(Material saplingType) {
        return REQUIRES_BONEMEAL.contains(saplingType);
    }
    
}