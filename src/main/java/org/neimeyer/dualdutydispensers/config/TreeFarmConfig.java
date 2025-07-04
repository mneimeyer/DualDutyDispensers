package org.neimeyer.dualdutydispensers.config;

import org.bukkit.configuration.file.FileConfiguration;

public class TreeFarmConfig extends BlockConfig {
    
    public TreeFarmConfig(FileConfiguration config) {
        super(config, "tree-farm");
    }
    
    public float getBaseGrowthChance() {
        return (float) config.getDouble("base-growth-chance", 5.0);
    }
    
    public float getBoneMealBonus() {
        return (float) config.getDouble("bone-meal-bonus", 20.0);
    }
    
    public boolean consumesBoneMeal() {
        return config.getBoolean("consume-bone-meal", true);
    }
    
    // Add the missing methods that TreeFarmBlock is trying to use
    public boolean isRequireSoil() {
        return config.getBoolean("require-soil", true);
    }
    
    public boolean isConsumeBoneMeal() {
        return consumesBoneMeal(); // Delegate to the existing method
    }
}