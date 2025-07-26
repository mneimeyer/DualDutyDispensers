package org.neimeyer.dualdutydispensers.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.EnderReceiverBlock;
import org.neimeyer.dualdutydispensers.blocks.EnderSenderBlock;
import org.neimeyer.dualdutydispensers.blocks.RedstoneClockBlock;
import org.neimeyer.dualdutydispensers.blocks.TreeFarmBlock;
import org.neimeyer.dualdutydispensers.blocks.WarpReceiverBlock;
import org.neimeyer.dualdutydispensers.blocks.WarpSenderBlock;
import org.neimeyer.dualdutydispensers.blocks.base.CustomBlock;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BlockManager {
    private final DualDutyDispensers plugin;
    private final Map<Location, CustomBlock> blocks = new ConcurrentHashMap<>();
    
    public BlockManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Get a custom block at the given location
     * @param location The location to check
     * @return The custom block, or null if none exists
     */
    public CustomBlock getBlock(Location location) {
        return blocks.get(location);
    }
    
    /**
     * Create and register a new custom block
     * @param location The location of the block
     * @param blockKey The key identifying the block type
     * @return The created custom block, or null if failed
     */
    public CustomBlock createBlock(Location location, String blockKey) {
        // Check if a block already exists at this location
        if (blocks.containsKey(location)) {
            return null;
        }
        
        // Create the appropriate block type
        CustomBlock block = switch (blockKey) {
            case "tree-farm" -> new TreeFarmBlock(location);
            case "redstone-clock" -> new RedstoneClockBlock(location);
            case "ender-sender" -> new EnderSenderBlock(location);
            case "ender-receiver" -> new EnderReceiverBlock(location);
            case "warp-sender" -> new WarpSenderBlock(location);
            case "warp-receiver" -> new WarpReceiverBlock(location);
            default -> null; // Add more block types here as they're implemented
        };
        
        if (block != null) {
            blocks.put(location, block);
        }
        
        return block;
    }
    
    /**
     * Remove a custom block from the registry
     * @param location The location of the block
     */
    public void removeBlock(Location location) {
        blocks.remove(location);
    }
    
    /**
     * Get all custom blocks registered in the manager
     * @return Collection of all custom blocks
     */
    public Collection<CustomBlock> getAllBlocks() {
        return blocks.values();
    }
    
    /**
     * Save all blocks to disk
     */
    public void saveBlocks() {
        try {
            // Create the data directory if it doesn't exist
            File dataDir = new File(plugin.getDataFolder(), "blocks");
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                if (!created) {
                    plugin.getLogger().warning("Failed to create blocks directory: " + dataDir.getPath());
                }
            }
            
            // Prepare location data
            List<String> locationData = prepareLocationData();
            
            // Save block locations to a file
            File locationsFile = new File(dataDir, "locations.txt");
            java.nio.file.Files.write(locationsFile.toPath(), locationData);

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save blocks", e);
        }
    }
    
    /**
     * Prepare location data for saving
     * @return List of strings with location data
     */
    private List<String> prepareLocationData() {
        List<String> locationData = new ArrayList<>();
        
        for (Map.Entry<Location, CustomBlock> entry : blocks.entrySet()) {
            Location loc = entry.getKey();
            CustomBlock block = entry.getValue();
            
            String locString = String.format("%s,%d,%d,%d,%s", 
                loc.getWorld().getName(), 
                loc.getBlockX(), 
                loc.getBlockY(), 
                loc.getBlockZ(),
                block.getBlockKey()
            );
            
            locationData.add(locString);
            
            // Save block-specific data
            block.saveData();
        }
        
        return locationData;
    }
    
    /**
     * Load all blocks from disk
     */
    public void loadBlocks() {
        blocks.clear();
        
        try {
            File locationsFile = new File(plugin.getDataFolder(), "blocks/locations.txt");
            if (!locationsFile.exists()) {
                return;
            }
            
            List<String> locationData = java.nio.file.Files.readAllLines(locationsFile.toPath());
            int loadedCount = 0;
            
            for (String line : locationData) {
                String[] parts = line.split(",");
                if (parts.length != 5) continue;
                
                try {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    String blockKey = parts[4];
                    
                    var world = plugin.getServer().getWorld(worldName);
                    if (world == null) {
                        plugin.getLogger().warning("World not found: " + worldName);
                        continue;
                    }
                    
                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();
                    
                    // Verify the block is still there
                    Material expectedType = getExpectedBlockType(blockKey);
                    if (block.getType() != expectedType) {
                        plugin.getLogger().warning("Block at " + loc + " is not " + 
                            expectedType + " (found " + block.getType() + ")");
                        continue;
                    }
                    
                    // Create and register the block
                    CustomBlock customBlock = createBlock(loc, blockKey);
                    if (customBlock != null) {
                        customBlock.loadData();
                        loadedCount++;
                    }
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load block: " + line + " - " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load blocks", e);
        }
    }
    
    private Material getExpectedBlockType(String blockKey) {
        return switch (blockKey) {
            case "tree-farm", "ender-receiver", "ender-sender", "warp-receiver", "warp-sender",
                 "compressor", "decompressor", "siphon", "block-breaker", "murder-block" -> Material.DISPENSER;
            case "redstone-clock" -> Material.OBSERVER;
            default -> Material.AIR;
        };
    }
}