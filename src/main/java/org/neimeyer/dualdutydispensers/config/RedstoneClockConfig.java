package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class RedstoneClockConfig extends BlockConfig {
    
    public RedstoneClockConfig() {
        super("redstone-clock");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
        // Basic settings
        config.addDefault("name", "Redstone Clock");
        config.addDefault("lore", "You hear soft ticking from inside.");
        config.addDefault("custom-model-data", 44499);
        
        // Recipe: Redstone Block, Repeater, Redstone Block / Comparator, Observer, Comparator / Redstone Block, Repeater, Redstone Block
        config.addDefault("recipe.item0", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item1", Material.REPEATER.name());
        config.addDefault("recipe.item2", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item3", Material.COMPARATOR.name());
        config.addDefault("recipe.item4", Material.OBSERVER.name());
        config.addDefault("recipe.item5", Material.COMPARATOR.name());
        config.addDefault("recipe.item6", Material.REDSTONE_BLOCK.name());
        config.addDefault("recipe.item7", Material.REPEATER.name());
        config.addDefault("recipe.item8", Material.REDSTONE_BLOCK.name());
        
        // Clock specific settings - use consistent naming
        config.addDefault("tick-interval", 8);
    }

    /**
     * Get the tick interval for redstone pulses
     * @return the number of ticks between pulses
     */
    public int getTickInterval() {
        return config.getInt("tick-interval", 8);
    }
}