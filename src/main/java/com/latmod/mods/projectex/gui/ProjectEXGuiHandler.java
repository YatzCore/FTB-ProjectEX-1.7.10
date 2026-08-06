package com.latmod.mods.projectex.gui;

import com.latmod.mods.projectex.tile.TileStoneTable;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ProjectEXGuiHandler implements IGuiHandler {
    public static final int STONE_TABLE = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (ID == STONE_TABLE && te instanceof TileStoneTable) {
            return new ContainerStoneTable(player.inventory, (TileStoneTable) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (ID == STONE_TABLE && te instanceof TileStoneTable) {
            return new GuiStoneTable(player.inventory, (TileStoneTable) te);
        }
        return null;
    }
}
