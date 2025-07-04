package org.neimeyer.dualdutydispensers.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.config.BlockConfig;

import java.util.Map;

public class RecipeManager {
    private final DualDutyDispensers plugin;
    
    public RecipeManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }
    
    public void registerAllRecipes() {
        registerBlockRecipe("tree-farm", Material.DISPENSER);
        registerBlockRecipe("redstone-clock", Material.OBSERVER);
        
        // TODO: Add other recipes as blocks are implemented
    }
    
    private void registerBlockRecipe(String blockKey, Material baseMaterial) {
        if (!plugin.getConfigManager().isBlockEnabled(blockKey) || 
            !plugin.getConfigManager().isRecipeEnabled(blockKey)) {
            return;
        }
        
        BlockConfig config = plugin.getConfigManager().getBlockConfig(blockKey);
        if (config == null) {
            plugin.getLogger().warning("No config found for block: " + blockKey);
            return;
        }
        
        Map<String, Material> recipeMap = config.getRecipe();
        if (recipeMap.isEmpty()) {
            plugin.getLogger().warning("No recipe defined for block: " + blockKey);
            return;
        }
        
        try {
            NamespacedKey key = new NamespacedKey(plugin, blockKey.replace("-", "_"));
            ShapedRecipe recipe = new ShapedRecipe(key, config.createItemStack(baseMaterial));
            
            // Set the shape (always 3x3)
            recipe.shape("ABC", "DEF", "GHI");
            
            // Map ingredients
            char[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
            for (int i = 0; i < 9; i++) {
                String itemKey = "item" + i;
                Material material = recipeMap.get(itemKey);
                if (material != null) {
                    recipe.setIngredient(chars[i], material);
                } else {
                    plugin.getLogger().warning("Missing recipe ingredient " + itemKey + " for " + blockKey);
                    return;
                }
            }
            
            plugin.getServer().addRecipe(recipe);
            plugin.getLogger().info("Registered recipe for " + config.getName());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register recipe for " + blockKey + ": " + e.getMessage());
        }
    }
}