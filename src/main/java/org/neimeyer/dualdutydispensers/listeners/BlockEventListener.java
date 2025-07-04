package org.neimeyer.dualdutydispensers.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.CustomBlock;
import org.neimeyer.dualdutydispensers.blocks.base.CustomDispenserBlock;
import org.neimeyer.dualdutydispensers.config.BlockConfig;

import java.util.List;

public class BlockEventListener implements Listener {
    private final DualDutyDispensers plugin;

    public BlockEventListener(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String blockKey = getBlockKeyFromItem(item);

        if (blockKey != null) {
            // Create the custom block
            plugin.getBlockManager().createBlock(event.getBlock().getLocation(), blockKey);
        }
    }
    
    @SuppressWarnings("deprecation") // Custom model data methods are still widely used in plugins
    private String getBlockKeyFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }

        int customModelData = item.getItemMeta().getCustomModelData();

        // Check against all configured block types using the ConfigManager
        for (String blockKey : plugin.getConfigManager().getBlockKeys()) {
            if (plugin.getConfigManager().isBlockEnabled(blockKey)) {
                BlockConfig config = plugin.getConfigManager().getBlockConfig(blockKey);
                if (config != null && config.getCustomModelData() == customModelData) {
                    return blockKey;
                }
            }
        }

        return null;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CustomBlock customBlock = plugin.getBlockManager().getBlock(block.getLocation());

        if (customBlock != null) {
            event.setDropItems(false);

            // Get drops from the custom block
            List<ItemStack> drops = customBlock.onBlockBreak();

            // Drop items naturally
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }

            // Remove from manager
            plugin.getBlockManager().removeBlock(block.getLocation());
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        // Check if this is a custom block
        CustomBlock customBlock = plugin.getBlockManager().getBlock(event.getBlock().getLocation());

        if (customBlock != null) {
            // Only trigger on rising edge (when redstone power increases)
            if (event.getNewCurrent() > event.getOldCurrent() && event.getNewCurrent() > 0) {
                try {
                    customBlock.onRedstoneActivation();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error during redstone activation for block " +
                            customBlock.getBlockKey() + " at " + customBlock.getLocation() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        // Check if this is a custom dispenser block
        CustomBlock customBlock = plugin.getBlockManager().getBlock(event.getBlock().getLocation());

        if (customBlock instanceof CustomDispenserBlock dispenserBlock) {
            // Check if this block allows natural dispensing
            if (!dispenserBlock.allowsNaturalDispensing()) {
                event.setCancelled(true);

                // Trigger our custom behavior and cancel the natural drop
                try {
                    customBlock.onRedstoneActivation();
                } catch (Exception e) {
                    plugin.getLogger().warning("Error during redstone activation for block " +
                            customBlock.getBlockKey() + " at " + customBlock.getLocation() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // If allowsNaturalDispensing() returns true, let the event proceed normally
        }
    }
}