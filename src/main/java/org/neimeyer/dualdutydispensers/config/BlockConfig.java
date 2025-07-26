package org.neimeyer.dualdutydispensers.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockConfig {
    protected FileConfiguration config;
    protected final String blockType;
    
    protected BlockConfig(String blockType) {
        this.blockType = blockType;
    }
    
    /**
     * Load the configuration for this block type
     */
    public void load(DualDutyDispensers plugin) {
        File configFile = new File(plugin.getDataFolder(), "configs/" + blockType + ".yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Set defaults
        setDefaults((YamlConfiguration) config);
        
        // Save the config with defaults
        config.options().copyDefaults(true);
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save config for " + blockType + ": " + e.getMessage());
        }
    }
    
    /**
     * Abstract method that subclasses must implement to set their defaults
     */
    public abstract void setDefaults(YamlConfiguration config);
    
    public String getName() {
        return config.getString("name", blockType);
    }
    
    public List<String> getLore() {
        if (config.isList("lore")) {
            return config.getStringList("lore");
        } else {
            // Handle backward compatibility with single-line lore
            String singleLore = config.getString("lore", "");
            return singleLore.isEmpty() ? new ArrayList<>() : List.of(singleLore);
        }
    }
    
    public int getCustomModelData() {
        return config.getInt("custom-model-data", 1000);
    }
    
    public Map<String, Material> getRecipe() {
        Map<String, Material> recipe = new HashMap<>();
        
        if (config.contains("recipe")) {
            var recipeSection = config.getConfigurationSection("recipe");
            if (recipeSection != null) {
                for (String key : recipeSection.getKeys(false)) {
                    String materialName = config.getString("recipe." + key);
                    if (materialName != null) {
                        try {
                            Material material = Material.valueOf(materialName.toUpperCase());
                            recipe.put(key, material);
                        } catch (IllegalArgumentException e) {
                            // Invalid material, skip
                        }
                    }
                }
            }
        }
        
        return recipe;
    }
    
    @SuppressWarnings("deprecation") // CMD still required for resource pack compatibility
    public ItemStack createItemStack(Material baseMaterial) {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getName());
            
            List<String> loreLines = getLore();
            if (!loreLines.isEmpty()) {
                meta.setLore(loreLines);
            }
            
            meta.setCustomModelData(getCustomModelData());
            item.setItemMeta(meta);
        }
        return item;
    }
}