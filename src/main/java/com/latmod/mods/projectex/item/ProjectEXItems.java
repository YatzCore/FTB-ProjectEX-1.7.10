package com.latmod.mods.projectex.item;

import cpw.mods.fml.common.registry.GameRegistry;

public class ProjectEXItems {

    public static ItemMatter MATTER;
    public static ItemStar STAR;
    public static ItemMagnumStar MAGNUM_STAR;
    public static ItemColossalStar COLOSSAL_STAR;
    public static ItemFinalStar FINAL_STAR;
    public static ItemKnowledgeSharingBook KNOWLEDGE_SHARING_BOOK;
    public static ItemAdvancedStarShard ADVANCED_STAR_SHARD;
    public static ItemCompressedCollector COMPRESSED_COLLECTOR;

    public static void init() {
        MATTER = new ItemMatter();
        GameRegistry.registerItem(MATTER, "matter");

        STAR = new ItemStar();
        GameRegistry.registerItem(STAR, "star");

        MAGNUM_STAR = new ItemMagnumStar();
        GameRegistry.registerItem(MAGNUM_STAR, "magnum_star");

        COLOSSAL_STAR = new ItemColossalStar();
        GameRegistry.registerItem(COLOSSAL_STAR, "colossal_star");

        FINAL_STAR = new ItemFinalStar();
        GameRegistry.registerItem(FINAL_STAR, "final_star");

        KNOWLEDGE_SHARING_BOOK = new ItemKnowledgeSharingBook();
        GameRegistry.registerItem(KNOWLEDGE_SHARING_BOOK, "knowledge_sharing_book");

        ADVANCED_STAR_SHARD = new ItemAdvancedStarShard();
        GameRegistry.registerItem(ADVANCED_STAR_SHARD, "advanced_star_shard");

        COMPRESSED_COLLECTOR = new ItemCompressedCollector();
        GameRegistry.registerItem(COMPRESSED_COLLECTOR, "compressed_collector");
    }
}
