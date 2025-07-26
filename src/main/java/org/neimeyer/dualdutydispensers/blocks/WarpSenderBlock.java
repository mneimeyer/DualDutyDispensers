package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.blocks.base.EnderBaseBlock;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;

public class WarpSenderBlock extends EnderBaseBlock {

    public WarpSenderBlock(Location location) {
        super(location, "warp-sender");
    }

    @Override
    public void onRedstoneActivation() {
        if (!isPaired()) {
            return; // Can't send without being in a queue
        }

        // Get the first available item from inventory
        ItemStack itemToSend = getFirstAvailableItem();
        if (itemToSend == null) {
            return; // Nothing to send
        }

        // Create a single item to send
        ItemStack singleItem = itemToSend.clone();
        singleItem.setAmount(1);
        
        // Try to add to the queue
        boolean wasAdded = EnderQueueManager.getInstance().addToQueue(queueId, singleItem);
        
        if (wasAdded) {
            // Only remove from inventory if successfully added to queue
            itemToSend.setAmount(itemToSend.getAmount() - 1);
            
            // If the stack is now empty, remove it from inventory
            if (itemToSend.getAmount() <= 0) {
                // Find the slot and clear it
                for (int i = 0; i < getInventory().getSize(); i++) {
                    if (getInventory().getItem(i) == itemToSend) {
                        getInventory().setItem(i, null);
                        break;
                    }
                }
            }
        }
        // If wasAdded is false, the item stays in the sender's inventory
    }

    /**
     * Gets the first available item from the inventory
     * @return The first non-empty ItemStack, or null if inventory is empty
     */
    protected ItemStack getFirstAvailableItem() {
        ItemStack[] contents = getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean handleHopperInput(ItemStack item) {
        return true; // Senders accept hopper input
    }

    @Override
    protected boolean shouldDropQueuedItemsOnBreak() {
        // Warp senders typically don't hold queued items - those belong to receivers
        // But if this is the only block left, we should drop them
        return super.shouldDropQueuedItemsOnBreak();
    }
}