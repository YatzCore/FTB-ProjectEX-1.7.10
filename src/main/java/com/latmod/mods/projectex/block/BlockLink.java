package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.tile.TileLink;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockLink extends Block {
    private final int tier; // 0 = Personal, 1 = Refined, 2 = Compressed
    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public BlockLink(int tier, String name) {
        super(Material.iron);
        this.tier = tier;
        setHardness(10.0f);
        setResistance(100.0f);
        setStepSound(soundTypeMetal);
        setCreativeTab(ProjectEX.TAB);
        setBlockName("projectex." + name);
    }

    public int getTier() {
        return tier;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileLink(tier);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileLink) {
                ((TileLink) te).setOwner((EntityPlayer) placer);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(ProjectEX.INSTANCE, 2, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileLink) {
            ((TileLink) te).dropItems(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        String iconName = "personal_link";
        if (tier == 1) iconName = "refined_link";
        if (tier == 2) iconName = "compressed_refined_link";
        icon = register.registerIcon("projectex:" + iconName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
