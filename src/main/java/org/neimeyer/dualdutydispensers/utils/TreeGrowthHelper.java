package org.neimeyer.dualdutydispensers.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class TreeGrowthHelper {
    
    // Map sapling types to their corresponding block materials
    private static final Map<Material, TreeBlocks> TREE_BLOCKS = new HashMap<>();
    static {
        // Regular trees
        TREE_BLOCKS.put(Material.OAK_SAPLING, new TreeBlocks(
            Material.OAK_LOG, 
            Material.OAK_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.BIRCH_SAPLING, new TreeBlocks(
            Material.BIRCH_LOG, 
            Material.BIRCH_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.SPRUCE_SAPLING, new TreeBlocks(
            Material.SPRUCE_LOG, 
            Material.SPRUCE_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.JUNGLE_SAPLING, new TreeBlocks(
            Material.JUNGLE_LOG, 
            Material.JUNGLE_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.ACACIA_SAPLING, new TreeBlocks(
            Material.ACACIA_LOG, 
            Material.ACACIA_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.DARK_OAK_SAPLING, new TreeBlocks(
            Material.DARK_OAK_LOG, 
            Material.DARK_OAK_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.MANGROVE_PROPAGULE, new TreeBlocks(
            Material.MANGROVE_LOG, 
            Material.MANGROVE_LEAVES, 
            Material.MANGROVE_ROOTS, 
            null
        ));
        TREE_BLOCKS.put(Material.CHERRY_SAPLING, new TreeBlocks(
            Material.CHERRY_LOG, 
            Material.CHERRY_LEAVES, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.PALE_OAK_SAPLING, new TreeBlocks(
            Material.PALE_OAK_LOG, 
            Material.PALE_OAK_LEAVES, 
            null, 
            null
        ));
        
        TREE_BLOCKS.put(Material.AZALEA, new TreeBlocks(
            Material.OAK_LOG, 
            Material.AZALEA_LEAVES, 
            null,
            Material.FLOWERING_AZALEA_LEAVES  // NOW properly using secondaryLeafType
        ));
        TREE_BLOCKS.put(Material.FLOWERING_AZALEA, new TreeBlocks(
            Material.OAK_LOG, 
            Material.FLOWERING_AZALEA_LEAVES, 
            null,
            Material.AZALEA_LEAVES
        ));
        
        // Nether fungi (require bone meal)
        TREE_BLOCKS.put(Material.CRIMSON_FUNGUS, new TreeBlocks(
            Material.CRIMSON_STEM, 
            Material.NETHER_WART_BLOCK, 
            Material.SHROOMLIGHT, 
            null
        ));
        TREE_BLOCKS.put(Material.WARPED_FUNGUS, new TreeBlocks(
            Material.WARPED_STEM, 
            Material.WARPED_WART_BLOCK, 
            Material.SHROOMLIGHT, 
            null
        ));
        
        // Mushrooms (require bone meal)
        TREE_BLOCKS.put(Material.RED_MUSHROOM, new TreeBlocks(
            null, // No log equivalent
            Material.RED_MUSHROOM_BLOCK, 
            null, 
            null
        ));
        TREE_BLOCKS.put(Material.BROWN_MUSHROOM, new TreeBlocks(
            null, // No log equivalent
            Material.BROWN_MUSHROOM_BLOCK, 
            null, 
            null
        ));
        
        // Chorus plant
        TREE_BLOCKS.put(Material.CHORUS_FLOWER, new TreeBlocks(
            Material.CHORUS_FLOWER,
            null,
            Material.CHORUS_FRUIT,
            null
        ));
    }
    
    // Base quantities for different tree types
    private static final Map<Material, TreeQuantities> BASE_QUANTITIES = new HashMap<>(); 
    static {
        // Regular trees
        BASE_QUANTITIES.put(Material.OAK_SAPLING, new TreeQuantities(4, 12, 0, 0));
        BASE_QUANTITIES.put(Material.BIRCH_SAPLING, new TreeQuantities(4, 10, 0, 0));
        BASE_QUANTITIES.put(Material.SPRUCE_SAPLING, new TreeQuantities(6, 15, 0, 0));
        BASE_QUANTITIES.put(Material.JUNGLE_SAPLING, new TreeQuantities(8, 20, 0, 0));
        BASE_QUANTITIES.put(Material.ACACIA_SAPLING, new TreeQuantities(4, 8, 0, 0));
        BASE_QUANTITIES.put(Material.DARK_OAK_SAPLING, new TreeQuantities(6, 16, 0, 0));
        BASE_QUANTITIES.put(Material.MANGROVE_PROPAGULE, new TreeQuantities(5, 12, 3, 0)); // Includes roots
        BASE_QUANTITIES.put(Material.CHERRY_SAPLING, new TreeQuantities(4, 10, 0, 0));
        BASE_QUANTITIES.put(Material.PALE_OAK_SAPLING, new TreeQuantities(4, 12, 0, 0));
        
        BASE_QUANTITIES.put(Material.AZALEA, new TreeQuantities(3, 6, 0, 2)); // 3 logs, 6 primary leaves, 0 secondary items, 2 secondary leaves
        BASE_QUANTITIES.put(Material.FLOWERING_AZALEA, new TreeQuantities(3, 6, 0, 2)); // 3 logs, 6 primary leaves, 0 secondary items, 2 secondary leaves
        
        // Nether fungi
        BASE_QUANTITIES.put(Material.CRIMSON_FUNGUS, new TreeQuantities(6, 15, 4, 0)); // Stems, wart blocks, shroomlight
        BASE_QUANTITIES.put(Material.WARPED_FUNGUS, new TreeQuantities(6, 15, 4, 0)); // Stems, wart blocks, shroomlight
        
        // Mushrooms
        BASE_QUANTITIES.put(Material.RED_MUSHROOM, new TreeQuantities(0, 20, 0, 0)); // Only mushroom blocks
        BASE_QUANTITIES.put(Material.BROWN_MUSHROOM, new TreeQuantities(0, 20, 0, 0)); // Only mushroom blocks
        
        // Chorus plant
        BASE_QUANTITIES.put(Material.CHORUS_FLOWER, new TreeQuantities(3, 0, 6, 0)); // 3 flowers, no leaves, 6 chorus fruits
    }
    
    public static List<ItemStack> generateTreeDrops(Material saplingType, boolean is2x2Tree, ItemStack tool) {
        TreeBlocks blocks = TREE_BLOCKS.get(saplingType);
        TreeQuantities quantities = BASE_QUANTITIES.get(saplingType);
        
        if (blocks == null || quantities == null) {
            return new ArrayList<>();
        }
        
        List<ItemStack> result = new ArrayList<>();
        
        // Process all four materials equally
        // Material 1: Primary blocks (logs)
        if (blocks.logType() != null && quantities.logs() > 0) {
            int amount = quantities.logs();
            if (is2x2Tree) {
                amount *= 3; // 2x2 trees give more logs
            }
            result.addAll(generateBlockDrops(blocks.logType(), amount, tool));
        }
        
        // Material 2: Primary leaves
        if (blocks.leafType() != null && quantities.leaves() > 0) {
            int amount = quantities.leaves();
            if (is2x2Tree) {
                amount *= 2; // 2x2 trees give more leaves
            }
            result.addAll(generateBlockDrops(blocks.leafType(), amount, tool));
        }
        
        // Material 3: Secondary blocks (roots, shroomlight, etc.)
        if (blocks.secondaryType() != null && quantities.secondary() > 0) {
            int amount = quantities.secondary();
            if (is2x2Tree) {
                amount *= 2; // Scale for 2x2 trees
            }
            result.addAll(generateBlockDrops(blocks.secondaryType(), amount, tool));
        }
        
        // Material 4: Secondary leaves
        if (blocks.secondaryLeafType() != null && quantities.secondaryLeaves() > 0) {
            int amount = quantities.secondaryLeaves();
            if (is2x2Tree) {
                amount *= 2; // Scale for 2x2 trees
            }
            result.addAll(generateBlockDrops(blocks.secondaryLeafType(), amount, tool));
        }
        
        return result;
    }
    
    /**
     * Generate drops for any block type (renamed from generateLeafLoot)
     */
    private static List<ItemStack> generateBlockDrops(Material blockType, int blockCount, ItemStack tool) {
        // Short circuit: When shears or silk_touch return the blocks themselves
        if (tool != null && !tool.getType().isAir()) {
            if (tool.getType() == Material.SHEARS) {
                return Collections.singletonList(new ItemStack(blockType, blockCount));
            }
            if (tool.hasItemMeta() && tool.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                return Collections.singletonList(new ItemStack(blockType, blockCount));
            }
        }
        
        // Otherwise, process each block individually for loot
        Map<Material, Integer> combinedDrops = new HashMap<>();
        Random random = new Random();
        
        // Get Fortune level
        int fortuneLevel = 0;
        if (tool != null && !tool.getType().isAir() && tool.hasItemMeta() && tool.getItemMeta().hasEnchant(Enchantment.FORTUNE)) {
            fortuneLevel = tool.getItemMeta().getEnchantLevel(Enchantment.FORTUNE);
        }
        
        for (int i = 0; i < blockCount; i++) {
            // Generate drops for each block
            generateSingleBlockDrops(blockType, random, fortuneLevel, combinedDrops);
        }
        
        // Convert to ItemStack list
        List<ItemStack> result = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : combinedDrops.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(new ItemStack(entry.getKey(), entry.getValue()));
            }
        }
        
        return result;
    }
    
    /**
     * Generate drops for a single block based on Minecraft behavior (renamed from generateSingleLeafDrops)
     */
    private static void generateSingleBlockDrops(Material blockType, Random random, int fortuneLevel, Map<Material, Integer> drops) {
        // Base sapling drop chance is 5% (1/20), increased by Fortune
        double saplingChance = 0.05 + (fortuneLevel * 0.025); // +2.5% per Fortune level
        
        // Base stick drop chance is 2% (1/50), increased by Fortune
        double stickChance = 0.02 + (fortuneLevel * 0.01); // +1% per Fortune level
        
        // Special drops for specific block types (mostly leaves)
        switch (blockType) {
            case OAK_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.OAK_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                // 0.5% chance for apple, not affected by Fortune
                if (random.nextDouble() < 0.005) {
                    drops.merge(Material.APPLE, 1, Integer::sum);
                }
                break;
            case DARK_OAK_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.DARK_OAK_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                // 0.5% chance for apple, not affected by Fortune
                if (random.nextDouble() < 0.005) {
                    drops.merge(Material.APPLE, 1, Integer::sum);
                }
                break;
            case BIRCH_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.BIRCH_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case SPRUCE_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.SPRUCE_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case JUNGLE_LEAVES:
                if (random.nextDouble() < saplingChance * 0.5) { // Jungle saplings are rarer (2.5% base)
                    drops.merge(Material.JUNGLE_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case ACACIA_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.ACACIA_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case MANGROVE_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.MANGROVE_PROPAGULE, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case CHERRY_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.CHERRY_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case PALE_OAK_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.PALE_OAK_SAPLING, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                // Note: Pale Oak does NOT drop apples
                break;
            case AZALEA_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.AZALEA, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            case FLOWERING_AZALEA_LEAVES:
                if (random.nextDouble() < saplingChance) {
                    drops.merge(Material.FLOWERING_AZALEA, 1, Integer::sum);
                }
                if (random.nextDouble() < stickChance) {
                    drops.merge(Material.STICK, 1, Integer::sum);
                }
                break;
            default:
                // Any block that doesn't have specific behavior returns itself
                // This handles logs, mangrove roots, shroomlight, nether wart blocks,
                // mushroom blocks, and any future blocks we add
                drops.merge(blockType, 1, Integer::sum);
                break;
        }
    }
    
    public static boolean hasLeafDrops(List<ItemStack> drops) {
        return drops.stream().anyMatch(item -> 
            item.getType().name().contains("LEAVES"));
    }
    
    public static ItemStack damageTool(ItemStack tool) {
        if (!(tool.getItemMeta() instanceof Damageable)) {
            return tool;
        }
        
        ItemStack damaged = tool.clone();
        Damageable meta = (Damageable) damaged.getItemMeta();
        
        int durabilityLoss = 1;
        
        // Check for Unbreaking enchantment
        if (meta.hasEnchant(Enchantment.UNBREAKING)) {
            int unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
            Random random = new Random();
            // Unbreaking has a chance to prevent durability loss
            if (random.nextInt(unbreakingLevel + 1) > 0) {
                durabilityLoss = 0;
            }
        }
        
        if (durabilityLoss > 0) {
            int newDamage = meta.getDamage() + durabilityLoss;
            if (newDamage >= tool.getType().getMaxDurability()) {
                // Tool is broken
                return null;
            }
            meta.setDamage(newDamage);
            damaged.setItemMeta(meta);
        }
        
        return damaged;
    }
    
    private record TreeBlocks(Material logType, Material leafType, Material secondaryType, Material secondaryLeafType) {}
    private record TreeQuantities(int logs, int leaves, int secondary, int secondaryLeaves) {}
}