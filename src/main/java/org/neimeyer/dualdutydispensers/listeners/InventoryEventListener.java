package org.neimeyer.dualdutydispensers.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.CustomBlock;
import org.neimeyer.dualdutydispensers.blocks.base.CustomDispenserBlock;

public class InventoryEventListener implements Listener {
    private final DualDutyDispensers plugin;
    
    public InventoryEventListener(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    @SuppressWarnings("unused") // Called by Bukkit event system
    public void onInventoryClick(InventoryClickEvent event) {
        // Handle clicks in custom dispenser inventories
        if (event.getView().getTopInventory().getType() == InventoryType.DISPENSER) {
            Inventory topInventory = event.getView().getTopInventory();
            if (topInventory.getHolder() instanceof Dispenser dispenser) {
                Block block = dispenser.getBlock();
                
                // Safer access to block manager
                if (plugin == null || plugin.getBlockManager() == null) {
                    return;
                }
                
                CustomBlock customBlock = plugin.getBlockManager().getBlock(block.getLocation());
                
                if (customBlock instanceof CustomDispenserBlock) {
                    handleCustomDispenserClick(event, (CustomDispenserBlock) customBlock);
                }
            }
        }
    }
    
    private void handleCustomDispenserClick(InventoryClickEvent event, CustomDispenserBlock customBlock) {
        Player player = (Player) event.getWhoClicked();
        
        // Handle shift-clicks from player inventory to custom dispenser
        if (event.isShiftClick() && event.getClickedInventory() != event.getView().getTopInventory()) {
            ItemStack clickedItem = event.getCurrentItem();
            
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                // Find a valid slot for this item
                int targetSlot = findValidSlotForItem(customBlock, clickedItem);
                
                if (targetSlot == -1) {
                    event.setCancelled(true);
                    player.sendMessage("§cThis item cannot be used in this block!");
                    return;
                }
                
                // Check if we can add to the target slot
                Inventory customInv = event.getView().getTopInventory();
                ItemStack existing = customInv.getItem(targetSlot);
                
                if (existing != null && !existing.getType().isAir() && !existing.isSimilar(clickedItem)) {
                    event.setCancelled(true);
                    player.sendMessage("§cThat slot already contains a different item!");
                    return;
                }
                
                if (existing != null && existing.getAmount() >= existing.getMaxStackSize()) {
                    event.setCancelled(true);
                    player.sendMessage("§cThat slot is full!");
                    return;
                }
                
                // Cancel default behavior and handle manually
                event.setCancelled(true);
                
                // Calculate how much we can move
                int amountToMove = clickedItem.getAmount();
                if (existing != null && !existing.getType().isAir()) {
                    amountToMove = Math.min(amountToMove, existing.getMaxStackSize() - existing.getAmount());
                }
                
                if (amountToMove > 0) {
                    // Move the items
                    if (existing == null || existing.getType().isAir()) {
                        ItemStack newStack = clickedItem.clone();
                        newStack.setAmount(amountToMove);
                        customInv.setItem(targetSlot, newStack);
                    } else {
                        existing.setAmount(existing.getAmount() + amountToMove);
                    }
                    
                    // Remove from source
                    clickedItem.setAmount(clickedItem.getAmount() - amountToMove);
                    if (clickedItem.getAmount() <= 0) {
                        event.setCurrentItem(null);
                    }
                }
            }
            return;
        }
        
        // Handle direct clicks in the custom dispenser
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            int slot = event.getSlot();
            
            // Check if this slot is allowed for this block type
            if (!isSlotAllowed(customBlock, slot)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot use this slot in this block!");
                return;
            }
            
            // If placing an item, validate it's the right type for the slot
            ItemStack cursorItem = event.getCursor();
            if (!cursorItem.getType().isAir()) {
                if (!customBlock.isValidItemForSlot(slot, cursorItem)) {
                    event.setCancelled(true);
                    player.sendMessage("§cThis item cannot be placed in this slot!");
                }
            }
        }
    }
    
    private int findValidSlotForItem(CustomDispenserBlock customBlock, ItemStack item) {
        // Check all slots to find where this item can go
        for (int slot = 0; slot < 9; slot++) {
            if (isSlotAllowed(customBlock, slot) && customBlock.isValidItemForSlot(slot, item)) {
                return slot;
            }
        }
        return -1; // No valid slot found
    }
    
    private boolean isSlotAllowed(CustomDispenserBlock customBlock, int slot) {
        // Different block types have different allowed slots
        String blockKey = customBlock.getBlockKey();
        
        return switch (blockKey) {
            case "tree-farm" ->
                // Tree Farm: only slots 1, 3, 5, 7 allowed (0, 2, 4, 6, 8 blocked)
                slot == 1 || slot == 3 || slot == 5 || slot == 7;
            
            case "ender-receiver" ->
                // Ender Receiver: fully locked (no manual placement)
                false;
            
            case "compressor", "decompressor", "siphon", "block-breaker", "murder-block", "ender-sender" ->
                // These blocks allow any slot but validate item types
                true;
            
            default -> true;
        };
    }
    
    @EventHandler
    @SuppressWarnings("unused") // Called by Bukkit event system
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Handle hopper input/output for custom dispensers
        if (event.getDestination().getType() == InventoryType.DISPENSER) {
            if (event.getDestination().getHolder() instanceof Dispenser dispenser) {
                Block block = dispenser.getBlock();
                
                // Safer access to block manager
                if (plugin == null || plugin.getBlockManager() == null) {
                    return;
                }
                
                CustomBlock customBlock = plugin.getBlockManager().getBlock(block.getLocation());
                
                if (customBlock instanceof CustomDispenserBlock customDispenser) {
                    // Use the handleHopperInput method for ALL custom dispensers
                    ItemStack item = event.getItem();
                    if (!customDispenser.handleHopperInput(item)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        
        // Handle hopper extraction (allow normal behavior for most blocks)
        if (event.getSource().getType() == InventoryType.DISPENSER) {
            if (event.getSource().getHolder() instanceof Dispenser dispenser) {
                Block block = dispenser.getBlock();
                
                // Safer access to block manager
                if (plugin == null || plugin.getBlockManager() == null) {
                    return;
                }
                
                CustomBlock customBlock = plugin.getBlockManager().getBlock(block.getLocation());
                
                if (customBlock != null) {
                    // Special handling for blocks that don't allow extraction
                    String blockKey = customBlock.getBlockKey();
                    
                    if ("ender-receiver".equals(blockKey)) {
                        // Ender Receiver doesn't allow hopper extraction
                        event.setCancelled(true);
                    }
                    // Most blocks allow normal extraction (default behavior)
                }
            }
        }
    }
}