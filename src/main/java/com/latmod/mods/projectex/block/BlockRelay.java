package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.tile.TileRelay;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockRelay extends Block {
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockRelay() {
        super(Material.iron);
        setHardness(10.0f);
        setResistance(100.0f);
        setStepSound(soundTypeMetal);
        setCreativeTab(ProjectEX.TAB);
        setBlockName("projectex.relay");
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileRelay();
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
        icons = new IIcon[EnumTier.VALUES.length];
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            icons[i] = register.registerIcon("projectex:relays/" + EnumTier.VALUES[i].getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (meta < 0 || meta >= EnumTier.VALUES.length) {
            return icons[0];
        }
        return icons[meta];
    }
}
