# ProjectEXtended (1.7.10) — Version 1.5

A feature-complete 1.7.10 backport and expansion of **Project EX**, originally created by LatvianModder and the FTB Team for Minecraft 1.12.2.

This version introduces an integrated **64-Bit Transmutation Engine**, **Advanced Transmutation Search Filters**, **Applied Energistics 2 Integration**, and **WAILA Diagnostics**, maintaining full compatibility with ProjectE 1.7.10 while supporting endgame EMC values up to Double.MAX_VALUE ($1.79 \times 10^{308}$) without numerical overflows or client crashes.

---

## Key Features

### 1. Single-Block Power Flowers (Bonsai Pots)
- Compact 1x1 blocks providing passive EMC generation across 16 tiers (up to 48 Trillion EMC/s with The Final Power Flower).
- Automatically deposits generated EMC directly into the player's personal Transmutation Table pool wirelessly.
- Server-friendly: tick-staggered generation using coordinate-offset modulo and weak-referenced player caching.

### 2. 64-Bit Transmutation Engine
- Clean ASM transformation lifting standard ProjectE's 32-bit `Constants.TILE_MAX_EMC` (1.07G) barrier.
- Safely insert, store, and withdraw items with massive EMC values seamlessly.
- Knowledge normalization: burning bulk stacks learns items without duplicating entries or inflating stack counts.

### 3. Magnum & Colossal Stars
- Klein Star progression expanded with Magnum Stars (up to 209.7B EMC) and Colossal Stars (up to 858.9T EMC).

### 4. Personal & Refined EMC Links
- Automation interface blocks allowing hoppers, pipes, and transport networks to interact directly with player Transmutation pools.

### 5. Transmutation Table Search & Query Filters
- **`@<mod>`**: Filter items by Mod ID or Display Name (e.g. `@projectex`, `@minecraft`, `@thermalfoundation`, `@"IndustrialCraft 2"`).
- **`#<range>`**: Search EMC ranges with SI unit scaling (e.g. `#1000-50000`, `#1k-50k`, `#10M-5G`, `#100B-1T`, `#>1M`, `#<10k`, `#500k+`).
- **`#aff` / `#affordable`**: Filter items immediately affordable with the current Transmutation balance.
- **`$<ore>`**: Filter by OreDictionary tags (e.g. `$ingot`, `$ore`, `$dust`, `$gem`, `$logWood`).
- **`%<tooltip>`**: Search text within item tooltips (e.g. `%energy`, `%radiation`, `%durability`, `%tier`).
- **`!<negation>`**: Negate and exclude items matching any filter (e.g. `!@minecraft`, `!ingot`, `!#100k+`).
- **`^fuel` / `^matter`**: Filter by EMC fuel or matter category.
- **`id:` / `*`**: Filter items by registry name or unlocalized ID (e.g. `id:item_matter`, `*matter`).
- **Compound Logic**: Space for AND, `|` for OR, and quotes for multi-word phrases.
- **Search History**: Use Up and Down arrow keys to navigate recent search queries.
- **Hover Syntax Guide**: Hover over the search box to view a syntax reference tooltip.

### 6. Applied Energistics 2 (AE2) Integration (Optional Soft Dependency)
- **ME EMC Link**:
  - Connects your AE2 network directly to your personal Transmutation Table pool (consumes 1 channel).
  - Learned items are exposed to the ME storage network with live synthetic counts.
  - Deposited items with EMC are automatically absorbed into the player's balance and learned.
  - GUI configuration: Read/Write, Read-Only, Write-Only modes, priority adjustments (-128 to +128), and a 16-slot filter grid.
  - **Match Precision Modes**: Toggle between **Exact** (item, metadata, NBT), **Fuzzy** (ignores damage/NBT for tools and armor), and **OreDict** (matches cross-mod equivalents).
  - Shift + Right-Click to claim/bind in-world with audio and chat confirmation.
- **ME Transmutation Storage Cell**:
  - 64-bit storage cell insertable into standard ME Drives to expose your personal Transmutation balance.
- **Zero-Crash Isolation**: All AE2 features are soft-loaded; if AE2 is not installed, the mod operates seamlessly in standalone mode without loading AE2 classes.

### 7. WAILA In-World HUD Diagnostics
- In-world tooltips showing owner, generation rates, buffered EMC, and ME EMC Link configuration (mode, priority, and filter precision).

### 8. Performance & NEI Optimization
- Tooltips utilize zero-allocation O(1) item checks with dynamic SI unit scaling by default and exact digit formatting on Shift.
- High FPS retained even when querying thousands of items simultaneously in NEI.

---

## Requirements

- **Minecraft**: 1.7.10
- **Forge**: 10.13.4.1614 or newer
- **ProjectE**: 1.7.10-PE1.10.1 or newer
- **Applied Energistics 2** *(Optional)*: rv3-beta-6 or compatible
- **WAILA** *(Optional)*: 1.5.10 or compatible

---

## Building from Source

```bash
git clone https://github.com/YatzCore/FTB-ProjectEX-1.7.10.git
cd FTB-ProjectEX-1.7.10
./gradlew.bat build --no-daemon
```

The output binary `FTB-ProjectEX-1.7.10-1.5.jar` will be generated in `build/libs/`.

---

## Automated Test Suite

Run unit tests via:

```bash
./gradlew.bat test --no-daemon
```

---

## Credits

- Original 1.12.2 mod by **LatvianModder** and the **FTB Team**.
- Base mod ProjectE by **MozeIntel**, **sinkillerj**, **williewillus**, and contributors.
- 1.7.10 Backport, 64-Bit Engine & Extended Features by **YatzCore**.
