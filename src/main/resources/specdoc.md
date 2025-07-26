## Dual Duty Dispensers

### 0. Blocks to Make
Each block uses a standard Dispenser block with the CustomModelData set.

#### Actors:
- **Siphon:** Dispenses filled containers when adjacent to a source block of fluid or a full cauldron and the Siphon contains empty buckets or bottles. Can emit Lava, Water or Powder Snow buckets or Water Bottles.
- **Block Breaker:** Mirror Sheep Shearing, use tools in inventory to break block in front of face. Enchantment support planned.
- **Murder Block:** Also mirrors Sheep Shearing, Use weapons in inventory to attack mobs in front of the block. Enchantment support planned.

#### Transfers:
- **Ender Sender:** Take Items from reality and queue them for a paired receiver.
- **Ender Receiver:** Take Items from its queue and inject them into reality.
- **Warp Sender:** An evolved Ender Sender. It can be paired to multiple Ender Receivers.
- **Warp Receiver:** An evolved Ender Receiver. It can be paired to multiple Ender Senders.

#### Transformers:
- **Tree Farm:** One Block Shop for Log Production. Includes drops from leaves or the leaves themselves. Enchantment support planned.
- **Compressor:** Convert Many into One (Ingots to Blocks, etc.)
- **Decompressor:** Convert One into Many (Blocks to Ingots, etc.)
- **Grinder:** Reduce input block to "lower" form. (Stone to Cobblestone, etc)

#### Utility:
- **Redstone Clock:** Bonus block! Based on Observer Block and not Dispenser.

---

### 1. Custom Recipes
**Basic Recipe Format on 3x3 crafting grid:**  
Row 1: Block, Item, Block  
Row 2: Item, BuiltOn, Item  
Row 3: Block, Item, Block

The BuiltOn block in the center is always a dispenser unless noted below.

#### Actors:
- **Siphon:**  
  Item: Empty Buckets  
  Block: Redstone Block

- **Block Breaker:**  
  Item: Diamond Pickaxes  
  Block: Redstone Block

- **Murder Block:**  
  Item: Diamond Sword  
  Block: Redstone Block

#### Transfers:
- **Ender Sender:**  
  Item: Eye of Ender (North/South), Hopper (East/West)  
  Block: Obsidian

- **Ender Receiver:**  
  Item: Eye of Ender (North/South), Dropper (East/West)  
  Block: Obsidian

- **Warp Sender:**
  Center: Ender Sender  
  Item: Shulker Chest (any color)  
  Block: Echo Shards

- **Warp Receiver:**
  Center: Ender Receiver  
  Item: Shulker Chest (any color)  
  Block: Echo Shards

#### Transformers:
- **Tree Farm:**  
  Item: Diamond Axes  
  Block: Redstone Block

- **Compressor:**  
  Item: Piston  
  Block: Redstone Block

- **Decompressor:**  
  Item: Sticky Piston  
  Block: Redstone Block

- **Grinder:**  
  Item: Flint  
  Block: Stone Bricks

#### Utility:
- **Redstone Clock:**  
  Center: Observer  
  Item: Repeater (East/West), Comparator (North/South)  
  Block: Redstone Block

---

### 2. On Redstone Trigger

#### Actors

- **Siphon:** Attempt to pull fluid source blocks (water or lava) from directly in front and fill containers from inventory.
- **Block Breaker:** Breaks the block in front as if mined using items in the internal inventory.
- **Murder Block:** Attacks entities directly in front with weapons contained in the internal inventory.

#### Transfers

- **Ender Sender:** Transfers items placed inside to its paired Ender Receiver(s), even across dimensions. Only valid pairs work.
- **Ender Receiver:** Receives items from its paired Ender Sender(s).  
- **Warp Sender:** Transfers items to multiple paired Receivers at once.
- **Warp Receiver:** Receives items from any number of paired Senders.

#### Transformers

- **Tree Farm:** Attempts to plant saplings and trigger tree growth (if conditions are right). 2x2 Trees are supported but require four saplings and four soil.
- **Compressor:** Compresses compatible blocks (e.g., iron ingots to iron blocks) using configurable recipes which, by default, mirror standard Minecraft recipes.
- **Decompressor:** Decompresses compatible blocks into base items (e.g., iron blocks to iron ingots).
- **Grinder:** Pulverizes items into smaller components (e.g., cobblestone to gravel, gravel to flint).

#### Utility

- **Redstone Clock:** Emits an adjustable redstone pulse at set intervals, providing a timer-based signal.

---

### 3. Drops When Block is Broken
- All blocks drop themselves.
- All blocks drop their inventory.
- **Ender Sender/Receiver:** If either end of a block pair is broken the remaining items in the queue are dropped as if they were contained in the inventory of the block being broken.
- **Warp Sender/Receiver:** If the Warp end of a net is broken then drop the remaining items in the queue as if they were contained in the inventory of the block being broken. If one of the Ender ends of a net is broken but more Ender ends remain do nothing but update the tracking.

---

### 4. Inventory Locking (for UI/user interaction)

#### Actors
- **Siphon:** No locking, but only allow empty buckets or empty bottles.
- **Block Breaker:** Not locked, but only allow tools (config-based whitelist?).
- **Murder Block:** Not locked, but only allow weapons (config-based whitelist?).

#### Transfers
- **Warp/Ender Sender:** No locking of local inventory.
- **Warp/Ender Receiver:** Fully lock inventory insertion. Can only pull (and emit) a single item from queue. If unable to emit, then store in inventory. Stored items can be removed manually.

#### Transformers
- **Tree Farm:** Lock all slots except allow sapling in "north," dirt/soils in "south," and shears in the middle.
- **Compressor:** No locking.
- **Decompressor:** No locking.
- **Grinder:** No locking.

#### Utility
- **Redstone Clock:** No inventory

---

### 5. Hopper (or Other Injection/Extraction) Interaction
Except as listed below all blocks accept normal injects and extractions.

- **Siphon:** Only accept empty buckets or empty bottles.
- **Block Breaker:** Only accept tools.
- **Murder Block:** Only accept weapons.
- **Warp/Ender Receiver:** Reject all injections.
- **Tree Farm:** Will receive pushed saplings, soil, bone meal, or shears into the dispenser.

---

### 6. Config Thoughts
- Master config to enable/disable any of the items (or paired items).
- Folder with individual configs for each type of block.
- Each config includes the default recipe (but allows changing).
- Each config includes **Display Name** and **Display Lore** override options.
- **Block Breaker/Murder Block:** Whitelist of allowed implements
- **Redstone Clock:** How often to pulse in ticks
- All transformers will utilize a config similar to below. The trees key will contain one entry for each "recipe". Some more abstraction will happen.

```yaml
TreeFarm:
	base_chance: 0.25
	bonemeal_bonus: 0.50
	sapling_consumed: true

trees:
  PaleOak_1x1:
    sapling: cherry_sapling
    sapling_count: 1
    bonemeal: optional
    soils: [dirt, grass_block]
    required_enchantments: []

    variants:
      - name: Pale Oak (Cherry Tree)
        weight: 100
        logs_count: { min: 6, max: 10 }
        logs:
          cherry_log:
            weight: 100
            max: -1
        leaves_count: { min: 40, max: 70 }
        leaves:
          cherry_leaves:
            weight: 100
            max: -1
```
---

### Default Lore Lines
#### Actors
- **Siphon:** Note: Buckets not included.
- **Block Breaker:** A tireless worker.
- **Murder Block:** Sometimes violence *is* the answer.

#### Transfers
- **Ender Sender:** A tear in space swallows all around it.
- **Ender Receiver:** *Things* appear from afar.
- **Warp Sender:** Where intent becomes inevitability.
- **Warp Receiver:** An anchor for impossible arrivals..

#### Transformers
- **Tree Farm:** Mother Nature’s best kept secret.
- **Compressor:** Under intense pressure things* *change*,
- **Decompressor:** *Nothing* stays whole forever.
- **Grinder:** Crush. Pulverize. Repeat.

#### Utility
- **Redstone Clock:** You hear soft ticking from inside.

---

### Warp and Ender Sender/Receivers

The premise is to extend item transfer over distances in the same conceptual way that an Ender Chest's contents can be accessed from any location with another Ender Chest. The core implementation needs are: Pairing and a Queue for each Pairing.

#### Pairing
- Ender blocks can be paired only one Sender to one Receiver.
- Warp blocks can be paired to multiple Ender blocks of the other type.
- Pairing is accomplished by use-clicking Sender to a placed Receiver block or vice-versa

#### The Queue
- Senders inject one item into a queue per Redstone Impulse received
- Receivers take one item from a queue per Redstone Impulse received (and then emit it)
- If a queue is empty any single item can be inserted into it.
- If a queue contains one or more items then more of that item may be inserted up to a full stack (either 64, 16 or 1 as appropriate) for that item.
- I don't know if a queue should know it's members or members should know their queue or both. Probably both.