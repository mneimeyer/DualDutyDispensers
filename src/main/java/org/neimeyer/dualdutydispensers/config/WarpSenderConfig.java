package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;

public class WarpSenderConfig extends BlockConfig {
    
    public WarpSenderConfig() {
        super("warp-sender");
    }
    
    @Override
    public void setDefaults(YamlConfiguration config) {
    // Basic settings
    config.addDefault("name", "Warp Sender");
    config.addDefault("lore", Arrays.asList(
        "An evolved Ender Sender.",
        "Can be paired to multiple receivers."
    ));
    config.addDefault("custom-model-data", 44404);
    
    // Recipe: Echo Shard, Shulker Box, Echo Shard / Shulker Box, Ender Sender, Shulker Box / Echo Shard, Shulker Box, Echo Shard
    config.addDefault("recipe.item0", Material.ECHO_SHARD.name());
    config.addDefault("recipe.item1", Material.SHULKER_BOX.name());
    config.addDefault("recipe.item2", Material.ECHO_SHARD.name());
    config.addDefault("recipe.item3", Material.SHULKER_BOX.name());
    config.addDefault("recipe.item4", "ender-sender");
    config.addDefault("recipe.item5", Material.SHULKER_BOX.name());
    config.addDefault("recipe.item6", Material.ECHO_SHARD.name());
    config.addDefault("recipe.item7", Material.SHULKER_BOX.name());
    config.addDefault("recipe.item8", Material.ECHO_SHARD.name());
}
}