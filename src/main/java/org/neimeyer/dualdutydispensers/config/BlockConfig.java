package org.neimeyer.dualdutydispensers.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockConfig {
    protected final FileConfiguration config;
    protected final String blockType;
    
    public BlockConfig(FileConfiguration config, String blockType) {
        this.config = config;
        this.blockType = blockType;
    }
    
    public String getName() {
        return config.getString("name", blockType);
    }
    
    public String getLore() {
        return config.getString("lore", "A custom block.");
    }
    
    public int getCustomModelData() {
        return config.getInt("custom-model-data", 44401);
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
            // Use modern Paper 1.21+ Component-based methods
            meta.displayName(Component.text(getName()).color(NamedTextColor.WHITE));
            meta.lore(List.of(Component.text(getLore()).color(NamedTextColor.GRAY)));
            meta.setCustomModelData(getCustomModelData());
            item.setItemMeta(meta);
        }
        
        return item;
    }
}