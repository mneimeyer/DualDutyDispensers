package org.neimeyer.dualdutydispensers.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.config.BlockConfig;

import java.util.Map;

public class RecipeManager {
    private final DualDutyDispensers plugin;
    
    public RecipeManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }
    
    public void registerAllRecipes() {
        // Register normal recipes with proper base materials
        registerBlockRecipe("tree-farm", Material.DISPENSER);
        registerBlockRecipe("redstone-clock", Material.OBSERVER);
        registerBlockRecipe("ender-sender", Material.DISPENSER);
        registerBlockRecipe("ender-receiver", Material.DISPENSER);
        
        // Register special Warp recipes that use custom items as ingredients
        registerWarpRecipes();
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
    
    private void registerWarpRecipes() {
        // Note: Warp recipes are handled by CraftingEventListener since they use custom items
        plugin.getLogger().info("Warp recipes handled by custom crafting events");
    }
    
    /**
     * Check if an ItemStack is one of our custom blocks
     */
    @SuppressWarnings("deprecation")
    public boolean isCustomBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return false;
        }
        
        int customModelData = meta.getCustomModelData();
        
        // Check against all our block types
        for (String blockKey : plugin.getConfigManager().getBlockKeys()) {
            BlockConfig config = plugin.getConfigManager().getBlockConfig(blockKey);
            if (config != null && config.getCustomModelData() == customModelData) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the block key for a custom item (used by CraftingEventListener)
     */
    @SuppressWarnings("deprecation")
    public String getBlockKeyFromItem(ItemStack item) {
        if (!isCustomBlock(item)) {
            return null;
        }
        
        int customModelData = item.getItemMeta().getCustomModelData();
        
        for (String blockKey : plugin.getConfigManager().getBlockKeys()) {
            BlockConfig config = plugin.getConfigManager().getBlockConfig(blockKey);
            if (config != null && config.getCustomModelData() == customModelData) {
                return blockKey;
            }
        }
        
        return null;
    }
}