package org.neimeyer.dualdutydispensers.utils;

import org.bukkit.Material;
import java.util.EnumSet;
import java.util.Set;

public class SaplingHelper {

    private static final Set<Material> SAPLINGS = EnumSet.of(
        // Traditional tree saplings
        Material.ACACIA_SAPLING,
        Material.AZALEA,
        Material.BIRCH_SAPLING,
        Material.CHERRY_SAPLING,
        Material.DARK_OAK_SAPLING,
        Material.FLOWERING_AZALEA,
        Material.JUNGLE_SAPLING,
        Material.MANGROVE_PROPAGULE,
        Material.OAK_SAPLING,
        Material.SPRUCE_SAPLING,
        Material.PALE_OAK_SAPLING,
        
        // Nether fungi
        Material.CRIMSON_FUNGUS,
        Material.WARPED_FUNGUS,
        
        // Mushrooms
        Material.RED_MUSHROOM,
        Material.BROWN_MUSHROOM,
        
        // Chorus plant
        Material.CHORUS_FLOWER
    );

    private static final Set<Material> LARGE_TREE_SAPLINGS = EnumSet.of(
        Material.DARK_OAK_SAPLING,
        Material.JUNGLE_SAPLING,
        Material.SPRUCE_SAPLING
    );

    public static boolean isSapling(Material material) {
        return SAPLINGS.contains(material);
    }

    public static boolean supports2x2(Material sapling) {
        return LARGE_TREE_SAPLINGS.contains(sapling);
    }
    /**
     * Check if a sapling type REQUIRES 2x2 planting (like Dark Oak)
     */
    public static boolean requires2x2(Material saplingType) {
        return saplingType == Material.DARK_OAK_SAPLING;
    }
    
    /**
     * Determine the actual planting mode and sapling consumption for a given scenario
     * @param saplingType The type of sapling
     * @param availableSaplings Determine how many saplings are available
     * @param availableSoil Determine how many soil blocks are available
     * @return TreePlantingResult indicating mode and consumption
     */
    public static TreePlantingResult determinePlantingMode(Material saplingType, int availableSaplings, int availableSoil) {
        boolean canDo2x2 = supports2x2(saplingType);
        boolean must2x2 = requires2x2(saplingType);
        
        // Dark Oak MUST be 2x2, so fail if we don't have enough resources
        if (must2x2) {
            if (availableSaplings >= 4 && availableSoil >= 4) {
                return new TreePlantingResult(true, 4, 4); // 2x2 mode, consume 4 saplings, 4 soil
            } else {
                return new TreePlantingResult(false, 0, 0); // Cannot plant
            }
        }
        
        // For trees that CAN do 2x2 but don't require it (Spruce, Jungle)
        if (canDo2x2 && availableSaplings >= 4 && availableSoil >= 4) {
            return new TreePlantingResult(true, 4, 4); // Prefer 2x2 if possible
        }
        
        // Fallback to 1x1 (the only option for most trees)
        if (availableSaplings >= 1 && availableSoil >= 1) {
            return new TreePlantingResult(false, 1, 1); // 1x1 mode, consume 1 sapling, 1 soil
        }
        
        // Not enough resources
        return new TreePlantingResult(false, 0, 0);
    }
    
    /**
     * Result of determining planting mode
     */
    public record TreePlantingResult(boolean is2x2, int saplingsToConsume, int soilToConsume) {
        public boolean canPlant() {
            return saplingsToConsume > 0;
        }
    }
}