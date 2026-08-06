package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.tile.TilePowerFlower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockPowerFlower extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] collectorIcons;
    @SideOnly(Side.CLIENT)
    private IIcon[] relayIcons;

    public BlockPowerFlower() {
        super(Material.iron);
        setHardness(10.0f);
        setResistance(100.0f);
        setStepSound(soundTypeMetal);
        setCreativeTab(ProjectEX.TAB);
        setBlockName("projectex.power_flower");
        setBlockBounds(0.125f, 0.0f, 0.125f, 0.875f, 0.75f, 0.875f);
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
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TilePowerFlower();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TilePowerFlower) {
                ((TilePowerFlower) te).setOwner((EntityPlayer) placer);
            }
        }
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        collectorIcons = new IIcon[EnumTier.VALUES.length];
        relayIcons = new IIcon[EnumTier.VALUES.length];
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            collectorIcons[i] = register.registerIcon("projectex:collectors/" + EnumTier.VALUES[i].getName());
            relayIcons[i] = register.registerIcon("projectex:relays/" + EnumTier.VALUES[i].getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (meta < 0 || meta >= EnumTier.VALUES.length) {
            meta = 0;
        }
        if (side == 1) { // Top
            return collectorIcons[meta];
        }
        return relayIcons[meta]; // Bottom and sides
    }
}
