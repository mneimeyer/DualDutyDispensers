package org.neimeyer.dualdutydispensers.managers;

import java.util.HashSet;
import java.util.Set;

/**
 * Metadata about a queue and its participants
 */
public class QueueMetadata {
    private final String queueId;
    private final Set<String> leftSideBlocks;   // Block location strings  
    private final Set<String> rightSideBlocks;  // Block location strings
    
    public QueueMetadata(String queueId) {
        this.queueId = queueId;
        this.leftSideBlocks = new HashSet<>();
        this.rightSideBlocks = new HashSet<>();
    }
    
    public String getQueueId() {
        return queueId;
    }
    
    public void addLeftSideBlock(String blockLocation) {
        leftSideBlocks.add(blockLocation);
    }
    
    public void addRightSideBlock(String blockLocation) {
        rightSideBlocks.add(blockLocation);
    }
    
    /**
     * Remove a block from this queue's metadata
     * @param blockLocation The block location to remove
     * @return true if the block was found and removed
     */
    public boolean removeBlock(String blockLocation) {
        boolean removed = false;
        if (leftSideBlocks.remove(blockLocation)) {
            removed = true;
        }
        if (rightSideBlocks.remove(blockLocation)) {
            removed = true;
        }
        return removed;
    }

    /**
     * Get all blocks in this queue (both sides)
     */
    public Set<String> getAllBlocks() {
        Set<String> allBlocks = new HashSet<>();
        allBlocks.addAll(leftSideBlocks);
        allBlocks.addAll(rightSideBlocks);
        return allBlocks;
    }
}