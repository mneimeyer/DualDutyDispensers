package org.neimeyer.dualdutydispensers.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.neimeyer.dualdutydispensers.DualDutyDispensers;
import org.neimeyer.dualdutydispensers.blocks.base.CustomDispenserBlock;
import org.neimeyer.dualdutydispensers.config.TreeFarmConfig;
import org.neimeyer.dualdutydispensers.utils.SaplingHelper;
import org.neimeyer.dualdutydispensers.utils.SoilHelper;
import org.neimeyer.dualdutydispensers.utils.TreeGrowthHelper;

import java.util.List;
import java.util.Random;

public class TreeFarmBlock extends CustomDispenserBlock {
    
    // Slot definitions for 3x3 dispenser inventory layout:
    // [ # T # ]    [0][1][2]
    // [ F # S ]    [3][4][5]
    // [ # D # ]    [6][7][8]
    public static final int SAPLING_SLOT = 1;     // T - Top center
    public static final int FERTILIZER_SLOT = 3;  // F - Left center
    public static final int SHEARS_SLOT = 5;      // S - Right center
    public static final int SOIL_SLOT = 7;        // D - Bottom center
    
    private final Random random = new Random();
    
    public TreeFarmBlock(Location location) {
        super(location, "tree-farm");
    }
    
    @Override
    public void onRedstoneActivation() {
        Inventory inventory = getInventory();
        if (inventory == null) return;
        
        TreeFarmConfig config = DualDutyDispensers.getInstance().getConfigManager().getTreeFarmConfig();
        if (config == null) return;
        
        // Check if we have minimum requirements (sapling and soil)
        ItemStack saplings = inventory.getItem(SAPLING_SLOT);
        ItemStack soil = inventory.getItem(SOIL_SLOT);
        ItemStack fertilizer = inventory.getItem(FERTILIZER_SLOT);
        ItemStack shears = inventory.getItem(SHEARS_SLOT);
        
        if (saplings == null || saplings.getType().isAir() || soil == null || soil.getType().isAir()) {
            return;
        }
        
        if (!SaplingHelper.isSapling(saplings.getType()) || !SoilHelper.isSoil(soil.getType())) {
            return;
        }
        
        // Check compatibility
        if (config.isRequireSoil() && !SoilHelper.isCompatible(saplings.getType(), soil.getType())) {
            return;
        }
        
        // Check if output is possible
        Block outputBlock = getOutputBlock();
        if (!canOutput(outputBlock)) {
            return;
        }
        
        // Determine tree size and resource consumption
        SaplingHelper.TreePlantingResult plantingResult = SaplingHelper.determinePlantingMode(
            saplings.getType(), 
            saplings.getAmount(), 
            soil.getAmount()
        );
        
        // Check if we can plant at all
        if (!plantingResult.canPlant()) {
            return; // Not enough resources or invalid combination
        }
        
        // Calculate growth chance
        double baseChance = config.getBaseGrowthChance();
        double bonusChance = 0.0;
        
        // Check if this sapling type requires bone meal
        if (SoilHelper.requiresBonemeal(saplings.getType())) {
            if (fertilizer == null || fertilizer.getType().isAir()) {
                return; // Bone meal is required but not present
            }
            // For required bone meal, set the bonus chance to 100% to ensure growth
            bonusChance = 100.0;
        } else if (fertilizer != null && !fertilizer.getType().isAir()) {
            bonusChance = config.getBoneMealBonus();
        }
        
        double totalChance = (baseChance + bonusChance) / 100.0; // Convert percentage to decimal
        double roll = random.nextDouble();
        
        // Attempt to grow the tree
        if (roll < totalChance) {
            // Consume saplings
            saplings.setAmount(saplings.getAmount() - plantingResult.saplingsToConsume());
            if (saplings.getAmount() <= 0) {
                inventory.setItem(SAPLING_SLOT, null);
            }
            
            // Handle soil conversion (only on success, and only for specific trees)
            if (SoilHelper.convertsSoil(saplings.getType()) && 
                (saplings.getType() != Material.SPRUCE_SAPLING || plantingResult.is2x2())) {
                
                Material newSoil = SoilHelper.getConvertedSoil(saplings.getType());
                ItemStack newSoilStack = new ItemStack(newSoil, soil.getAmount());
                inventory.setItem(SOIL_SLOT, newSoilStack);
            }
            
            // Generate tree drops using the actual planting mode
            List<ItemStack> drops = TreeGrowthHelper.generateTreeDrops(
                saplings.getType(), 
                plantingResult.is2x2(), 
                shears
            );
            
            // Consume shears durability if used
            if (shears != null && !shears.getType().isAir() && TreeGrowthHelper.hasLeafDrops(drops)) {
                shears = TreeGrowthHelper.damageTool(shears);
                inventory.setItem(SHEARS_SLOT, shears); // Set to null if shears broke, otherwise update durability
            }
            
            // Output drops using the parent class method
            outputItems(drops);
        }
        
        // ALWAYS consume fertilizer on an attempt (regardless of success/failure)
        if (fertilizer != null && !fertilizer.getType().isAir() && config.isConsumeBoneMeal()) {
            fertilizer.setAmount(fertilizer.getAmount() - 1);
            if (fertilizer.getAmount() <= 0) {
                inventory.setItem(FERTILIZER_SLOT, null);
            }
        }
    }
    
    @Override
    public void saveData() {
        // Tree farms don't need to persist additional state beyond their inventory
        // Minecraft automatically saves the inventory
        // If we needed to save custom data, we would use the BlockManager's data storage here
    }
    
    @Override
    public void loadData() {
        // Tree farms don't need to load additional state beyond their inventory
        // Minecraft automatically loads the inventory
        // If we needed to load custom data, we would use the BlockManager's data storage here
    }
    
    @Override
    public boolean allowsNaturalDispensing() {
        return false; // Tree farms don't use natural dispensing
    }
    
    @Override
    public boolean handleHopperInput(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        
        Inventory inventory = getInventory();
        if (inventory == null) {
            return false;
        }
        
        // Determine which slot this item should go to
        int targetSlot = getTargetSlotForItem(item);
        if (targetSlot == -1) {
            return false; // Item doesn't belong in the tree farm
        }
        
        // Check if we can add to the target slot
        ItemStack existing = inventory.getItem(targetSlot);
        
        if (existing == null || existing.getType().isAir()) {
            // Slot is empty, take ONE item from the hopper stack
            ItemStack newItem = item.clone();
            newItem.setAmount(1);
            inventory.setItem(targetSlot, newItem);
            
            // Remove one from the source
            item.setAmount(item.getAmount() - 1);
            return true;
        }
        
        if (existing.isSimilar(item)) {
            // Same item type, try to stack
            if (existing.getAmount() < existing.getMaxStackSize()) {
                existing.setAmount(existing.getAmount() + 1);
                // Remove one from the source
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        
        return false; // Couldn't add the item
    }
    
    private int getTargetSlotForItem(ItemStack item) {
        if (SaplingHelper.isSapling(item.getType())) {
            return SAPLING_SLOT;
        }
        if (item.getType() == Material.SHEARS) {
            return SHEARS_SLOT;
        }
        if (SoilHelper.isSoil(item.getType())) {
            return SOIL_SLOT;
        }
        if (item.getType() == Material.BONE_MEAL) {
            return FERTILIZER_SLOT;
        }
        return -1; // Item doesn't belong in the tree farm
    }
    
    @Override
    public boolean isValidItemForSlot(int slot, ItemStack item) {
        // Handle null / air items first
        if (item == null || item.getType().isAir()) {
            return true;
        }
        
        // Use enhanced switch expression for cleaner code
        return switch (slot) {
            case SAPLING_SLOT -> SaplingHelper.isSapling(item.getType());
            case SHEARS_SLOT -> item.getType() == Material.SHEARS;
            case SOIL_SLOT -> SoilHelper.isSoil(item.getType());
            case FERTILIZER_SLOT -> item.getType() == Material.BONE_MEAL;
            default -> false;
        };
    }
}