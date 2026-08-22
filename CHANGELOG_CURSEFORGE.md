# ProjectEXtended — Changelog

## Version 1.5.1

Minecraft 1.7.10 | Forge 10.13.4.1614+ | Standard ProjectE PE1.10.1+ & FMProjectE

---

### FMProjectE & Universal 64-Bit Compatibility
- **Universal Polymorphic EMC Bridge**:
  - Full dual-compatibility with both Standard ProjectE (`1.7.10-PE1.10.1+`) and **FMProjectE** (64-bit `long` API fork).
  - Dynamically adapts to method descriptors returning `int`, `long`, `double`, or `Number` across `EMCHelper`, `IEMCProxy`, and `Transmutation`.
  - Polymorphic player property reflection: automatically inspects and updates `double`, `long`, and `int` balance fields in `TransmutationProps`.
  - Added dual 64-bit `long` and `double` overloads across all Stars (Magnum, Colossal, Final) and TileEntities (`TileLink`, `TileRelay`, `TileCollector`, `TileStoneTable`).
  - Added type-safe `EMCMapper` registration and auto-sanitization to prevent `ClassCastException` in tooltips and item burning.

---

## Version 1.5.0

### Applied Energistics 2 Integration (Optional Soft Dependency)
- **ME EMC Link**:
  - Connects your AE2 storage network directly to your personal Transmutation Table pool (consumes 1 channel).
  - Exposes learned items to AE2 with live synthetic stock counts proportional to available EMC.
  - Automatically converts deposited items with EMC into personal balance and learns undiscovered knowledge.
  - In-GUI configuration: Read/Write, Read-Only, and Write-Only access modes, priority adjustments (-128 to +128), and a 16-slot ghost filter grid.
  - **Match Precision Modes**: Toggle between Exact (item, damage/metadata, NBT), Fuzzy (ignores durability/damage for tools and armor), and OreDict (matches equivalent materials across different mods).
  - Shift + Right-Click to bind to your player account in-world with sound and chat confirmation.
- **ME Transmutation Storage Cell**:
  - 64-bit storage cell insertable into standard ME Drives to expose your personal Transmutation pool to the network.
- **Modular Crash-Proof Isolation**:
  - All AE2 components are soft-loaded via runtime interface stripping; ProjectEX operates seamlessly in standalone mode if AE2 is not installed.

---

### Advanced Transmutation Search Engine
- **Query Filter Prefixes**:
  - `@<mod>`: Filter by Mod ID or display name (e.g. `@projectex`, `@minecraft`, `@"IndustrialCraft 2"`).
  - `#<range>`: Search EMC numerical ranges with SI unit scaling (e.g. `#1000-50000`, `#1k-50k`, `#10M-5G`, `#>1M`, `#<10k`).
  - `#aff` / `#affordable`: Display only items immediately affordable with your current EMC balance.
  - `$<ore>`: Filter by OreDictionary tags (e.g. `$ingot`, `$ore`, `$dust`, `$gem`, `$logWood`).
  - `%<tooltip>`: Search inside item tooltip text (e.g. `%energy`, `%radiation`, `%tier`).
  - `!<negation>`: Negate/exclude matching items (e.g. `!@minecraft`, `!ingot`).
  - `^fuel` / `^matter`: Filter by fuel or matter category.
  - `id:` / `*`: Filter by unlocalized ID or registry name.
- **Compound Logic**: Full support for AND (spaces), OR (`|`), and quoted phrases.
- **Search History**: Navigate through recent queries inside the search bar using Up and Down arrow keys.
- **Interactive Syntax Tooltip**: Hold Shift or hover over the search bar for an in-game syntax reference guide.

---

### 64-Bit Transmutation Engine
- Uncapped standard ProjectE 32-bit integer limits (Constants.TILE_MAX_EMC = 1.07G) using clean ASM transformation.
- Supports late-game item values and balances up to Double.MAX_VALUE (1.79e308) across all slots and star charge/discharge operations without numerical overflow or client crashes.

---

### WAILA In-World HUD Integration
- Added real-time diagnostics when looking at blocks:
  - **Power Flowers**: Displays owner name, generation rate (+X EMC/s with compact SI formatting), and buffered EMC.
  - **Personal & Refined Links**: Displays owner name and buffered EMC.
  - **ME EMC Links**: Displays owner name, access mode, priority, and active filter configuration.

---

### Performance & Engine Optimizations
- **NEI Search Optimization**: Converted item EMC lookups to O(1) direct reference checks, eliminating CPU lockups, GC thrashing, and framerate drops when searching in NEI.
- **Dynamic Tooltips**: Zero-allocation formatting with dynamic SI metric scaling (M, G, T, P, E, Z, Y) by default and full exact digits when holding Shift.
- **Server Tick Staggering**: Staggered Power Flower tile entity ticks using coordinate-offset modulo arithmetic to distribute server load evenly across ticks.
- **Player Caching**: Cached player UUID and EntityPlayer references using WeakReference to eliminate heavy per-tick player list scans.
- **Stone Table Recipe Optimization**: Replaced linear recipe iteration with O(1) hash map lookups.
- **Texture Cleanup**: Cleaned animated texture .mcmeta files to eliminate OptiFine CPU stitching loops.
