package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class EnderSenderConfig extends BlockConfig {
    
    public EnderSenderConfig() {
        super("ender-sender");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
        // Basic settings
        config.addDefault("name", "Ender Sender");
        config.addDefault("lore", "A tear in space swallows all around it.");
        config.addDefault("custom-model-data", 44402);
        
        // Recipe: Obsidian, Eye of Ender, Obsidian / Hopper, Dispenser, Hopper / Obsidian, Eye of Ender, Obsidian
        config.addDefault("recipe.item0", Material.OBSIDIAN.name());
        config.addDefault("recipe.item1", Material.ENDER_EYE.name());
        config.addDefault("recipe.item2", Material.OBSIDIAN.name());
        config.addDefault("recipe.item3", Material.HOPPER.name());
        config.addDefault("recipe.item4", Material.DISPENSER.name());
        config.addDefault("recipe.item5", Material.HOPPER.name());
        config.addDefault("recipe.item6", Material.OBSIDIAN.name());
        config.addDefault("recipe.item7", Material.ENDER_EYE.name());
        config.addDefault("recipe.item8", Material.OBSIDIAN.name());
    }
}