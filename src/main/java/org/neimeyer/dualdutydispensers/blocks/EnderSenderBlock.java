package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.neimeyer.dualdutydispensers.blocks.base.EnderBaseBlock;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;
import org.bukkit.inventory.Inventory;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.bukkit.block.Block;

public class EnderSenderBlock extends EnderBaseBlock {

    public EnderSenderBlock(Location location) {
        super(location, "ender-sender");
    }

    @Override
    public boolean allowsNaturalDispensing() {
        // Override base implementation to prevent vanilla dispensing
        return false;
    }

    @Override
    public void onRedstoneActivation() {
        //TODO: Figure out why the last item remains in the Sender when inventory gets down to 1 item
        Block block = location.getBlock();

        // Obtain the inventory holder directly from the block state
        org.bukkit.inventory.Inventory inventory = null;
        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            inventory = ((org.bukkit.inventory.InventoryHolder) block.getState()).getInventory();
        }
        if (inventory == null) {
            return;
        }

        // Log entire inventory contents, slot by slot
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < inventory.getSize(); i++) {
            org.bukkit.inventory.ItemStack item = inventory.getItem(i);
            contents.append(item == null ? "-" : item.getType() + "x" + item.getAmount());
            if (i < inventory.getSize() - 1) contents.append(", ");
        }

        // Now proceed with the usual send logic
        if (!checkPairingAndLog()) {
            return;
        }

        // Find first non-air stack in slots 0–7
        int slotToSendFrom = -1;
        org.bukkit.inventory.ItemStack itemToSend = null;
        for (int i = 0; i < Math.min(8, inventory.getSize()); i++) {
            org.bukkit.inventory.ItemStack stack = inventory.getItem(i);
            if (stack != null && !stack.getType().isAir() && stack.getAmount() > 0) {
                slotToSendFrom = i;
                itemToSend = stack;
                break;
            }
        }
        if (slotToSendFrom < 0) {
            return;
        }

        // Remove one from the stack and queue it
        org.bukkit.inventory.ItemStack single = itemToSend.clone();
        single.setAmount(1);
        boolean added = EnderQueueManager.getInstance().addToQueue(queueId, single);
        if (!added) {
            return;
        }

        itemToSend.setAmount(itemToSend.getAmount() - 1);
        if (itemToSend.getAmount() <= 0) {
            inventory.setItem(slotToSendFrom, null);
        } else {
            inventory.setItem(slotToSendFrom, itemToSend);
        }
    }

    @Override
    public boolean handleHopperInput(ItemStack item) {
        return true; // Senders accept hopper input
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack item) {
        // Allow any item in any slot for senders
        return true;
    }

    @Override
    protected ItemStack getFirstAvailableItem() {
        // Override to skip last slot
        ItemStack[] contents = getInventory().getContents();
        for (int i = 0; i < contents.length - 1; i++) {
            ItemStack item = contents[i];
            if (item != null && !item.getType().isAir()) {
                return item;
            }
        }
        return null;
    }
}