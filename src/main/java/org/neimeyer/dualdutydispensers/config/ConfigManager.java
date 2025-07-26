package org.neimeyer.dualdutydispensers.config;

import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final DualDutyDispensers plugin;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    
    public ConfigManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    public void loadConfigs() {
        // Load master config first
        loadMasterConfig();
        
        // Create the config folder for individual configs
        File configFolder = new File(plugin.getDataFolder(), "configs");
        if (!configFolder.exists()) {
            boolean created = configFolder.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Failed to create config folder: " + configFolder.getPath());
            }
        }
        
        // Load all block configs using the uniform system
        loadBlockConfig("tree-farm", TreeFarmConfig.class);
        loadBlockConfig("redstone-clock", RedstoneClockConfig.class);
        loadBlockConfig("ender-sender", EnderSenderConfig.class);
        loadBlockConfig("ender-receiver", EnderReceiverConfig.class);
        loadBlockConfig("warp-sender", WarpSenderConfig.class);
        loadBlockConfig("warp-receiver", WarpReceiverConfig.class);
    }
    
    private void loadMasterConfig() {
        // The master config is the main plugin config
        plugin.getConfig().addDefault("blocks.tree-farm.enabled", true);
        plugin.getConfig().addDefault("blocks.redstone-clock.enabled", true);
        plugin.getConfig().addDefault("blocks.ender-sender.enabled", true);
        plugin.getConfig().addDefault("blocks.ender-receiver.enabled", true);
        plugin.getConfig().addDefault("blocks.warp-sender.enabled", true);
        plugin.getConfig().addDefault("blocks.warp-receiver.enabled", true);
        
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    private <T extends BlockConfig> void loadBlockConfig(String blockKey, Class<T> configClass) {
        try {
            File configFile = new File(plugin.getDataFolder(), "configs/" + blockKey + ".yml");
            
            // First, try to save the default file from resources if it doesn't exist
            if (!configFile.exists()) {
                try {
                    plugin.saveResource("configs/" + blockKey + ".yml", false);
                    plugin.getLogger().info("Created default config file for " + blockKey + " from resources");
                } catch (IllegalArgumentException e) {
                    // Resource file doesn't exist, create from Java defaults
                    plugin.getLogger().info("No resource file found for " + blockKey + ", creating from defaults");
                }
            }
            
            // Create the config instance
            T blockConfig = configClass.getDeclaredConstructor().newInstance();
            blockConfig.load(plugin);  // Fix: Pass the plugin instance
            blockConfigs.put(blockKey, blockConfig);
            
            plugin.getLogger().info("Loaded config for " + blockKey);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load config for " + blockKey + ": " + e.getMessage());
            // TODO: Use proper logging instead of printStackTrace
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get all available block keys
     */
    public Set<String> getBlockKeys() {
        return blockConfigs.keySet();
    }
    
    public BlockConfig getBlockConfig(String blockKey) {
        return blockConfigs.get(blockKey);
    }
    
    public TreeFarmConfig getTreeFarmConfig() {
        return (TreeFarmConfig) blockConfigs.get("tree-farm");
    }
    
    public RedstoneClockConfig getRedstoneClockConfig() {
        return (RedstoneClockConfig) blockConfigs.get("redstone-clock");
    }
    
    public boolean isBlockEnabled(String blockKey) {
        return plugin.getConfig().getBoolean("blocks." + blockKey + ".enabled", true);
    }

    /**
     * Check if recipes are enabled for a specific block type from master config
     * @param blockKey The block type key (e.g., "tree-farm", "redstone-clock")
     * @return true if recipes are enabled, false otherwise
     */
    public boolean isRecipeEnabled(String blockKey) {
        return plugin.getConfig().getBoolean("blocks." + blockKey + ".recipe-enabled", true);
    }
}