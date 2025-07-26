package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpReceiverConfig extends BlockConfig {
    
    public WarpReceiverConfig() {
        super("warp-receiver");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
        // Basic settings
        config.addDefault("name", "Warp Receiver");
        config.addDefault("lore", "An evolved Ender Receiver for mass collection.");
        config.addDefault("custom-model-data", 44405);
        
        // Recipe: Echo Shard, Shulker Box, Echo Shard / Shulker Box, Ender Receiver, Shulker Box / Echo Shard, Shulker Box, Echo Shard
        config.addDefault("recipe.item0", Material.ECHO_SHARD.name());
        config.addDefault("recipe.item1", Material.SHULKER_BOX.name());
        config.addDefault("recipe.item2", Material.ECHO_SHARD.name());
        config.addDefault("recipe.item3", Material.SHULKER_BOX.name());
        config.addDefault("recipe.item4", "ENDER-RECEIVER"); // Special handling needed - use hyphen format
        config.addDefault("recipe.item5", Material.SHULKER_BOX.name());
        config.addDefault("recipe.item6", Material.ECHO_SHARD.name());
        config.addDefault("recipe.item7", Material.SHULKER_BOX.name());
        config.addDefault("recipe.item8", Material.ECHO_SHARD.name());
    }
}