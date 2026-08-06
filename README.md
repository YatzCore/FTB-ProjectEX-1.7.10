# FTB Project EX (Minecraft 1.7.10 Backport)

A high-performance 1.7.10 backport of **Project EX**, the premier endgame expansion for **ProjectE** originally created by *LatvianModder* and the *FTB Team*. 

Designed to eliminate TPS overhead while unlocking virtually unlimited EMC production, automated extraction pipelines, and massive energy storage.

---

## Features

### EMC Automation & Logistics
* **Basic Energy EMC Link:** Ingests raw EMC from external Collectors and injects it directly into the player's Personal EMC network.
* **Personal EMC Link:** Seamlessly handles multi-directional item import and export bound to the player's EMC grid.
* **Refined EMC Link:** Features 1 input and 9 dedicated output slots engineered for high-throughput logistics integration (Refined Storage/AE2 style). Automatically learns items from input slots.
* **Compressed Refined EMC Link:** Expanded 54-slot extraction matrix built for extreme endgame automation.

### TPS-Optimized Power Generation & Transfer
* **Matter Collectors (16 Tiers):** From MK1 up to The Final Collector. Generates EMC on a 1-second pulse cycle, drastically reducing tick lag compared to legacy collector arrays.
* **Matter Relays (16 Tiers):** High-speed EMC transfer networks featuring tier-scaled transfer rates and bonuses.
* **Power Flower Bonsai Pots (16 Tiers):** Compact, single-block bonsai power flowers that feed massive passive EMC directly into your personal network without complex multi-block Lag-Causing setups.

### Transmutation & Matrix Access
* **Stone Transmutation Table:** A sleek 4-pixel high stone slab providing full in-world access to ProjectE's Transmutation matrix.

### High-Tier Items & Endgame Progression
* **Klein Stars:** Standard Tiers Ein to Omega.
* **Magnum Stars:** Massive EMC capacity storage devices (Tiers Ein to Omega).
* **Colossal Stars:** Extreme endgame EMC capacitors storing up to trillions of EMC points.
* **The Final Star & Shard:** The ultimate endgame artifact—enables infinite item duplication when paired with pedestal setups.
* **Knowledge Sharing Book:** Bind and synchronize learned transmutation knowledge across players.
* **Matter Tiers (12 New Alloys):** Magenta, Pink, Purple, Violet, Blue, Cyan, Green, Lime, Yellow, Orange, White, and Fading Matter.

---

## Requirements

* **Minecraft:** 1.7.10
* **Minecraft Forge:** 10.13.4.1614 or newer
* **ProjectE:** 1.7.10-PE1.10.1 or newer

---

## Building from Source

This project compiles with **RetroFuturaGradle**:

```bash
git clone https://github.com/YatzCore/FTB-ProjectEX-1.7.10.git
cd FTB-ProjectEX-1.7.10
./gradlew build
```

The production artifact will be generated at `build/libs/FTB-ProjectEX-1.7.10-1.0.0-1.7.10.jar`.

---

## Credits

* **Project EX (Original 1.12.2 Mod):** [LatvianModder](https://github.com/LatvianModder) & the FTB Team.
* **ProjectE Core Engine:** MozeIntel, sinkillerj, williewillus, and the ProjectE development team.
