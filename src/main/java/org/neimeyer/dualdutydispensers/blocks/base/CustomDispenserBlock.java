package org.neimeyer.dualdutydispensers.blocks.base;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class CustomDispenserBlock extends CustomBlock {
    
    public CustomDispenserBlock(Location location, String blockKey) {
        super(location, blockKey);
    }
    
    /**
     * Whether this block allows natural dispenser behavior (ejecting items on redstone).
     * Override this method to customize behavior per block type.
     * 
     * @return true if natural dispenser behavior should be allowed, false to suppress it
     */
    public abstract boolean allowsNaturalDispensing();
    
    /**
     * Check if an item is valid for a specific slot in this dispenser.
     * Empty or null items are generally considered valid for any slot.
     * 
     * @param slot The slot number (0-8)
     * @param item The item to check (it may be null)
     * @return true if the item is valid for this slot
     */
    public boolean isValidItemForSlot(int slot, ItemStack item) {
        // Default implementation that handles null/air items
        return item == null || item.getType().isAir(); 
    }
    
    /**
     * Handles items being input from a hopper or other source
     * @param item The item being input
     * @return true if the item was accepted, false if it should be rejected
     */
    public abstract boolean handleHopperInput(ItemStack item);
    
    @Override
    public void onTick() {
        // Default implementation for dispenser blocks
        // Subclasses should call super.onTick() if they override this
    }
    
    protected Inventory getInventory() {
        Block block = location.getBlock();
        if (block.getType() == Material.DISPENSER) {
            Dispenser dispenser = (Dispenser) block.getState();
            return dispenser.getInventory();
        }
        return null;
    }
    
    protected BlockFace getFacing() {
        Block block = location.getBlock();
        if (block.getBlockData() instanceof Directional) {
            return ((Directional) block.getBlockData()).getFacing();
        }
        return BlockFace.NORTH;
    }
    
    protected Block getOutputBlock() {
        BlockFace face = getFacing();
        return location.getBlock().getRelative(face);
    }
    
    protected boolean canOutput(Block outputBlock) {
        return outputBlock.getState() instanceof org.bukkit.inventory.InventoryHolder || 
               outputBlock.getType().isAir();
    }
    
    protected void outputItems(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        Block outputBlock = getOutputBlock();
        
        if (outputBlock.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            Inventory targetInv = ((org.bukkit.inventory.InventoryHolder) outputBlock.getState()).getInventory();
            
            for (ItemStack item : items) {
                HashMap<Integer, ItemStack> leftover = targetInv.addItem(item);
                
                // Drop any items that don't fit
                for (ItemStack overflow : leftover.values()) {
                    outputBlock.getWorld().dropItemNaturally(outputBlock.getLocation(), overflow);
                }
            }
        } else {
            // Drop items naturally if no inventory
            for (ItemStack item : items) {
                outputBlock.getWorld().dropItemNaturally(outputBlock.getLocation(), item);
            }
        }
    }
    
    @Override
    public List<ItemStack> onBlockBreak() {
        List<ItemStack> drops = new ArrayList<>();
        
        // Add the custom block itself - get from config
        Material baseMaterial = getBaseMaterial();
        if (baseMaterial != null) {
            ItemStack customItem = DualDutyDispensers.getInstance().getConfigManager()
                .getBlockConfig(blockKey).createItemStack(baseMaterial);
            drops.add(customItem);
        }
        
        // Add inventory contents
        Inventory inv = getInventory();
        if (inv != null) {
            for (ItemStack item : inv.getContents()) {
                if (item != null && !item.getType().isAir()) {
                    drops.add(item);
                }
            }
        }
        
        return drops;
    }
    
    private Material getBaseMaterial() {
        // Dispenser-based blocks use DISPENSER
        return Material.DISPENSER;
    }
}