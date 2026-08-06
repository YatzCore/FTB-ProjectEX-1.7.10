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

            // Official FTB-ProjectEX 1.12.2 Horizontal Recipe: A A A / M M M / A A A
            GameRegistry.addRecipe(new ShapedOreRecipe(
                nextMatter.copy(),
                "AAA",
                "MMM",
                "AAA",
                'A', aetalisFuel,
                'M', currentMatter
            ));

            // Official FTB-ProjectEX 1.12.2 Vertical Recipe: A M A / A M A / A M A
            GameRegistry.addRecipe(new ShapedOreRecipe(
                nextMatter.copy(),
                "AMA",
                "AMA",
                "AMA",
                'A', aetalisFuel,
                'M', currentMatter
            ));

            // Reverse: 1 higher matter -> 4 lower matter
            GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(ProjectEXItems.MATTER, 4, i),
                nextMatter.copy()
            ));
        }

        // Advanced Star Shard: Nether Star + 4 Magenta Matter
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD, 1),
            " M ",
            "MSM",
            " M ",
            'S', Items.nether_star,
            'M', new ItemStack(ProjectEXItems.MATTER, 1, 0)
        ));

        // Knowledge Sharing Book: Book + Dark Matter
        GameRegistry.addRecipe(new ShapelessOreRecipe(
            new ItemStack(ProjectEXItems.KNOWLEDGE_SHARING_BOOK),
            Items.book,
            new ItemStack(ObjHandler.matter, 1, 0)
        ));

        // Stone Table: Smooth stone + Dark Matter
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.STONE_TABLE, 1, 0),
            "SSS",
            " M ",
            " S ",
            'S', Blocks.stone,
            'M', new ItemStack(ObjHandler.matter, 1, 0)
        ));

        // Links: Dark Matter + Transmutation Tablet + Diamond Block
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.PERSONAL_LINK),
            "DMD",
            "MTM",
            "DMD",
            'D', Blocks.diamond_block,
            'M', new ItemStack(ObjHandler.matter, 1, 0),
            'T', ObjHandler.transmutationTablet
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.REFINED_LINK),
            " R ",
            "RLR",
            " R ",
            'L', ProjectEXBlocks.PERSONAL_LINK,
            'R', new ItemStack(ProjectEXItems.MATTER, 1, 0)
        ));

        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXBlocks.COMPRESSED_REFINED_LINK),
            " R ",
            "RLR",
            " R ",
            'L', ProjectEXBlocks.REFINED_LINK,
            'R', new ItemStack(ProjectEXItems.MATTER, 1, 1)
        ));

        // Magnum Star & Colossal Star Recipes (6 Tiers: Ein, Zwei, Drei, Vier, Sphere, Omega)
        for (int i = 0; i < StarTier.VALUES.length; i++) {
            // Magnum Star: 4x Klein Star (or previous Magnum Star) + Advanced Star Shard
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i),
                " S ",
                "SAS",
                " S ",
                'S', new ItemStack(ObjHandler.kleinStars, 1, i),
                'A', ProjectEXItems.ADVANCED_STAR_SHARD
            ));

            // Colossal Star: 4x Magnum Star + Advanced Star Shard
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, i),
                " M ",
                "MSM",
                " M ",
                'M', new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, i),
                'S', ProjectEXItems.ADVANCED_STAR_SHARD
            ));
        }

        // 16 Tiers for Collectors, Relays, Power Flowers (Basic .. Final)
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            ItemStack matterStack;
            if (i == 0) matterStack = new ItemStack(Blocks.diamond_block);
            else if (i == 1) matterStack = new ItemStack(ObjHandler.matter, 1, 0); // Dark
            else if (i == 2) matterStack = new ItemStack(ObjHandler.matter, 1, 1); // Red
            else if (i < 15) matterStack = new ItemStack(ProjectEXItems.MATTER, 1, i - 3); // Magenta..Fading
            else matterStack = new ItemStack(ProjectEXItems.ADVANCED_STAR_SHARD); // Final

            // Collector Tier Progression
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i),
                "SMS",
                "MCM",
                "SMS",
                'S', new ItemStack(ObjHandler.kleinStars, 1, Math.min(i, 5)),
                'M', matterStack,
                'C', i == 0 ? ObjHandler.energyCollector : (i <= 2 ? ObjHandler.collectorMK2 : new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i - 1))
            ));

            // Relay Tier Progression
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXBlocks.RELAY, 1, i),
                "SMS",
                "MRM",
                "SMS",
                'S', new ItemStack(ObjHandler.kleinStars, 1, Math.min(i, 5)),
                'M', matterStack,
                'R', i == 0 ? ObjHandler.relay : (i <= 2 ? ObjHandler.relayMK2 : new ItemStack(ProjectEXBlocks.RELAY, 1, i - 1))
            ));

            // Power Flower: Collector + Relay of same tier
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ProjectEXBlocks.POWER_FLOWER, 1, i),
                " C ",
                "CRC",
                " C ",
                'C', new ItemStack(ProjectEXBlocks.COLLECTOR, 1, i),
                'R', new ItemStack(ProjectEXBlocks.RELAY, 1, i)
            ));
        }

        // Final Star: 4x Colossal Star Omega + Nether Star
        GameRegistry.addRecipe(new ShapedOreRecipe(
            new ItemStack(ProjectEXItems.FINAL_STAR),
            " C ",
            "CSC",
            " C ",
            'S', Items.nether_star,
            'C', new ItemStack(ProjectEXItems.COLOSSAL_STAR, 1, 5)
        ));
    }
}
