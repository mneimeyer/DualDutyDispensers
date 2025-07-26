package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class TreeFarmConfig extends BlockConfig {
    
    public TreeFarmConfig() {
        super("tree-farm");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
        // Basic settings
        config.addDefault("name", "Tree Farm");
        config.addDefault("lore", "Mother Nature's best kept secret.");
        config.addDefault("custom-model-data", 44401);
        
        // Recipe: Redstone Block, Diamond Axe, Redstone Block / Diamond Axe, Dispenser, Diamond Axe / Redstone Block, Diamond Axe, Redstone Block
        config.addDefault("recipe.item0", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item1", Material.DIAMOND_AXE.name());
        config.addDefault("recipe.item2", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item3", Material.DIAMOND_AXE.name());
        config.addDefault("recipe.item4", Material.DISPENSER.name());
        config.addDefault("recipe.item5", Material.DIAMOND_AXE.name());
        config.addDefault("recipe.item6", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item7", Material.DIAMOND_AXE.name());
        config.addDefault("recipe.item8", Material.REDSTONE_BLOCK.name());
        
        // Tree farm specific settings
        config.addDefault("base-growth-chance", 5.0);  // Match YAML
        config.addDefault("bone-meal-bonus", 20.0);    // Match YAML
        config.addDefault("require-soil", true);
        config.addDefault("consume-bone-meal", true);
    }
    
    /**
     * Get the base growth chance percentage
     */
    public double getBaseGrowthChance() {
        return config.getDouble("base-growth-chance", 70.0);
    }
    
    /**
     * Get the bonus growth chance from bone meal
     */
    public double getBoneMealBonus() {
        return config.getDouble("bone-meal-bonus", 30.0);
    }
    
    /**
     * Whether soil is required for tree growth
     */
    public boolean isRequireSoil() {
        return config.getBoolean("require-soil", true);
    }
    
    /**
     * Whether bone meal should be consumed when used
     */
    public boolean isConsumeBoneMeal() {
        return config.getBoolean("consume-bone-meal", true);
    }
}