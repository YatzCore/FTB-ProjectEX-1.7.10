package com.latmod.mods.projectex;

import com.latmod.mods.projectex.block.ProjectEXBlocks;
import com.latmod.mods.projectex.item.ProjectEXItems;
import com.latmod.mods.projectex.tile.*;
import cpw.mods.fml.common.registry.GameRegistry;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ProjectEXEventHandler {

    public static void init() {
        // Tile Entity Registrations
        GameRegistry.registerTileEntity(TileCollector.class, "projectex:collector");
        GameRegistry.registerTileEntity(TileRelay.class, "projectex:relay");
        GameRegistry.registerTileEntity(TilePowerFlower.class, "projectex:power_flower");
        GameRegistry.registerTileEntity(TileLink.class, "projectex:link");
        GameRegistry.registerTileEntity(TileStoneTable.class, "projectex:stone_table");

        // Initialize default Alchemy Table / Stone Table recipes
        AlchemyTableRecipes.INSTANCE.addDefaultRecipes();

        // Crafting Recipes
        addRecipes();
    }

    private static void addRecipes() {
        ItemStack aetalisFuel = new ItemStack(ObjHandler.fuels, 1, 2); // Aeternalis Fuel

        // 1. Official FTB-ProjectEX 1.12.2 Recipes for Magenta Matter (from Red Matter)
        // Horizontal Layout: A A A / M M M / A A A
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.MATTER, 1, 0),
            "AAA",
            "MMM",
            "AAA",
            'A', aetalisFuel,
            'M', new ItemStack(ObjHandler.matter, 1, 1)
        ));

        // Vertical Layout: A M A / A M A / A M A
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.MATTER, 1, 0),
            "AMA",
            "AMA",
            "AMA",
            'A', aetalisFuel,
            'M', new ItemStack(ObjHandler.matter, 1, 1)
        ));

        // 2. Official Matter Progression (Magenta -> Pink -> ... -> Fading)
        for (int i = 0; i < EnumMatter.VALUES.length - 1; i++) {
            ItemStack currentMatter = new ItemStack(ProjectEXItems.MATTER, 1, i);
            ItemStack nextMatter = new ItemStack(ProjectEXItems.MATTER, 1, i + 1);

            // Horizontal Recipe: A A A / M M M / A A A
            GameRegistry.addRecipe(new ShapedOreRecipe(
                nextMatter.copy(),
                "AAA",
                "MMM",
                "AAA",
                'A', aetalisFuel,
                'M', currentMatter
            ));

            // Vertical Recipe: A M A / A M A / A M A
            GameRegistry.addRecipe(new ShapedOreRecipe(
                nextMatter.copy(),
                "AMA",
                "AMA",
                "AMA",
                'A', aetalisFuel,
                'M', currentMatter
            ));

            // Reverse: 1 higher matter -> 3 lower matter (official ratio)
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXItems.MATTER, 3, i),
                nextMatter.copy()
            ));
        }

        // 3. Official Magnum Star Recipes (4x previous star shapeless)
        // Magnum Star Ein = 4x Klein Star Omega (data 5)
        GameRegistry.addRecipe(new ShapelessOreRecipe(
            new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 0),
            new ItemStack(ObjHandler.kleinStars, 1, 5),
            new ItemStack(ObjHandler.kleinStars, 1, 5),
            new ItemStack(ObjHandler.kleinStars, 1, 5),
            new ItemStack(ObjHandler.kleinStars, 1, 5)
        ));

        for (int i = 1; i < StarTier.VALUES.length; i++) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i),
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i - 1)
            ));
        }

        // 4. Official Colossal Star Recipes (4x previous star shapeless)
        // Colossal Star Ein = 4x Magnum Star Omega (data 5)
        GameRegistry.addRecipe(new ShapelessOreRecipe(
            new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, 0),
            new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 5),
            new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 5),
            new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 5),
            new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 5)
        ));

        for (int i = 1; i < StarTier.VALUES.length; i++) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i),
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i - 1),
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i - 1)
            ));
        }

        // 5. Official Final Star Shard (Advanced Star Shard) Recipe: 8x Colossal Star Omega + 1 Nether Star
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD, 1),
            "SSS",
            "SNS",
            "SSS",
            'S', new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, 5),
            'N', Items.nether_star
        ));

        // 6. Official Knowledge Sharing Book Recipe: 4 Violet Matter + 4 Nether Star + 1 Writable Book
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.KNOWLEDGE_SHARING_BOOK),
            "RNR",
            "NBN",
            "RNR",
            'R', new ItemStack(ProjectEXItems.MATTER, 1, 3), // Violet Matter
            'N', Items.nether_star,
            'B', Items.writable_book
        ));

        // 7. Official Stone Table Recipe: 8 Stone Bricks + 1 Transmutation Table / Philosopher's Stone
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.STONE_TABLE, 1, 0),
            "SSS",
            "STS",
            "SSS",
            'S', Blocks.stonebrick,
            'T', ObjHandler.transmuteStone
        ));
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.STONE_TABLE, 1, 0),
            "SSS",
            "STS",
            "SSS",
            'S', Blocks.stonebrick,
            'T', ObjHandler.philosStone
        ));

        // 8. Official Links Recipes
        // Personal Link: 4 Dark Matter + 4 Red Matter Blocks + 1 Condenser MK2
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.PERSONAL_LINK),
            "RBR",
            "BCB",
            "RBR",
            'R', new ItemStack(ObjHandler.matterBlock, 1, 1), // Red Matter Block
            'B', new ItemStack(ObjHandler.matter, 1, 0),      // Dark Matter
            'C', ObjHandler.condenserMk2
        ));

        // Refined Link: 9x Personal Link
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.REFINED_LINK),
            "LLL",
            "LLL",
            "LLL",
            'L', ProjectEXBlocks.PERSONAL_LINK
        ));

        // Compressed Refined Link: 6x Refined Link
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.COMPRESSED_REFINED_LINK),
            "LLL",
            "LLL",
            "LLL",
            'L', ProjectEXBlocks.REFINED_LINK
        ));

        // 9. Official Collector & Relay Progression (16 Tiers: 0..15)
        // Conversions from ProjectE MK1, MK2, MK3
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.COLLECTOR, 1, 0), ObjHandler.energyCollector));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.COLLECTOR, 1, 1), ObjHandler.collectorMK2));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.COLLECTOR, 1, 2), ObjHandler.collectorMK3));

        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.RELAY, 1, 0), ObjHandler.relay));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.RELAY, 1, 1), ObjHandler.relayMK2));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ProjectEXBlocks.RELAY, 1, 2), ObjHandler.relayMK3));

        // Shapeless progression: 1x prev Collector/Relay + 1x Tier Matter/Shard
        for (int i = 1; i < EnumTier.VALUES.length; i++) {
            ItemStack matterStack;
            if (i == 1) matterStack = new ItemStack(ObjHandler.matter, 1, 0); // Dark
            else if (i == 2) matterStack = new ItemStack(ObjHandler.matter, 1, 1); // Red
            else if (i < 15) matterStack = new ItemStack(ProjectEXItems.MATTER, 1, i - 3); // Magenta..Fading
            else matterStack = new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD); // Final

            // Collector
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i),
                new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i - 1),
                matterStack
            ));

            // Relay
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXBlocks.RELAY, 1, i),
                new ItemStack(ProjectEXBlocks.RELAY, 1, i - 1),
                matterStack
            ));
        }

        // 10. Official Compressed Collector & Power Flower Recipes (16 Tiers: 0..15)
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            // Compressed Collector: 9x Collector of tier i
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXItems.COMPRESSED_COLLECTOR, 1, i),
                "CCC",
                "CCC",
                "CCC",
                'C', new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i)
            ));

            // Power Flower: 2x Compressed Collector + 6x Relay + 1x Personal Link
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXBlocks.POWER_FLOWER, 1, i),
                "CLC",
                "RRR",
                "RRR",
                'C', new ItemStack(ProjectEXItems.COMPRESSED_COLLECTOR, 1, i),
                'L', ProjectEXBlocks.PERSONAL_LINK,
                'R', new ItemStack(ProjectEXBlocks.RELAY, 1, i)
            ));
        }

        // 11. Official Final Star Recipe: 8x Final Power Flower (Tier 15) + 1 Dragon Egg
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.FINAL_STAR),
            "SSS",
            "SES",
            "SSS",
            'S', new ItemStack(ProjectEXBlocks.POWER_FLOWER, 1, 15),
            'E', Blocks.dragon_egg
        ));
    }
}
