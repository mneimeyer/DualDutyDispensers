package org.neimeyer.dualdutydispensers.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final DualDutyDispensers plugin;
    private FileConfiguration masterConfig;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    
    public ConfigManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
        loadConfigs(); // Load configs immediately when the manager is created
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
        
        // Load individual block configs
        loadBlockConfig("tree-farm", TreeFarmConfig.class);
        loadBlockConfig("redstone-clock", RedstoneClockConfig.class);
        
        // TODO: Add other block configs as they're implemented
    }
    
    private void loadMasterConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        masterConfig = plugin.getConfig();
    }
    
    private void loadBlockConfig(String blockKey, Class<? extends BlockConfig> configClass) {
        if (!isBlockEnabled(blockKey)) {
            return;
        }
        
        FileConfiguration config = loadConfig(blockKey + ".yml");
        try {
            if (configClass == TreeFarmConfig.class) {
                blockConfigs.put(blockKey, new TreeFarmConfig(config));
            } else if (configClass == RedstoneClockConfig.class) {
                blockConfigs.put(blockKey, new RedstoneClockConfig(config));
            } else {
                blockConfigs.put(blockKey, new BlockConfig(config, blockKey));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load config for " + blockKey + ": " + e.getMessage());
        }
    }
    
    private FileConfiguration loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), "configs/" + fileName);
        if (!configFile.exists()) {
            try {
                plugin.saveResource("configs/" + fileName, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Default config not found for " + fileName + ", creating minimal config");
                return new YamlConfiguration();
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }
    
    // Master Config Methods
    public boolean isBlockEnabled(String blockKey) {
        String path = "blocks." + blockKey + ".enabled";
        return masterConfig.getBoolean(path, true);
    }
    
    public boolean isRecipeEnabled(String blockKey) {
        String path = "blocks." + blockKey + ".recipe-enabled";
        return masterConfig.getBoolean(path, true);
    }
    
    // Block Config Getters
    public BlockConfig getBlockConfig(String blockKey) {
        return blockConfigs.get(blockKey);
    }
    
    public TreeFarmConfig getTreeFarmConfig() {
        return (TreeFarmConfig) blockConfigs.get("tree-farm");
    }
    
    public RedstoneClockConfig getRedstoneClockConfig() {
        return (RedstoneClockConfig) blockConfigs.get("redstone-clock");
    }
    
    /**
     * Get all registered block keys
     * @return Set of block keys
     */
    public Set<String> getBlockKeys() {
        return blockConfigs.keySet();
    }
}