package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.blocks.base.EnderBaseBlock;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;

import java.util.Collections;

public class WarpReceiverBlock extends EnderBaseBlock {

    public WarpReceiverBlock(Location location) {
        super(location, "warp-receiver");
    }

    @Override
    public void onRedstoneActivation() {
        if (!isPaired()) {
            return; // Can't receive without being in a queue
        }

        // Get item from queue
        ItemStack queuedItem = EnderQueueManager.getInstance().getFromQueue(queueId);
        if (queuedItem == null) {
            return; // No items in queue
        }

        // Try to output the item
        if (canOutput(getOutputBlock())) {
            outputItems(Collections.singletonList(queuedItem));
        } else {
            // Can't output, store in inventory if possible
            if (getInventory().firstEmpty() != -1) {
                getInventory().addItem(queuedItem);
            } else {
                // Inventory full, put item back in queue
                EnderQueueManager.getInstance().addToQueue(queueId, queuedItem);
            }
        }
    }

    @Override
    public boolean handleHopperInput(ItemStack item) {
        return false; // Receivers don't accept hopper input
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack item) {
        // Receivers can only have items placed manually or from queue
        return false; // Prevent hopper insertion
    }

    @Override
    protected boolean shouldDropQueuedItemsOnBreak() {
        // Warp receivers should drop queued items since they're destinations
        return queueId != null;
    }
}