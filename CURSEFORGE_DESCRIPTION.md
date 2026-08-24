# ProjectEXtended (Minecraft 1.7.10)

**ProjectEXtended** is a complete, performance-optimized 1.7.10 backport and expansion of **Project EX**, originally created by LatvianModder and the FTB Team for Minecraft 1.12.2.

This mod expands upon ProjectE by introducing **Single-Block Power Flowers**, **16 Tiers of Matter**, **Magnum & Colossal Stars**, **EMC Automation Links**, an integrated **64-Bit Transmutation Engine**, **Applied Energistics 2 Integration**, **Advanced Search Filters**, and **WAILA Diagnostics**.

---

## Key Features

### 1. Single-Block Power Flowers
- Compact 1x1 blocks providing passive EMC generation across 16 tiers (from MK1 up to 48 Trillion EMC/s with The Final Power Flower).
- Automatically deposits generated EMC directly into the player's personal Transmutation Table pool wirelessly.
- Server-friendly architecture: tick-staggered generation using coordinate-offset modulo and weak-referenced player caching.

### 2. Built-in 64-Bit Transmutation Engine
- Uncaps standard ProjectE's 32-bit integer limits (`Constants.TILE_MAX_EMC = 1.07G`) using clean ASM transformation.
- Supports endgame item values and balances up to `Double.MAX_VALUE` (1.79e308) across all slots and charge/discharge operations without numerical overflow or client crashes.
- Clean knowledge normalization: burning bulk stacks learns items without duplicating entries or inflating stack counts.

### 3. Universal Dual Compatibility (Standard ProjectE & FMProjectE)
- Built-in polymorphic EMC bridge that dynamically detects and runs on both **Standard ProjectE** (`1.7.10-PE1.10.1+`) and **FMProjectE** (64-bit `long` API fork) out of the box without requiring manual patches or configuration changes.

### 4. Applied Energistics 2 (AE2) Integration *(Optional Soft Dependency)*
- **ME EMC Link**:
  - Connects your AE2 storage network directly to your personal Transmutation Table pool (consumes 1 channel).
  - Exposes learned items to the ME storage network with live synthetic stock counts proportional to available EMC.
  - Automatically converts deposited items with EMC into personal balance and learns undiscovered knowledge.
  - In-GUI configuration: Read/Write, Read-Only, and Write-Only access modes, priority adjustments (-128 to +128), and a 16-slot ghost filter grid.
  - **Match Precision Modes**: Toggle between **Exact** (item, metadata, NBT), **Fuzzy** (ignores durability/damage for tools and armor), and **OreDict** (matches cross-mod equivalents).
  - Shift + Right-Click to bind to your player account in-world with audio and chat confirmation.
- **ME Transmutation Storage Cell**:
  - 64-bit storage cell insertable into standard ME Drives to expose your personal Transmutation pool to the network.
- **Crash-Proof Isolation**: All AE2 components are soft-loaded; if AE2 is not installed, the mod operates seamlessly in standalone mode without loading AE2 classes.

### 5. Advanced Transmutation Search Engine
- **Search Filters**:
  - `@<mod>`: Filter by Mod ID or display name (e.g. `@projectex`, `@minecraft`, `@"IndustrialCraft 2"`).
  - `#<range>`: Search EMC ranges with SI unit scaling (e.g. `#1000-50000`, `#1k-50k`, `#10M-5G`, `#>1M`, `#<10k`).
  - `#aff` / `#affordable`: Display only items immediately affordable with your current EMC balance.
  - `$<ore>`: Filter by OreDictionary tags (e.g. `$ingot`, `$ore`, `$dust`, `$gem`, `$logWood`).
  - `%<tooltip>`: Search inside item tooltip text (e.g. `%energy`, `%radiation`, `%tier`).
  - `!<negation>`: Negate/exclude matching items (e.g. `!@minecraft`, `!ingot`).
  - `^fuel` / `^matter`: Filter by fuel or matter category.
  - `id:` / `*`: Filter by unlocalized ID or registry name.
- **Compound Logic**: Support for AND (spaces), OR (`|`), and quoted phrases.
- **Search History**: Navigate through recent queries inside the search bar using Up and Down arrow keys.
- **Interactive Syntax Tooltip**: Hold Shift or hover over the search bar for an in-game syntax reference guide.

### 6. Magnum & Colossal Stars
- Klein Star progression expanded with **Magnum Stars** (up to 209.7 Billion EMC) and **Colossal Stars** (up to 858.9 Trillion EMC).
- **The Final Star**: Infinite EMC reservoir item with full charge/discharge capabilities.

### 7. Personal & Refined EMC Links
- In-world automation blocks allowing hoppers, pipes, item conduits, and transport networks to interact directly with player Transmutation pools.

### 8. WAILA In-World HUD Integration *(Optional Soft Dependency)*
- Live in-world diagnostics when looking at blocks:
  - **Power Flowers**: Owner name, generation rate (+X EMC/s with compact SI formatting), and buffered EMC.
  - **Personal & Refined Links**: Owner name and buffered EMC.
  - **ME EMC Links**: Owner name, access mode, priority, and active filter configuration.

### 9. Performance & NEI Optimization
- Tooltips utilize zero-allocation O(1) item checks with dynamic SI unit scaling by default and exact digit formatting on Shift.
- High FPS retained even when querying thousands of items simultaneously in NEI.

---

## Requirements

| Component | Required Version |
| :--- | :--- |
| **Minecraft** | `1.7.10` |
| **Minecraft Forge** | `10.13.4.1614` or newer |
| **ProjectE** | Standard ProjectE `1.7.10-PE1.10.1+` **OR** `FMProjectE` |
| **Applied Energistics 2** *(Optional)* | `rv3-beta-6` or compatible |
| **WAILA** *(Optional)* | `1.5.10` or compatible |
| **NotEnoughItems (NEI)** *(Optional)* | `1.0.5+` |

---

## Credits

- Original 1.12.2 mod by **LatvianModder** and the **FTB Team**.
- Base mod ProjectE by **MozeIntel**, **sinkillerj**, **williewillus**, and contributors.
- 1.7.10 Backport, 64-Bit Engine & Extended Features by **YatzCore**.
