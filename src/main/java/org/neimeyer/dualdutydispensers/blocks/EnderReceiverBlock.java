package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.EnderBaseBlock;
import org.neimeyer.dualdutydispensers.config.BlockConfig;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EnderReceiverBlock extends EnderBaseBlock {
    // Create a placeholder item that will never be dispensed
    private final ItemStack PLACEHOLDER_ITEM;
    private final int PLACEHOLDER_SLOT = 8; // Last slot in dispenser inventory

    public EnderReceiverBlock(Location location) {
        super(location, "ender-receiver");
        
        // Get placeholder item settings from config
        BlockConfig config = DualDutyDispensers.getInstance().getConfigManager().getBlockConfig(blockKey);
        String name = "Heart of the Void";
        String lore = "This item creates a small tear in reality and cannot be removed";
        
        // Try to get values from config if available
        if (config != null) {
            // Use the public methods from BlockConfig to get config values
            try {
                // Use reflection to get access to the protected config field
                java.lang.reflect.Field configField = BlockConfig.class.getDeclaredField("config");
                configField.setAccessible(true);
                FileConfiguration yamlConfig = (FileConfiguration) configField.get(config);
                
                if (yamlConfig != null) {
                    name = yamlConfig.getString("placeholder.name", name);
                    lore = yamlConfig.getString("placeholder.lore", lore);
                }
            } catch (Exception e) {
                // If reflection fails, just use default values
            }
        }
        
        // Create a placeholder item that will never be dispensed naturally
        PLACEHOLDER_ITEM = new ItemStack(Material.STRUCTURE_VOID, 1);
        ItemMeta meta = PLACEHOLDER_ITEM.getItemMeta();
        if (meta != null) {
            // Use the deprecated methods (acceptable with warning)
            meta.setDisplayName("§5" + name);
            
            // Split lore by newlines and convert to list in a single operation
            List<String> loreList = new ArrayList<>(Arrays.asList(lore.split("\\n")));
            
            meta.setLore(loreList);
            PLACEHOLDER_ITEM.setItemMeta(meta);
        }
        
        // Add the placeholder item to the dispenser
        addPlaceholderItem();
    }

    /**
     * Add a placeholder item to the dispenser to ensure redstone activation works
     */
    private void addPlaceholderItem() {
        Inventory inv = getInventory();
        if (inv != null) {
            inv.setItem(PLACEHOLDER_SLOT, PLACEHOLDER_ITEM);
        }
    }

    @Override
    public boolean allowsNaturalDispensing() {
        // Always false - we handle dispensing manually
        return false;
    }

    @Override
    public void onTick() {
        super.onTick();
        
        // Ensure placeholder item is always present
        Inventory inv = getInventory();
        if (inv != null) {
            ItemStack currentItem = inv.getItem(PLACEHOLDER_SLOT);
            if (currentItem == null || !PLACEHOLDER_ITEM.isSimilar(currentItem)) {
                inv.setItem(PLACEHOLDER_SLOT, PLACEHOLDER_ITEM);
            }
        }
    }

    @Override
    public void onRedstoneActivation() {
        if (!checkPairingAndLog()) {
            return;
        }

        // Get item from queue
        EnderQueueManager qm = EnderQueueManager.getInstance();
        ItemStack itemToReceive = qm.getFromQueue(queueId);
        
        if (itemToReceive == null) {
            // No items in queue, nothing to do
            return;
        }

        // Validate the block is still a dispenser
        Block block = location.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }
        
        try {
            // Manually output the item without adding it to inventory
            outputItems(Collections.singletonList(itemToReceive));
        } catch (Exception e) {
            // Put item back in queue if we couldn't dispense it
            qm.addToQueue(queueId, itemToReceive);
        }
    }

    @Override
    public boolean handleHopperInput(ItemStack item) {
        // Receivers don't accept hopper input
        return false;
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack item) {
        // Block placeholder slot from being modified externally
        return slot != PLACEHOLDER_SLOT;
    }
    
    @Override
    public List<ItemStack> onBlockBreak() {
        // Get normal drops but filter out our placeholder item
        List<ItemStack> drops = super.onBlockBreak();
        drops.removeIf(item -> item != null && PLACEHOLDER_ITEM.isSimilar(item));
        return drops;
    }
}