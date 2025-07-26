package org.neimeyer.dualdutydispensers.managers;

import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages queues for Ender/Warp blocks using UUID-based queue system
 */
public class EnderQueueManager {
    
    private static EnderQueueManager instance;
    private final Map<String, Queue<ItemStack>> queues = new ConcurrentHashMap<>();
    private final Map<String, QueueMetadata> queueMetadata = new ConcurrentHashMap<>();
    
    private EnderQueueManager() {}
    
    public static EnderQueueManager getInstance() {
        if (instance == null) {
            instance = new EnderQueueManager();
        }
        return instance;
    }
    
    /**
     * Initialize manager by loading saved queues and metadata
     */
    public void initialize() {
        EnderDataManager dataManager = EnderDataManager.getInstance();
        
        // Load queue metadata
        Map<String, QueueMetadata> loadedMetadata = dataManager.loadQueueMetadata();
        queueMetadata.putAll(loadedMetadata);
        
        // Load actual queue contents
        for (String queueId : queueMetadata.keySet()) {
            Queue<ItemStack> queue = dataManager.loadQueue(queueId);
            if (queue != null) {
                queues.put(queueId, queue);
            }
        }
    }
    
    /**
     * Save all queues and metadata to persistent storage
     */
    public void saveAllQueues() {
        EnderDataManager dataManager = EnderDataManager.getInstance();

        // Save metadata
        dataManager.saveQueueMetadata(queueMetadata);

        // Save queue contents
        for (Map.Entry<String, Queue<ItemStack>> entry : queues.entrySet()) {
            dataManager.saveQueue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Remove a block from all queue metadata (called when block is broken)
     */
    public void removeBlockFromAllQueues(String blockLocation) {
        boolean metadataChanged = false;

        // Remove from all queue metadata
        for (QueueMetadata metadata : queueMetadata.values()) {
            if (metadata.removeBlock(blockLocation)) {
                metadataChanged = true;
            }
        }

        // Clean up empty metadata entries but keep the queue data
        List<String> emptyQueues = new ArrayList<>();
        for (Map.Entry<String, QueueMetadata> entry : queueMetadata.entrySet()) {
            if (entry.getValue().getAllBlocks().isEmpty()) {
                emptyQueues.add(entry.getKey());
            }
        }

        // Remove empty metadata but preserve the actual queue
        for (String queueId : emptyQueues) {
            queueMetadata.remove(queueId);
            metadataChanged = true;
        }

        if (metadataChanged) {
            saveQueueMetadata();
        }
    }

    /**
     * Adds an item to a queue if there's room
     *
     * @param queueId The queue to add to
     * @param item    The item to add (should be exactly 1 item from sender)
     * @return true if the item was added, false if queue is full
     */
    public boolean addToQueue(String queueId, ItemStack item) {
        if (queueId == null || item == null || item.getType().isAir()) {
            return false;
        }

        Queue<ItemStack> queue = queues.computeIfAbsent(queueId, k -> new LinkedList<>());

        // Queue should have at most one stack
        if (queue.isEmpty()) {
            // Empty queue - add the item as the first and only stack
            queue.offer(item.clone());
            saveQueue(queueId);
            return true;
        }

        // Queue has one stack - try to add to it
        ItemStack existing = queue.peek(); // Get the single stack without removing it
        if (existing.isSimilar(item) && existing.getAmount() < existing.getMaxStackSize()) {
            existing.setAmount(existing.getAmount() + item.getAmount());
            saveQueue(queueId);
            return true;
        }

        // Queue is full (either wrong item type or stack is at max size)
        return false;
    }

    /**
     * Gets and removes an item from a queue
     */
    public ItemStack getFromQueue(String queueId) {
        Queue<ItemStack> queue = queues.get(queueId);
        if (queue == null) {
            return null;
        }

        if (queue.isEmpty()) {
            return null;
        }

        ItemStack existing = queue.peek(); // Don't remove yet
        if (existing == null || existing.getAmount() <= 0) {
            queue.poll(); // Remove empty stack
            saveQueue(queueId);
            return null;
        }

        // Create the single item to return
        ItemStack singleItem = existing.clone();
        singleItem.setAmount(1);

        // Reduce the existing stack
        existing.setAmount(existing.getAmount() - 1);

        // Remove stack if empty
        if (existing.getAmount() <= 0) {
            queue.poll();
        }

        saveQueue(queueId);
        return singleItem;
    }

    /**
     * Get and clear all items from a queue (used when dropping items on break)
     */
    public List<ItemStack> getAndClearQueue(String queueId) {
        Queue<ItemStack> queue = queues.get(queueId);
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }

        List<ItemStack> allItems = new ArrayList<>();

        // Convert all queue items to a list and clear the queue
        while (!queue.isEmpty()) {
            ItemStack item = queue.poll();
            if (item != null && item.getAmount() > 0) {
                allItems.add(item);
            }
        }

        // Save the now-empty queue
        saveQueue(queueId);

        return allItems;
    }

    /**
     * Save a specific queue to persistent storage
     */
    private void saveQueue(String queueId) {
        Queue<ItemStack> queue = queues.get(queueId);
        if (queue != null) {
            EnderDataManager.getInstance().saveQueue(queueId, queue);
        }
    }

    /**
     * Convert location to string (replacing LocationUtils functionality)
     */
    public static String locationToString(org.bukkit.Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return String.format("%s;%d;%d;%d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private void saveQueueMetadata() {
        EnderDataManager.getInstance().saveQueueMetadata(queueMetadata);
    }

    /**
     * Add a sender block to a queue
     */
    public void addSenderToQueue(String queueId, String blockLocation) {
        QueueMetadata metadata = queueMetadata.computeIfAbsent(queueId, QueueMetadata::new);
        metadata.addLeftSideBlock(blockLocation);  // Senders are "left side" in the metadata
        saveQueueMetadata();
    }

    /**
     * Add a receiver block to a queue
     */
    public void addReceiverToQueue(String queueId, String blockLocation) {
        QueueMetadata metadata = queueMetadata.computeIfAbsent(queueId, QueueMetadata::new);
        metadata.addRightSideBlock(blockLocation); // Receivers are "right side" in the metadata
        saveQueueMetadata();
    }
}