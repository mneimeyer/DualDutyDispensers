package org.neimeyer.dualdutydispensers.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.CustomBlock;
import org.neimeyer.dualdutydispensers.blocks.base.CustomDispenserBlock;
import org.neimeyer.dualdutydispensers.blocks.base.EnderBaseBlock;
import org.neimeyer.dualdutydispensers.config.BlockConfig;
import org.neimeyer.dualdutydispensers.managers.EnderQueueManager;

import java.util.List;
import java.util.logging.Level;

public class BlockEventListener implements Listener {
    private final DualDutyDispensers plugin;

    public BlockEventListener(DualDutyDispensers plugin) {
        this.plugin = plugin;
    }

@EventHandler
@SuppressWarnings("unused")
public void onBlockPlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    String blockKey = getBlockKeyFromItem(item);

    if (blockKey != null) {
        // Create the custom block
        CustomBlock newBlock = plugin.getBlockManager().createBlock(event.getBlock().getLocation(), blockKey);
        
        // Check if this is an ender block with pairing info
        if (newBlock instanceof EnderBaseBlock enderBlock && item.hasItemMeta()) {
            var meta = item.getItemMeta();
            var dataContainer = meta.getPersistentDataContainer();
            var queueIdKey = new org.bukkit.NamespacedKey(plugin, "queue_id");
            
            if (dataContainer.has(queueIdKey, org.bukkit.persistence.PersistentDataType.STRING)) {
                String queueId = dataContainer.get(queueIdKey, org.bukkit.persistence.PersistentDataType.STRING);
                
                if (queueId != null && !queueId.isEmpty()) {
                    enderBlock.setQueueId(queueId);
                    enderBlock.saveData();
                    
                    // Add to queue metadata
                    EnderQueueManager queueManager = EnderQueueManager.getInstance();
                    String blockLocation = EnderQueueManager.locationToString(enderBlock.getLocation());
                    
                    if (blockKey.equals("ender-sender")) {
                        queueManager.addSenderToQueue(queueId, blockLocation);
                    } else {
                        queueManager.addReceiverToQueue(queueId, blockLocation);
                    }
                    
                    event.getPlayer().sendMessage("§aBlock pairing completed!");
                }
            }
        }
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
    @SuppressWarnings("unused") // Called by Bukkit event system
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
@SuppressWarnings("unused") // Called by Bukkit event system
public void onRedstone(BlockRedstoneEvent event) {
    // Check if this is a custom block
    CustomBlock customBlock = plugin.getBlockManager().getBlock(event.getBlock().getLocation());

    if (customBlock != null) {
        // Only trigger on rising edge (when redstone power increases)
        if (event.getNewCurrent() > event.getOldCurrent() && event.getNewCurrent() > 0) {
            try {
                customBlock.onRedstoneActivation();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, String.format(
                    "Error during redstone activation for block %s at %s: %s",
                    customBlock.getBlockKey(), 
                    customBlock.getLocation(), 
                    e.getMessage()
                ), e);
            }
        }
    }
}

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused") // Called by Bukkit event system
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
                    plugin.getLogger().log(Level.SEVERE, String.format(
                        "Error during redstone activation for block %s at %s: %s",
                        customBlock.getBlockKey(), 
                        customBlock.getLocation(), 
                        e.getMessage()
                    ), e);
                }
            }
            // If allowsNaturalDispensing() returns true, let the event proceed normally
        }
    }

@EventHandler
@SuppressWarnings("unused")
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking()) {
        return; // Only pair when shift-right-clicking
    }
    
    Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null) {
        return;
    }
    
    Player player = event.getPlayer();
    ItemStack item = event.getItem();
    
    // Check if clicking on an ender block with an ender block
    CustomBlock targetBlock = plugin.getBlockManager().getBlock(clickedBlock.getLocation());
    if (!(targetBlock instanceof EnderBaseBlock enderTarget)) {
        return;
    }
    
    if (item == null || item.getType().isAir()) {
        // No item - show current pairing status
        if (enderTarget.isPaired()) {
            // Use getter method since we're in different packages
            String queueId = enderTarget.getQueueId();
            player.sendMessage("§7This block is paired. Queue ID: " + queueId.substring(0, 8) + "...");
        } else {
            player.sendMessage("§7This block is not paired.");
        }
        return;
    }
    
    String heldBlockKey = getBlockKeyFromItem(item);
    if (heldBlockKey == null || !isEnderBlock(heldBlockKey)) {
        return; // Not holding an ender block
    }
    
    String targetBlockKey = enderTarget.getBlockKey();
    if (!isEnderBlock(targetBlockKey)) {
        return; // Target isn't an ender block
    }
    
    // Check if holding more than one item - prevent pairing with stacks
    if (item.getAmount() > 1) {
        player.sendMessage("§cYou can only pair with a single block item, not a stack!");
        player.sendMessage("§7Split your stack to single items before pairing.");
        return;
    }
    
    // Both are ender blocks - try to pair
    if (targetBlockKey.equals(heldBlockKey)) {
        player.sendMessage("§cCannot pair two blocks of the same type!");
        return;
    }
    
    if (enderTarget.isPaired()) {
        player.sendMessage("§cTarget block is already paired!");
        return;
    }
    
    event.setCancelled(true);
    
    // Create a simple pairing
    String newQueueId = java.util.UUID.randomUUID().toString();
    enderTarget.setQueueId(newQueueId);
    enderTarget.saveData();
    
    // Add to queue manager
    EnderQueueManager queueManager = EnderQueueManager.getInstance();
    String targetLocation = EnderQueueManager.locationToString(enderTarget.getLocation());
    
    if (targetBlockKey.equals("ender-sender")) {
        queueManager.addSenderToQueue(newQueueId, targetLocation);
    } else {
        queueManager.addReceiverToQueue(newQueueId, targetLocation);
    }
    
    // Store pairing info in item's meta for when it gets placed
    var meta = item.getItemMeta();
    if (meta != null) {
        meta.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(plugin, "queue_id"), 
            org.bukkit.persistence.PersistentDataType.STRING, 
            newQueueId
        );
        item.setItemMeta(meta);
    }
    
    player.sendMessage("§aBlocks paired! Place the " + heldBlockKey + " to complete the connection.");
}
    
    /**
     * Check if a block key represents an ender block (sender or receiver)
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isEnderBlock(String blockKey) {
        return blockKey.equals("ender-sender") || blockKey.equals("ender-receiver");
    }
}