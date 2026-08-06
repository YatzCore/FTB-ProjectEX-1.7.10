package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.ProjectEX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockStoneTable extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;

    public BlockStoneTable() {
        super(Material.rock);
        setHardness(1.5f);
        setResistance(10.0f);
        setStepSound(soundTypeStone);
        setCreativeTab(ProjectEX.TAB);
        setBlockName("projectex.stone_table");
        setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(moze_intel.projecte.PECore.instance, moze_intel.projecte.utils.Constants.TRANSMUTATION_GUI, world, x, y, z);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("projectex:stone_table_side");
        iconTop = register.registerIcon("projectex:stone_table_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) {
            return iconTop;
        }
        return iconSide;
    }
}
