package org.neimeyer.dualdutydispensers.blocks.base;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.util.List;

public abstract class CustomObserverBlock extends CustomBlock {
    
    public CustomObserverBlock(Location location, String blockKey) {
        super(location, blockKey);
    }
    
    protected void sendRedstoneSignal() {
        Block block = location.getBlock();
        
        // Force the observer to think it detected a change
        if (block.getBlockData() instanceof org.bukkit.block.data.type.Observer observer) {
            // Toggle the powered state to simulate detection
            observer.setPowered(!observer.isPowered());
            block.setBlockData(observer, true);
            
            // Schedule to reset after 2 ticks (like normal observer behavior)
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                DualDutyDispensers.getInstance(), 
                () -> {
                    if (block.getType() == org.bukkit.Material.OBSERVER) {
                        var blockData = block.getBlockData();
                        if (blockData instanceof org.bukkit.block.data.type.Observer obs) {
                            obs.setPowered(false);
                            block.setBlockData(obs, true);
                        }
                    }
                }, 
                2L
            );
        }
    }
    
    @Override
    public List<ItemStack> onBlockBreak() {
        // Observer-based blocks just drop themselves - get from config
        ItemStack customItem = DualDutyDispensers.getInstance().getConfigManager()
            .getBlockConfig(blockKey).createItemStack(getBaseMaterial());
        return List.of(customItem);
    }
    
    private org.bukkit.Material getBaseMaterial() {
        // Observer-based blocks use OBSERVER
        return org.bukkit.Material.OBSERVER;
    }
    
    @Override
    public void onRedstoneActivation() {
        // Observer blocks don't respond to redstone, they generate it
    }
}