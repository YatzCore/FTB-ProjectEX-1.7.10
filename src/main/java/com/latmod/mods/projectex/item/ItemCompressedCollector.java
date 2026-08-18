package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemCompressedCollector extends Item {
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemCompressedCollector() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(ProjectEX.TAB);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack != null ? stack.getItemDamage() : 0;
        return "tile.projectex.collector." + EnumTier.byMeta(meta).getName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack != null ? stack.getItemDamage() : 0;
        return StatCollector.translateToLocal("tile.projectex.collector." + EnumTier.byMeta(meta).getName() + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[EnumTier.VALUES.length];
        for (int i = 0; i < EnumTier.VALUES.length; i++) {
            icons[i] = register.registerIcon("projectex:collectors/" + EnumTier.VALUES[i].getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (damage < 0 || damage >= EnumTier.VALUES.length) {
            return icons[0];
        }
        return icons[damage];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(StatCollector.translateToLocal("tile.projectex.collector.compressed"));
    }
}
