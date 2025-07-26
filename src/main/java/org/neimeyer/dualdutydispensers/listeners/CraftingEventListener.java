package org.neimeyer.dualdutydispensers.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.managers.RecipeManager;

public class CraftingEventListener implements Listener {
    private final RecipeManager recipeManager;
    
    public CraftingEventListener(DualDutyDispensers plugin) {
        this.recipeManager = new RecipeManager(plugin);
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        
        // Check if any ingredients are our custom blocks
        boolean hasCustomBlocks = false;
        for (ItemStack item : matrix) {
            if (recipeManager.isCustomBlock(item)) {
                hasCustomBlocks = true;
                break;
            }
        }
        
        if (hasCustomBlocks) {
            // Check if this is a valid Warp recipe
            if (isValidWarpRecipe(matrix)) {
                // Set the result for Warp crafting
                ItemStack result = getWarpCraftingResult(matrix);
                event.getInventory().setResult(result);
            } else {
                // Block custom blocks from being used in other recipes
                event.getInventory().setResult(null);
            }
        }
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void onCraftItem(CraftItemEvent event) {
        // Handle the actual crafting when player takes the result
        // Vanilla Minecraft handles ingredient consumption automatically for custom recipes
        // We just need to validate that custom blocks are only used in valid recipes
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        
        // Check if any ingredients are our custom blocks
        boolean hasCustomBlocks = false;
        for (ItemStack item : matrix) {
            if (recipeManager.isCustomBlock(item)) {
                hasCustomBlocks = true;
                break;
            }
        }
        
        // If custom blocks are present, but it's not a valid Warp recipe, cancel the event
        if (hasCustomBlocks && !isValidWarpRecipe(matrix)) {
            event.setCancelled(true);
        }
    }
    
    private boolean isValidWarpRecipe(ItemStack[] matrix) {
        // Check for Warp recipe pattern: ABA / BCB / ABA
        // A = Echo Shard, B = Shulker Box, C = Ender Sender/Receiver
        
        if (matrix.length != 9) return false;
        
        // Expected pattern positions
        int[] echoShardPositions = {0, 2, 6, 8}; // corners
        int[] shulkerBoxPositions = {1, 3, 5, 7}; // sides
        int centerPosition = 4; // center
        
        // Check echo shards
        for (int pos : echoShardPositions) {
            if (matrix[pos] == null || matrix[pos].getType() != Material.ECHO_SHARD) {
                return false;
            }
        }
        
        // Check shulker boxes
        for (int pos : shulkerBoxPositions) {
            if (matrix[pos] == null || !isShulkerBox(matrix[pos].getType())) {
                return false;
            }
        }
        
        // Check center for Ender Sender or Ender Receiver
        if (matrix[centerPosition] == null) return false;
        String blockKey = recipeManager.getBlockKeyFromItem(matrix[centerPosition]);
        return "ender-sender".equals(blockKey) || "ender-receiver".equals(blockKey);
    }
    
    private ItemStack getWarpCraftingResult(ItemStack[] matrix) {
        // Get the center item to determine result type
        ItemStack centerItem = matrix[4];
        String centerBlockKey = recipeManager.getBlockKeyFromItem(centerItem);
        
        if ("ender-sender".equals(centerBlockKey)) {
            // Create Warp Sender
            return DualDutyDispensers.getInstance().getConfigManager()
                .getBlockConfig("warp-sender").createItemStack(Material.DISPENSER);
        } else if ("ender-receiver".equals(centerBlockKey)) {
            // Create Warp Receiver
            return DualDutyDispensers.getInstance().getConfigManager()
                .getBlockConfig("warp-receiver").createItemStack(Material.DISPENSER);
        }
        
        return null;
    }
    
    private boolean isShulkerBox(Material material) {
        return switch (material) {
            case SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX,
                 LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX, PINK_SHULKER_BOX,
                 GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX, CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX,
                 BLUE_SHULKER_BOX, BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX,
                 BLACK_SHULKER_BOX -> true;
            default -> false;
        };
    }
}