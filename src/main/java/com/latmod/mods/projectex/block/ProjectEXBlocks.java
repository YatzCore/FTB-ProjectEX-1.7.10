package com.latmod.mods.projectex.block;

import cpw.mods.fml.common.registry.GameRegistry;

public class ProjectEXBlocks {

    public static BlockCollector COLLECTOR;
    public static BlockRelay RELAY;
    public static BlockPowerFlower POWER_FLOWER;
    public static BlockLink PERSONAL_LINK;
    public static BlockLink REFINED_LINK;
    public static BlockLink COMPRESSED_REFINED_LINK;
    public static BlockStoneTable STONE_TABLE;

    public static void init() {
        COLLECTOR = new BlockCollector();
        GameRegistry.registerBlock(COLLECTOR, ItemBlockMatter.class, "collector");

        RELAY = new BlockRelay();
        GameRegistry.registerBlock(RELAY, ItemBlockMatter.class, "relay");

        POWER_FLOWER = new BlockPowerFlower();
        GameRegistry.registerBlock(POWER_FLOWER, ItemBlockMatter.class, "power_flower");

        PERSONAL_LINK = new BlockLink(0, "personal_link");
        GameRegistry.registerBlock(PERSONAL_LINK, "personal_link");

        REFINED_LINK = new BlockLink(1, "refined_link");
        GameRegistry.registerBlock(REFINED_LINK, "refined_link");

        COMPRESSED_REFINED_LINK = new BlockLink(2, "compressed_refined_link");
        GameRegistry.registerBlock(COMPRESSED_REFINED_LINK, "compressed_refined_link");

        STONE_TABLE = new BlockStoneTable();
        GameRegistry.registerBlock(STONE_TABLE, "stone_table");
    }
}
