package com.latmod.mods.projectex;

import com.latmod.mods.projectex.block.ProjectEXBlocks;
import com.latmod.mods.projectex.item.ProjectEXItems;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ProjectEXEMCRegistration {

    private static Item itemPowerFlower;
    private static Item itemCollector;
    private static Item itemRelay;
    private static Item itemStoneTable;
    private static Item itemPersonalLink;
    private static Item itemRefinedLink;
    private static Item itemCompressedLink;

    public static void cacheItemReferences() {
        try {
            itemPowerFlower = Item.getItemFromBlock(ProjectEXBlocks.POWER_FLOWER);
            itemCollector = Item.getItemFromBlock(ProjectEXBlocks.COLLECTOR);
            itemRelay = Item.getItemFromBlock(ProjectEXBlocks.RELAY);
            itemStoneTable = Item.getItemFromBlock(ProjectEXBlocks.STONE_TABLE);
            itemPersonalLink = Item.getItemFromBlock(ProjectEXBlocks.PERSONAL_LINK);
            itemRefinedLink = Item.getItemFromBlock(ProjectEXBlocks.REFINED_LINK);
            itemCompressedLink = Item.getItemFromBlock(ProjectEXBlocks.COMPRESSED_REFINED_LINK);
        } catch (Throwable ignored) {}
    }

    public static double getProjectExEmc(ItemStack stack) {
        if (stack == null) return 0.0;
        Item item = stack.getItem();
        if (item == null) return 0.0;

        int meta = stack.getItemDamage();
        if (meta < 0) meta = 0;

        if (itemPowerFlower == null) {
            cacheItemReferences();
        }

        // Direct reference equality checks (zero allocation, instantaneous O(1))
        if (item == itemPowerFlower) {
            if (meta < ProjectEXConfig.powerFlowerEmcValue.length) {
                return (double) ProjectEXConfig.powerFlowerEmcValue[meta];
            }
            return 0.0;
        }
        if (item == itemCollector) {
            if (meta < ProjectEXConfig.collectorEmc.length) {
                return (double) ProjectEXConfig.collectorEmc[meta];
            }
            return 0.0;
        }
        if (item == itemRelay) {
            if (meta < ProjectEXConfig.relayEmc.length) {
                return (double) ProjectEXConfig.relayEmc[meta];
            }
            return 0.0;
        }
        if (item == ProjectEXItems.MATTER) {
            if (meta < ProjectEXConfig.matterEmc.length) {
                return (double) ProjectEXConfig.matterEmc[meta];
            }
            return 0.0;
        }
        if (item == ProjectEXItems.MAGNUM_STAR) {
            if (meta < ProjectEXConfig.magnumStarEmc.length) {
                return (double) ProjectEXConfig.magnumStarEmc[meta];
            }
            return 0.0;
        }
        if (item == ProjectEXItems.COLOSSAL_STAR) {
            if (meta < ProjectEXConfig.colossalStarEmc.length) {
                return (double) ProjectEXConfig.colossalStarEmc[meta];
            }
            return 0.0;
        }
        if (item == ProjectEXItems.ADVANCED_STAR_SHARD) {
            return (double) ProjectEXConfig.starShardEmc;
        }
        if (item == ProjectEXItems.KNOWLEDGE_SHARING_BOOK) {
            return (double) ProjectEXConfig.knowledgeBookEmc;
        }
        if (item == ProjectEXItems.FINAL_STAR) {
            return (double) ProjectEXConfig.finalStarEmc;
        }
        if (item == itemStoneTable) {
            return (double) ProjectEXConfig.stoneTableEmc;
        }
        if (item == itemPersonalLink) {
            return (double) ProjectEXConfig.personalLinkEmc;
        }
        if (item == itemRefinedLink) {
            return (double) ProjectEXConfig.refinedLinkEmc;
        }
        if (item == itemCompressedLink) {
            return (double) ProjectEXConfig.compressedRefinedLinkEmc;
        }

        return 0.0;
    }

    private static void register(ItemStack stack, long emc) {
        if (stack != null && stack.getItem() != null && emc > 0) {
            boolean registered = false;
            try {
                Object proxy = ProjectEAPI.getEMCProxy();
                if (proxy != null) {
                    for (java.lang.reflect.Method m : proxy.getClass().getMethods()) {
                        if (m.getName().equals("registerCustomEMC") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == ItemStack.class) {
                            Class<?> p2 = m.getParameterTypes()[1];
                            if (p2 == long.class) {
                                m.invoke(proxy, stack, emc);
                                registered = true;
                                break;
                            } else if (p2 == int.class) {
                                m.invoke(proxy, stack, (int) Math.min(emc, (long) Integer.MAX_VALUE));
                                registered = true;
                                break;
                            } else if (p2 == Number.class) {
                                m.invoke(proxy, stack, Long.valueOf(emc));
                                registered = true;
                                break;
                            } else if (p2 == double.class) {
                                m.invoke(proxy, stack, (double) emc);
                                registered = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
            if (!registered) {
                try {
                    ProjectEAPI.getEMCProxy().registerCustomEMC(stack, (int) Math.min(emc, Integer.MAX_VALUE));
                } catch (Throwable ignored) {}
            }
        }
    }

    public static void registerEMCValues() {
        cacheItemReferences();

        // 1. Matters (Meta 0..11: Magenta .. Fading)
        for (int i = 0; i < ProjectEXConfig.matterEmc.length; i++) {
            register(new ItemStack(ProjectEXItems.MATTER, 1, i), ProjectEXConfig.matterEmc[i]);
        }

        // 2. Final Star Shard (Advanced Star Shard)
        register(new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD), ProjectEXConfig.starShardEmc);

        // 3. Knowledge Sharing Book
        register(new ItemStack(ProjectEXItems.KNOWLEDGE_SHARING_BOOK), ProjectEXConfig.knowledgeBookEmc);

        // 4. Tables
        register(new ItemStack(ProjectEXBlocks.STONE_TABLE, 1, 0), ProjectEXConfig.stoneTableEmc);

        // 5. Links
        register(new ItemStack(ProjectEXBlocks.PERSONAL_LINK), ProjectEXConfig.personalLinkEmc);
        register(new ItemStack(ProjectEXBlocks.REFINED_LINK), ProjectEXConfig.refinedLinkEmc);
        register(new ItemStack(ProjectEXBlocks.COMPRESSED_REFINED_LINK), ProjectEXConfig.compressedRefinedLinkEmc);

        // 6. Collectors (16 Tiers: 0..15)
        for (int i = 0; i < ProjectEXConfig.collectorEmc.length; i++) {
            register(new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i), ProjectEXConfig.collectorEmc[i]);
        }

        // 7. Relays (16 Tiers: 0..15)
        for (int i = 0; i < ProjectEXConfig.relayEmc.length; i++) {
            register(new ItemStack(ProjectEXBlocks.RELAY, 1, i), ProjectEXConfig.relayEmc[i]);
        }

        // 8. Power Flowers (16 Tiers: 0..15)
        for (int i = 0; i < 16; i++) {
            register(new ItemStack(ProjectEXBlocks.POWER_FLOWER, 1, i), ProjectEXConfig.powerFlowerEmcValue[i]);
        }

        // 9. Magnum Stars (6 Tiers: 0..5)
        for (int i = 0; i < ProjectEXConfig.magnumStarEmc.length; i++) {
            register(new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i), ProjectEXConfig.magnumStarEmc[i]);
        }

        // 10. Colossal Stars (6 Tiers: 0..5)
        for (int i = 0; i < ProjectEXConfig.colossalStarEmc.length; i++) {
            register(new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i), ProjectEXConfig.colossalStarEmc[i]);
        }

        // 11. Final Star (Configurable: default 0 = craft only)
        if (ProjectEXConfig.finalStarEmc > 0) {
            register(new ItemStack(ProjectEXItems.FINAL_STAR), ProjectEXConfig.finalStarEmc);
        }

        ProjectEXUtils.sanitizeEmcMapper();
    }
}
