package org.neimeyer.dualdutydispensers.blocks.base;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class CustomBlock {
    protected final Location location;
    protected final String blockKey;
    
    public CustomBlock(Location location, String blockKey) {
        this.location = location;
        this.blockKey = blockKey;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public String getBlockKey() {
        return blockKey;
    }
    
    /**
     * Called when this block is broken
     * @return A list of item drops when this block is broken
     */
    public abstract List<ItemStack> onBlockBreak();
    
    /**
     * Called when this block receives redstone activation
     * Override to implement custom behavior on redstone pulse
     */
    public void onRedstoneActivation() {
        // Default implementation does nothing
    }
    
    /**
     * Called on regular ticks for this block
     * Default implementation does nothing, override in subclasses
     */
    public void onTick() {
        // Default implementation does nothing
    }
    
    /**
     * Save custom data for this block to the plugin data store
     */
    public abstract void saveData();
    
    /**
     * Load custom data for this block from the plugin data store
     */
    public abstract void loadData();
}