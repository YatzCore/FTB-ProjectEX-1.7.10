package com.latmod.mods.projectex.gui;

import com.latmod.mods.projectex.integration.ae2.AE2Integration;
import com.latmod.mods.projectex.tile.TileStoneTable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.IGuiHandler;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ProjectEXGuiHandler implements IGuiHandler {
    public static final int STONE_TABLE = 1;
    public static final int ME_EMC_LINK = 2;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        int baseId = ID & 0x0F;
        if (baseId == STONE_TABLE) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileStoneTable) {
                return new ContainerStoneTable(player.inventory, (TileStoneTable) te);
            }
        }

        if (Loader.isModLoaded("appliedenergistics2")) {
            Object ae2Gui = AE2Integration.getServerGuiElement(ID, player, world, x, y, z);
            if (ae2Gui != null) return ae2Gui;
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        int baseId = ID & 0x0F;
        if (baseId == STONE_TABLE) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileStoneTable) {
                return new GuiStoneTable(player.inventory, (TileStoneTable) te);
            }
        }

        if (Loader.isModLoaded("appliedenergistics2")) {
            Object ae2Gui = AE2Integration.getClientGuiElement(ID, player, world, x, y, z);
            if (ae2Gui != null) return ae2Gui;
        }

        return null;
    }
}
