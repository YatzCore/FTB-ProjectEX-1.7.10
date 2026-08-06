package com.latmod.mods.projectex;

import com.latmod.mods.projectex.block.ProjectEXBlocks;
import com.latmod.mods.projectex.item.ProjectEXItems;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.ItemStack;

public class ProjectEXEMCRegistration {

    private static void register(ItemStack stack, long emc) {
        if (stack != null && stack.getItem() != null) {
            int emcInt = (int) Math.min(emc, Integer.MAX_VALUE);
            try {
                ProjectEAPI.getEMCProxy().registerCustomEMC(stack, emcInt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void registerEMCValues() {
        // 1. Matters (Meta 0..11)
        long[] matterEmcs = {
            1866240L,      // Magenta (0)
            7464960L,      // Pink (1)
            29859840L,     // Purple (2)
            119439360L,    // Violet (3)
            477757440L,    // Blue (4)
            1911029760L,   // Cyan (5)
            7644119040L,   // Green (6)
            30576476160L,  // Lime (7)
            122305904640L, // Yellow (8)
            489223618560L, // Orange (9)
            1956894474240L,// White (10)
            7827577896960L // Fading (11)
        };

        for (int i = 0; i < matterEmcs.length; i++) {
            register(new ItemStack(ProjectEXItems.MATTER, 1, i), matterEmcs[i]);
        }

        // 2. Advanced Star Shard
        long starShardEmc = 7604928L;
        register(new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD), starShardEmc);

        // 3. Knowledge Sharing Book
        register(new ItemStack(ProjectEXItems.KNOWLEDGE_SHARING_BOOK), 139968L);

        // 4. Stone Table
        register(new ItemStack(ProjectEXBlocks.STONE_TABLE, 1, 0), 139973L);

        // 5. Links
        register(new ItemStack(ProjectEXBlocks.PERSONAL_LINK), 994752L);
        register(new ItemStack(ProjectEXBlocks.REFINED_LINK), 8459712L);
        register(new ItemStack(ProjectEXBlocks.COMPRESSED_REFINED_LINK), 38319552L);

        // 6. Collectors (16 Tiers: 0..15)
        long[] collectorEmcs = {
            544064L,       // Basic (0)
            1507328L,      // Dark (1)
            5508608L,      // Red (2)
            19173568L,     // Magenta (3)
            70233408L,     // Pink (4)
            271272768L,    // Purple (5)
            1068561920L,   // Violet (6)
            4253163520L,   // Blue (7)
            16986968064L,  // Cyan (8)
            67897217024L,  // Green (9)
            271488212992L, // Lime (10)
            1085752201216L,// Yellow (11)
            4342808158208L,// Orange (12)
            17370831986688L,// White (13)
            69482527293440L,// Fading (14)
            277928708521984L// Final (15)
        };

        for (int i = 0; i < collectorEmcs.length; i++) {
            register(new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i), collectorEmcs[i]);
        }

        // 7. Relays (16 Tiers: 0..15)
        long[] relayEmcs = {
            527680L,       // Basic (0)
            1445760L,      // Dark (1)
            5361152L,      // Red (2)
            19026112L,     // Magenta (3)
            70085952L,     // Pink (4)
            271125312L,    // Purple (5)
            1068414464L,   // Violet (6)
            4253016064L,   // Blue (7)
            16986820608L,  // Cyan (8)
            67897069568L,  // Green (9)
            271488065536L, // Lime (10)
            1085752053760L,// Yellow (11)
            4342808010752L,// Orange (12)
            17370831839232L,// White (13)
            69482527145984L,// Fading (14)
            277928708374528L// Final (15)
        };

        for (int i = 0; i < relayEmcs.length; i++) {
            register(new ItemStack(ProjectEXBlocks.RELAY, 1, i), relayEmcs[i]);
        }

        // 8. Power Flowers (16 Tiers: 0..15)
        for (int i = 0; i < 16; i++) {
            long pfEmc = (4 * collectorEmcs[i]) + relayEmcs[i];
            register(new ItemStack(ProjectEXBlocks.POWER_FLOWER, 1, i), pfEmc);
        }

        // 9. Magnum Stars (6 Tiers: 0..5)
        long[] magnumStarEmcs = {
            7804928L,      // Ein (0)
            8404928L,      // Zwei (1)
            10804928L,     // Drei (2)
            20404928L,     // Vier (3)
            58804928L,     // Sphere (4)
            212404928L     // Omega (5)
        };

        for (int i = 0; i < magnumStarEmcs.length; i++) {
            register(new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i), magnumStarEmcs[i]);
        }

        // 10. Colossal Stars (6 Tiers: 0..5)
        long[] colossalStarEmcs = {
            38824640L,     // Ein (0)
            41224640L,     // Zwei (1)
            50824640L,     // Drei (2)
            89224640L,     // Vier (3)
            242824640L,    // Sphere (4)
            857224640L     // Omega (5)
        };

        for (int i = 0; i < colossalStarEmcs.length; i++) {
            register(new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i), colossalStarEmcs[i]);
        }

        // 11. Final Star
        register(new ItemStack(ProjectEXItems.FINAL_STAR), 2147483647L);
    }
}
