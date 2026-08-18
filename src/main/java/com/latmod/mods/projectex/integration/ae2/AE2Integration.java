package com.latmod.mods.projectex.integration.ae2;

import appeng.api.AEApi;
import com.latmod.mods.projectex.block.ProjectEXBlocks;
import com.latmod.mods.projectex.item.ProjectEXItems;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class AE2Integration {

    public static BlockMEEMCLink blockMEEMCLink;
    public static ItemMEEMCCell itemMEEMCCell;

    // Weak set of active tiles to avoid memory leaks
    private static final Set<TileMEEMCLink> ACTIVE_TILES = Collections.newSetFromMap(new WeakHashMap<TileMEEMCLink, Boolean>());

    public static void registerTile(TileMEEMCLink tile) {
        if (tile != null) {
            ACTIVE_TILES.add(tile);
        }
    }

    public static void unregisterTile(TileMEEMCLink tile) {
        if (tile != null) {
            ACTIVE_TILES.remove(tile);
        }
    }

    public static void notifyHandlersForPlayer(UUID uuid) {
        if (uuid == null) return;

        for (TileMEEMCLink tile : ACTIVE_TILES) {
            if (tile != null && uuid.equals(tile.getOwnerUUID())) {
                tile.notifyGrid();
            }
        }

        if (itemMEEMCCell != null) {
            itemMEEMCCell.notifyGridForPlayer(uuid);
        }
    }

    public static void preInit() {
        blockMEEMCLink = new BlockMEEMCLink();
        GameRegistry.registerBlock(blockMEEMCLink, "me_emc_link");
        GameRegistry.registerTileEntity(TileMEEMCLink.class, "projectex:tile_me_emc_link");

        itemMEEMCCell = new ItemMEEMCCell();
        GameRegistry.registerItem(itemMEEMCCell, "me_emc_cell");
    }

    public static void init() {
        try {
            if (AEApi.instance() != null && AEApi.instance().registries() != null) {
                if (AEApi.instance().registries().cell() != null && itemMEEMCCell != null) {
                    AEApi.instance().registries().cell().addCellHandler(itemMEEMCCell);
                }
            }

            addRecipes();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void registerPackets(SimpleNetworkWrapper net) {
        net.registerMessage(MessageMEEMCLink.Handler.class, MessageMEEMCLink.class, 1, Side.SERVER);
    }

    public static Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        int baseId = ID & 0x0F;
        TileEntity te = world.getTileEntity(x, y, z);
        if (baseId == 2 && te instanceof TileMEEMCLink) {
            return new ContainerMEEMCLink(player.inventory, (TileMEEMCLink) te);
        }
        return null;
    }

    public static Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        int baseId = ID & 0x0F;
        TileEntity te = world.getTileEntity(x, y, z);
        if (baseId == 2 && te instanceof TileMEEMCLink) {
            return new GuiMEEMCLink(player.inventory, (TileMEEMCLink) te);
        }
        return null;
    }

    private static void addRecipes() {
        if (AEApi.instance() == null || AEApi.instance().definitions() == null) return;

        ItemStack emptyHousing = AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).orNull();
        ItemStack ifaceBlock = AEApi.instance().definitions().blocks().iface().maybeStack(1).orNull();

        ItemStack redMatter = new ItemStack(ObjHandler.matter, 1, 1);
        ItemStack magnumStarEin = new ItemStack(ProjectEXItems.MAGNUM_STAR, 1, 0);
        ItemStack personalLink = new ItemStack(ProjectEXBlocks.PERSONAL_LINK);

        // 1. ME Transmutation Storage Cell: Housing + Personal Link + 4 Red Matter + Magnum Star Ein
        if (emptyHousing != null) {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(itemMEEMCCell),
                "RMR",
                "HLH",
                "RMR",
                'H', emptyHousing,
                'L', personalLink,
                'R', redMatter,
                'M', magnumStarEin
            ));
        }

        // 2. ME EMC Link Block: ME Interface + Personal Link + 4 Red Matter
        if (ifaceBlock != null) {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(blockMEEMCLink),
                "RIR",
                "ILI",
                "RIR",
                'I', ifaceBlock,
                'L', personalLink,
                'R', redMatter
            ));
        }
    }
}
