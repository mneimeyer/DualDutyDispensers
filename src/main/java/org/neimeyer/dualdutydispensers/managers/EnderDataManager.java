package org.neimeyer.dualdutydispensers.managers;

import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages persistent data for Ender/Warp blocks
 */
public class EnderDataManager {

    private static EnderDataManager instance;
    private final DualDutyDispensers plugin;
    private final File dataDir;

    private EnderDataManager(DualDutyDispensers plugin) {
        this.plugin = plugin;
        this.dataDir = new File(plugin.getDataFolder(), "ender-data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (!created) {
                plugin.getLogger().warning("Failed to create ender-data directory: " + dataDir.getPath());
            }
        }
    }

    public static EnderDataManager getInstance() {
        if (instance == null) {
            instance = new EnderDataManager(DualDutyDispensers.getInstance());
        }
        return instance;
    }

    /**
     * Save queue metadata
     */
    public void saveQueueMetadata(Map<String, QueueMetadata> metadataMap) {
        File metadataFile = new File(dataDir, "queue-metadata.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
            for (QueueMetadata metadata : metadataMap.values()) {
                writer.write("QUEUE:" + metadata.getQueueId());
                writer.newLine();

                // Save all blocks as generic entries since we can't access left/right separately
                for (String blockLocation : metadata.getAllBlocks()) {
                    writer.write("BLOCK:" + blockLocation);
                    writer.newLine();
                }

                writer.write("END");
                writer.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save queue metadata", e);
        }
    }

    /**
     * Load queue metadata - UPDATED to work with simplified format
     */
    public Map<String, QueueMetadata> loadQueueMetadata() {
        Map<String, QueueMetadata> metadataMap = new ConcurrentHashMap<>();
        File metadataFile = new File(dataDir, "queue-metadata.txt");

        if (!metadataFile.exists()) {
            return metadataMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;
            QueueMetadata currentQueue = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.startsWith("QUEUE:")) {
                    String queueId = line.substring(6);
                    currentQueue = new QueueMetadata(queueId);

                } else if (line.startsWith("LEFT:") && currentQueue != null) {
                    // Legacy format support - treat LEFT as sender
                    String blockLocation = line.substring(5);
                    currentQueue.addLeftSideBlock(blockLocation);

                } else if (line.startsWith("RIGHT:") && currentQueue != null) {
                    // Legacy format support - treat RIGHT as receiver  
                    String blockLocation = line.substring(6);
                    currentQueue.addRightSideBlock(blockLocation);

                } else if (line.startsWith("BLOCK:") && currentQueue != null) {
                    // New simplified format - we'll need to determine left/right from block type
                    String blockLocation = line.substring(6);
                    // For now, add as left side - this will need refinement when we know block types
                    currentQueue.addLeftSideBlock(blockLocation);

                } else if (line.equals("END") && currentQueue != null) {
                    metadataMap.put(currentQueue.getQueueId(), currentQueue);
                    currentQueue = null;
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load queue metadata", e);
        }

        return metadataMap;
    }

    /**
     * Save queue data using new Queue interface
     */
    public void saveQueue(String queueId, Queue<ItemStack> queue) {
        List<ItemStack> items = new ArrayList<>(queue);
        saveQueueItems(queueId, items);
    }

    /**
     * Load queue data and return as Queue
     */
    public Queue<ItemStack> loadQueue(String queueId) {
        List<ItemStack> items = loadQueueItems(queueId);
        return new LinkedList<>(items);
    }

    /**
     * Save queue items to file
     */
    private void saveQueueItems(String queueId, List<ItemStack> items) {
        File queueFile = getQueueFile(queueId);

        if (items.isEmpty()) {
            if (queueFile.exists()) {
                boolean deleted = queueFile.delete();
                if (!deleted) {
                    plugin.getLogger().warning("Failed to delete empty queue file: " + queueFile.getPath());
                }
            }
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(queueFile))) {
            // Use Bukkit's serialization instead of Java's
            List<Map<String, Object>> serializableItems = new ArrayList<>();
            for (ItemStack item : items) {
                if (item != null) {
                    serializableItems.add(item.serialize());
                }
            }
            oos.writeObject(serializableItems);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save queue for " + queueId, e);
        }
    }

    /**
     * Load queue items from file
     */
    @SuppressWarnings("unchecked")
    private List<ItemStack> loadQueueItems(String queueId) {
        File queueFile = getQueueFile(queueId);

        if (!queueFile.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(queueFile))) {
            List<Map<String, Object>> serializedItems = (List<Map<String, Object>>) ois.readObject();
            List<ItemStack> items = new ArrayList<>();
            for (Map<String, Object> serializedItem : serializedItems) {
                ItemStack item = ItemStack.deserialize(serializedItem);
                items.add(item);
            }
            return items;
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load queue for " + queueId, e);
            return new ArrayList<>();
        }
    }


    /**
     * Generate queue file name for a queue ID
     */
    private File getQueueFile(String queueId) {
        // Clean up queue ID for safe file naming
        String safeQueueId = queueId.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(dataDir, "queue_" + safeQueueId + ".dat");
    }

    // Legacy methods for backward compatibility during transition
    @Deprecated
    public void savePairings(String blockLocation) {
        // This method is deprecated and should not be used in the new system
        plugin.getLogger().warning("Deprecated savePairings method called for " + blockLocation);
    }

    @Deprecated
    public Set<String> loadPairings(String blockLocation) {
        // This method is deprecated and should not be used in the new system
        plugin.getLogger().warning("Deprecated loadPairings method called for " + blockLocation);
        return new HashSet<>();
    }
}