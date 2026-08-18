# ProjectEXtended — Changelog (v1.0.0 -> v1.5.0)

Minecraft 1.7.10 | Forge 10.13.4.1614+ | ProjectE 1.7.10-PE1.10.1+

---

### New Features

#### Applied Energistics 2 Integration (Optional Soft Dependency)
- **ME EMC Link**:
  - Connects your AE2 storage network directly to your personal Transmutation Table pool (consumes 1 channel).
  - Exposes learned items to AE2 with live synthetic stock counts proportional to available EMC.
  - Automatically absorbs deposited items with EMC directly into the player's balance and learns undiscovered knowledge.
  - In-GUI configuration: Read/Write, Read-Only, and Write-Only access modes, priority adjustments (-128 to +128), and a 16-slot ghost filter grid.
  - **Match Precision Modes**: Toggle between Exact (item, damage/metadata, NBT), Fuzzy (ignores durability/damage for tools and armor), and OreDict (matches equivalent materials across different mods).
  - In-world claiming via Shift + Right-Click with sound and chat confirmation.
- **ME Transmutation Storage Cell**:
  - 64-bit storage cell insertable into standard ME Drives to expose personal Transmutation storage.
- **Modular Crash-Proof Isolation**:
  - All AE2 components are soft-loaded via runtime interface stripping; ProjectEX functions fully in standalone mode if AE2 is not installed.

#### Advanced Transmutation Search Engine
- **Query Filter Prefixes**:
  - `@<mod>`: Filter by Mod ID or display name (e.g. `@projectex`, `@minecraft`, `@"IndustrialCraft 2"`).
  - `#<range>`: Search EMC numerical ranges with SI unit scaling (e.g. `#1000-50000`, `#1k-50k`, `#10M-5G`, `#>1M`, `#<10k`).
  - `#aff` / `#affordable`: Display only items immediately affordable with current EMC balance.
  - `$<ore>`: Filter by OreDictionary tags (e.g. `$ingot`, `$ore`, `$dust`, `$gem`, `$logWood`).
  - `%<tooltip>`: Search inside item tooltip text (e.g. `%energy`, `%radiation`, `%tier`).
  - `!<negation>`: Negate/exclude matching items (e.g. `!@minecraft`, `!ingot`).
  - `^fuel` / `^matter`: Filter by fuel or matter category.
  - `id:` / `*`: Filter by unlocalized ID or registry name.
- **Compound Logic**: Support for AND (spaces), OR (`|`), and quoted phrases.
- **Search History**: Cycle previous queries inside the search bar using Up and Down arrow keys.
- **Interactive Syntax Tooltip**: Hold Shift or hover over the search bar for an in-game syntax reference guide.

#### WAILA In-World HUD Integration
- Added real-time diagnostics on block look:
  - **Power Flowers**: Displays owner name, generation rate (+X EMC/s with compact SI formatting), and buffered EMC.
  - **Personal & Refined Links**: Displays owner name and buffered EMC.
  - **ME EMC Links**: Displays owner name, access mode, priority, and active filter configuration.

---

### Core Fixes & Engine Enhancements

#### 64-Bit Transmutation Engine
- Removed standard ProjectE 32-bit integer limits (Constants.TILE_MAX_EMC = 1.07G) using clean ASM transformation.
- Supports late-game item values and balances up to Double.MAX_VALUE (1.79e308) without numerical overflow, stack corruption, or JVM VerifyError crashes.

#### Knowledge & Stack Normalization
- Fixed a bug where burning items in bulk (e.g. 64x Stone) duplicated knowledge list entries or displayed stacked counts in matter output slots.
- Transmutation output slots now consistently display single item representations (stackSize = 1).
- Shift-clicking items from inventory into the Transmutation Table now reliably learns knowledge and updates balances in real time.

#### Atomic State Synchronization
- Fixed a bug where closing/reopening the Transmutation Table or interacting with output slots clobbered or desynchronized current player EMC.
- Added bidirectional tile entity network description packets (S35PacketUpdateTileEntity) for instant client-server state synchronization upon claiming blocks.

---

### Performance & Optimizations

#### NEI Search Optimization
- Converted item EMC lookups to O(1) direct reference checks, completely eliminating 100% CPU lockups, GC thrashing, and framerate drops when typing queries into the NEI search bar.
- Tooltips utilize zero-allocation formatting with dynamic SI metric scaling (M, G, T, P, E, Z, Y) and full exact numbers when holding Shift.

#### Server Tick Staggering & Caching
- Staggered Power Flower tile entity ticks using coordinate-offset modulo arithmetic to prevent simultaneous tick spikes across large flower setups.
- Cached player UUID and EntityPlayer references using WeakReference to eliminate heavy per-tick player list scans.
- Replaced linear recipe iteration in Stone Tables with O(1) hash map lookups.
- Cleaned animated texture .mcmeta files to eliminate OptiFine CPU stitching loops.
