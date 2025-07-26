package org.neimeyer.dualdutydispensers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.neimeyer.dualdutydispensers.blocks.base.CustomBlock;
import org.neimeyer.dualdutydispensers.config.ConfigManager;
import org.neimeyer.dualdutydispensers.listeners.BlockEventListener;
import org.neimeyer.dualdutydispensers.listeners.CraftingEventListener;
import org.neimeyer.dualdutydispensers.listeners.InventoryEventListener;
import org.neimeyer.dualdutydispensers.managers.BlockManager;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;
import org.neimeyer.dualdutydispensers.managers.RecipeManager;

public final class DualDutyDispensers extends JavaPlugin {
    private static DualDutyDispensers instance;
    
    private ConfigManager configManager;
    private BlockManager blockManager;
    private BukkitTask blockTicker;
    
    @Override
    public void onEnable() {
        // Set instance
        instance = this;
        
        // Create config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        blockManager = new BlockManager(this);
        RecipeManager recipeManager = new RecipeManager(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new BlockEventListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryEventListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingEventListener(this), this);
        
        // Register recipes
        recipeManager.registerAllRecipes();
        
        // Load blocks
        blockManager.loadBlocks();
        
        // Start the block ticker task
        startBlockTicker();
        
        // Initialize EnderQueueManager
        EnderQueueManager.getInstance().initialize();
        
        getLogger().info("DualDutyDispensers has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save blocks
        if (blockManager != null) {
            blockManager.saveBlocks();
        }
        
        // Cancel the block ticker
        if (blockTicker != null) {
            blockTicker.cancel();
            blockTicker = null;
        }
        
        // Save ender queue data
        EnderQueueManager.getInstance().saveAllQueues();
        
        getLogger().info("DualDutyDispensers has been disabled!");
        instance = null;
    }
    
    private void startBlockTicker() {
        blockTicker = Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Tick all custom blocks
            if (blockManager != null) {
                for (CustomBlock block : blockManager.getAllBlocks()) {
                    try {
                        block.onTick();
                    } catch (Exception e) {
                        getLogger().warning("Error ticking block at " + block.getLocation() + ": " + e.getMessage());
                    }
                }
            }
        }, 20L, 10L); // Start after 1 second, run every 0.5 seconds
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public BlockManager getBlockManager() {
        return blockManager;
    }
    
    public static DualDutyDispensers getInstance() {
        return instance;
    }
}