package org.neimeyer.dualdutydispensers.blocks.base;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;

import java.util.List;

public abstract class EnderBaseBlock extends CustomDispenserBlock {
    
    protected String queueId; // The queue this block participates in
    
    public EnderBaseBlock(Location location, String blockKey) {
        super(location, blockKey);
    }
    
    @Override
    public void saveData() {
        // Save the queue ID to NBT for easy retrieval
        BlockState state = getLocation().getBlock().getState();
        if (state instanceof TileState tileState) {
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            
            NamespacedKey key = new NamespacedKey(DualDutyDispensers.getInstance(), "queueId");
            
            if (queueId != null) {
                container.set(key, PersistentDataType.STRING, queueId);
            } else {
                container.remove(key);
            }
            
            tileState.update();
        }
    }
    
    @Override
    public void loadData() {
        // Load the queue ID from NBT
        BlockState state = getLocation().getBlock().getState();
        if (state instanceof TileState tileState) {
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            
            NamespacedKey key = new NamespacedKey(DualDutyDispensers.getInstance(), "queueId");
            
            if (container.has(key, PersistentDataType.STRING)) {
                queueId = container.get(key, PersistentDataType.STRING);
                // Store the queue ID in NBT for easier debugging
                saveQueueIdToBlockNBT();
            }
        }
    }
    
    @Override
    public boolean allowsNaturalDispensing() {
        // Default implementation for ender blocks
        // Receivers should override to return true
        // Senders should override to return false
        return false;
    }
    
    /**
     * Checks if this block is currently paired (part of a queue)
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPaired() {
        return queueId != null && !queueId.isEmpty();
    }
    
    /**
     * Sets the queue ID for this block (public for pairing)
     */
    public void setQueueId(String queueId) {
        this.queueId = queueId;
        // Save the new queue ID to NBT
        saveData();
        // Also store it in block NBT for easier debugging
        saveQueueIdToBlockNBT();
    }

    /**
     * Gets the queue ID for this block
     * @return the queue ID, or null if not paired
     */
    public String getQueueId() {
        return queueId;
    }
    
    /**
     * Gets this block's location as a string key
     */
    protected String getLocationKey() {
        return EnderQueueManager.locationToString(getLocation());
    }
    
    @Override
    public List<ItemStack> onBlockBreak() {
        List<ItemStack> drops = super.onBlockBreak();
        
        // If this block is paired, check if there are items in the queue to drop
        if (isPaired()) {
            List<ItemStack> queueItems = EnderQueueManager.getInstance().getAndClearQueue(queueId);
            drops.addAll(queueItems);
            
            // Remove this block from all queues
            EnderQueueManager.getInstance().removeBlockFromAllQueues(getLocationKey());
        }
        
        return drops;
    }
    
    /**
     * All Ender blocks always drop queued items when broken (per spec)
     */
    protected boolean shouldDropQueuedItemsOnBreak() {
        return true;
    }
    
    /**
     * Common redstone activation setup - checks pairing
     * @return true if the block is paired and ready to operate, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean checkPairingAndLog() {
        return isPaired();
    }
    
    /**
     * Gets the first available item from the inventory
     * @return The first non-empty ItemStack, or null if inventory is empty
     */
    protected ItemStack getFirstAvailableItem() {
        Inventory inventory = getInventory();
        if (inventory == null) {
            return null;
        }
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir()) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Save the queue ID to the block's NBT
     */
    private void saveQueueIdToBlockNBT() {
        if (queueId == null) {
            return;
        }
        
        BlockState state = getLocation().getBlock().getState();
        if (state instanceof TileState tileState) {
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            
            NamespacedKey key = new NamespacedKey(DualDutyDispensers.getInstance(), "queueId");
            container.set(key, PersistentDataType.STRING, queueId);
            
            tileState.update();
        }
    }
}