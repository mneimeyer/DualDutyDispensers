# DualDutyDispensers

A PaperMC 1.21+ plugin that adds specialized utility blocks based on the vanilla Dispenser. These custom blocks act as Tier 2 dispensers — extending the "take thing, output thing" mechanic with smart behaviors and enhanced logic.

## 🔧 Features

- **Tree Farm** – Grows trees with saplings/soil, optionally collecting leaves with shears
- **Compressor / Decompressor** – Transforms materials in bulk (e.g., ingots ⇄ blocks)
- **Siphon** – Fills buckets/bottles from nearby fluids
- **Ender Sender / Receiver** – Queue-based remote item transfer between paired blocks
- **Block Breaker / Murder Block** – Uses tools/weapons to break blocks or attack mobs
- **Redstone Clock** – Emits redstone pulses on a configurable tick cycle

All blocks are crafted using standard recipes that embed a dispenser at the core and define a CustomModelData value.

## 📦 Installation

1. Install [PaperMC 1.21.4+](https://papermc.io/)
2. Drop the `DualDutyDispensers.jar` into your server's `plugins/` folder
3. Restart the server

## 🧪 Compatibility

- Requires **no external dependencies**
- Tested with **Paper 1.21.4**
- Should work with **any plugin** that doesn't override dispenser behavior

## 🛠 Configuration

Each block has its own config in the `plugins/DualDutyDispensers/` folder, where you can:

- Enable/disable block types
- Change recipes, display names, and lore
- Define block-specific behavior (e.g., compression rules, redstone timing)

## 🎯 Crafting

All blocks use this pattern (3x3 grid):

|   |   |   |
|---|---|---|
| B | I | B |
| I | D | I |
| B | I | B |

Where:
- `D` = Dispenser
- `I` = Item (block-specific)
- `B` = Base block (e.g., Redstone Block, Obsidian)

See the config files for full recipes.

## 📚 Blocks Implemented (v1.0.0)

- ✅ Tree Farm
- ✅ Redstone Clock

## 🚧 Roadmap (v1.0.1+)

- ➕ Compressor / Decompressor
- ➕ Siphon
- ➕ Ender Sender / Receiver
- ➕ Block Breaker / Murder Block
- ➕ Enchantment support for tools and weapons

## 📄 License

This project is licensed under the MIT License. See [LICENSE](./LICENSE) for details.

## 🙏 A Friendly Request

This project is licensed under the MIT license, so you're free to use, modify, and share it — including in your own projects.

However, if you improve the plugin or add new features, I’d really appreciate it if you shared them back via a pull request or fork link. While not required, contributions help everyone benefit.

## 🧑‍💻 Contributing

Pull requests welcome! Please read [CONTRIBUTING.md](./CONTRIBUTING.md) first.

## 🤝 Code of Conduct

All contributors are expected to follow our [Code of Conduct](./CODE_OF_CONDUCT.md).
