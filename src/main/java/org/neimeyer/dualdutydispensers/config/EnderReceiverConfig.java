package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class EnderReceiverConfig extends BlockConfig {
    
    public EnderReceiverConfig() {
        super("ender-receiver");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
        // Basic settings
        config.addDefault("name", "Ender Receiver");
        config.addDefault("lore", "Things appear from afar.");
        config.addDefault("custom-model-data", 44403);
        
        // Recipe: Obsidian, Eye of Ender, Obsidian / Dropper, Dispenser, Dropper / Obsidian, Eye of Ender, Obsidian
        config.addDefault("recipe.item0", Material.OBSIDIAN.name());
        config.addDefault("recipe.item1", Material.ENDER_EYE.name());
        config.addDefault("recipe.item2", Material.OBSIDIAN.name());
        config.addDefault("recipe.item3", Material.DROPPER.name());
        config.addDefault("recipe.item4", Material.DISPENSER.name());
        config.addDefault("recipe.item5", Material.DROPPER.name());
        config.addDefault("recipe.item6", Material.OBSIDIAN.name());
        config.addDefault("recipe.item7", Material.ENDER_EYE.name());
        config.addDefault("recipe.item8", Material.OBSIDIAN.name());
    }
}