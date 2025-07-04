package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.CustomObserverBlock;

import java.util.List;

public class RedstoneClockBlock extends CustomObserverBlock {
    
    private BukkitTask clockTask;
    private int tickInterval = -1; // -1 means not initialized yet
    
    public RedstoneClockBlock(Location location) {
        super(location, "redstone-clock");
        // Start the clock immediately when the block is created
        startClock();
    }
    
    private int getTickInterval() {
        if (tickInterval == -1) {
            // Initialize on first access
            tickInterval = DualDutyDispensers.getInstance().getConfigManager()
                .getRedstoneClockConfig().getTickInterval();
        }
        return tickInterval;
    }
    
    private void startClock() {
        if (clockTask != null) {
            clockTask.cancel();
        }
        
        clockTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Send redstone pulse
                sendRedstoneSignal();
            }
        }.runTaskTimer(DualDutyDispensers.getInstance(), 0L, getTickInterval());
    }
    
    @Override
    public void onTick() {
        // No need to do anything on regular ticks - we have our own timer
    }
    
    @Override
    public void loadData() {
        // Restart the clock after loading (in case it wasn't running)
        startClock();
    }
    
    @Override
    public void saveData() {
        // Clock doesn't need additional persistence
        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }
    }
    
    @Override
    public List<ItemStack> onBlockBreak() {
        // Make sure to cancel the task when the block is broken
        if (clockTask != null) {
            clockTask.cancel();
            clockTask = null;
        }
        
        // Call the parent implementation to get the drops
        return super.onBlockBreak();
    }
}