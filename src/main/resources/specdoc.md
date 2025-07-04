## Dual Duty Dispensers

### 0. Blocks to Make
Each block uses a standard Dispenser block with the CustomModelData set.

- **Tree Farm:** One Block Shop for Log Production. Includes drops from leaves or the leaves themselves. Enchantment support planned.
- **Compressor:** Convert Many into One (Ingots to Blocks, etc.)
- **Decompressor:** Convert One into Many (Blocks to Ingots, etc.)
- **Siphon:** Dispenses filled buckets when adjacent to source fluid or full cauldron and contains empty buckets. Can emit Lava, Water or Powder Snow buckets or Water Bottles.
- **Ender Sender:** Take Items from reality and queue them in a data structure of some sort in memory/on disk. Paired one to one with Receiver by Use-Clicking Receiver on placed Sender. Ensure queue is written to permanent storage when the unload event runs and is read from when the load event runs.
- **Ender Receiver:** Take Items from the queue and inject them into reality
- **Block Breaker:** Mirror Sheep Shearing, use tools in inventory to break block in front of face. Enchantment support planned.
- **Murder Block:** Also mirrors Sheep Shearing, Use weapons in inventory to attack mobs in front of the block. Enchantment support planned.
- **Redstone Clock:** Bonus block! Based on Observer Block and not Dispenser.

---

### 1. Custom Recipes
**Basic Recipe Format on 3x3 crafting grid:**  
Row 1: Block, Item, Block  
Row 2: Item, Dispenser, Item  
Row 3: Block, Item, Block

- **Tree Farm:**  
  Item: Diamond Axes  
  Block: Redstone Block

- **Compressor:**  
  Item: Piston  
  Block: Redstone Block

- **Decompressor:**  
  Item: Sticky Piston  
  Block: Redstone Block

- **Siphon:**  
  Item: Empty Buckets  
  Block: Redstone Block

- **Ender Sender:**  
  Item: Eye of Ender (North/South), Hopper (East/West)  
  Block: Obsidian

- **Ender Receiver:**  
  Item: Eye of Ender (North/South), Dropper (East/West)  
  Block: Obsidian

- **Block Breaker:**  
  Item: Diamond Pickaxes  
  Block: Redstone Block

- **Murder Block:**  
  Item: Diamond Sword  
  Block: Redstone Block

- **Redstone Clock:**  
  Item: Repeater (East/West), Comparator (North/South)  
  Block: Redstone Block

---

### 2. On Redstone Trigger
- **Tree Farm:** Attempt to grow a virtual tree if inventory contains soil, saplings, and optionally shears. Emit results through face. Trees that support 2x2 planting will attempt this if four saplings available and four soil available. On success 4 saplings are removed instead of just one and drops quantities as appropriate. If shears are present leaf blocks are dropped instead of loot. If sapling growth would convert the soil under it in nature then soil slot is converted as well.
- **Compressor:** Check inventory for enough blocks to convert checking against the list of recipes. Emit result of first match through face. If no items can be compressed, emit the first uncompressable item through the face.
- **Decompressor:** Check inventory for a block to convert from the list of recipes. Emit results of first match through face. If no items can be decompressed, emit the first undecompressable result through the face.
- **Siphon:** Fill empty buckets or empty bottles in inventory if possible from the adjacent source block or full cauldron. Remove source block or empty cauldron. Emit through face.
- **Ender Sender:** Remove item from inventory, add to queue for receiver. Emit nothing. If unpaired then do nothing.
- **Ender Receiver:** Check queue from sender, receive one and emit one item through face.
- **Block Breaker:** Uses `breakNaturally(tool)` with the first available tool from inventory (following the pattern used by Sheep Shearing Dispenser), applying enchantments (Fortune, Silk Touch, Unbreaking) and vanilla logic. Emit nothing from the dispenser; drops handled by environment.
- **Murder Block:** Mirror sheep shearing. Emit nothing from the dispenser. Drops handled by environment.
- **Redstone Clock:** Generates a Redstone pulse every four ticks (configurable) regardless of whether something would ordinarily be observed.

---

### 3. Drops When Block is Broken
- All blocks drop themselves.
- All blocks drop their inventory.
- **Ender Sender/Receiver:** If either end of a block pair is broken the remaining items in the queue are dropped as if they were contained in the inventory of the block being broken.

---

### 4. Inventory Locking (for UI/user interaction)
- **Tree Farm:** Lock all slots except allow sapling in "north," dirt/soils in "south," and shears in the middle.
- **Compressor:** No locking.
- **Decompressor:** No locking.
- **Siphon:** No locking, but only allow empty buckets or empty bottles.
- **Ender Sender:** No locking of local inventory. Limits on queue. If queue is empty, one of any item can be added. If queue contains items but less than one full stack (64, 16 or 1 as appropriate), increment quantity and remove from local inventory.
- **Ender Receiver:** Fully lock inventory. Can only pull (and emit) a single item from queue. If unable to emit, then store in inventory.
- **Block Breaker:** Not locked, but only allow tools (config-based whitelist?).
- **Murder Block:** Not locked, but only allow weapons (config-based whitelist?).

---

### 5. Hopper (or Other Injection/Extraction) Interaction
- **Tree Farm:** Will receive pushed saplings, soil, or shears into the dispenser.
- **Siphon:** Only accept empty buckets or empty bottles.
- **Ender Receiver:** Reject all injections.
- **Block Breaker:** Only accept tools.
- **Murder Block:** Only accept weapons.
- **All other blocks:** Normal injects.
- **All blocks:** Normal extractions.

---

### 6. Config Thoughts
- Master config to enable/disable any of the items (or paired items).
- Folder with individual configs for each type of block.
- Each config includes the default recipe (but allows changing).
- Each config includes **Display Name** and **Display Lore** override options.
- **Block Breaker/Murder Block:** Whitelist of allowed implements
- **Compressor/Decompressor:**
    - List of recipes, e.g. One block of X ↔ Nine ingots of X.
    - Directionality (Compress, Decompress, Both)
- **Tree Farm:**
    - **Base Random:** Percentage chance to grow per activation (can be set to 100%).
    - **Bone Meal Bonus:** Additional percentage bonus applied if Bone Meal is present (can also be set to 100%).
    - Final chance to grow = base + bonus (e.g., 30% base, 25% bonus = 55% total). Exceeding 100% is permitted but has no special impact.
- **Redstone Clock:** How often to pulse in ticks

---

### Default Lore Lines
- **Tree Farm:** *Mother Nature’s best kept secret.*
- **Compressor:** *Under intense pressure things* **change** *.*
- **Decompressor:** ***Nothing*** *stays whole forever.*
- **Siphon:** *Note: Buckets not included.*
- **Ender Sender:** *A tear in space swallows all around it.*
- **Ender Receiver:** *Things* *appear from afar.*
- **Block Breaker:** *A tireless worker.*
- **Murder Block:** *Sometimes violence* **is** *the answer.*
- **Redstone Clock:** *You hear soft ticking from inside.*

