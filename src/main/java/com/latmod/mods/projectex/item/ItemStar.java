package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.StarTier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemStar extends ItemPE implements IItemEmc {
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemStar() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(ProjectEX.TAB);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack != null ? stack.getItemDamage() : 0;
        return "item.projectex.star_" + StarTier.byMeta(meta).getName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < StarTier.VALUES.length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[StarTier.VALUES.length];
        for (int i = 0; i < StarTier.VALUES.length; i++) {
            icons[i] = register.registerIcon("projecte:stars/klein_star_" + (i + 1));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        if (damage < 0 || damage >= StarTier.VALUES.length) {
            return icons[0];
        }
        return icons[damage];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(StatCollector.translateToLocal("projecte.emc.stored") + " " + String.format("%,.0f", getStoredEmc(stack)) + " / " + String.format("%,.0f", getMaximumEmc(stack)));
    }

    // IItemEmc Implementation
    @Override
    public double getStoredEmc(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return 0.0;
        }
        return stack.getTagCompound().getDouble("StoredEMC");
    }

    public void setStoredEmc(ItemStack stack, double amount) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setDouble("StoredEMC", Math.min(amount, getMaximumEmc(stack)));
    }

    @Override
    public double addEmc(ItemStack stack, double amount) {
        double current = getStoredEmc(stack);
        double max = getMaximumEmc(stack);
        double toAdd = Math.min(amount, max - current);
        setStoredEmc(stack, current + toAdd);
        return toAdd;
    }

    public long addEmc(ItemStack stack, long amount) {
        return (long) Math.min((double) Long.MAX_VALUE, addEmc(stack, (double) amount));
    }

    @Override
    public double extractEmc(ItemStack stack, double amount) {
        double current = getStoredEmc(stack);
        double toRemove = Math.min(amount, current);
        setStoredEmc(stack, current - toRemove);
        return toRemove;
    }

    public long extractEmc(ItemStack stack, long amount) {
        return (long) Math.min((double) Long.MAX_VALUE, extractEmc(stack, (double) amount));
    }

    @Override
    public double getMaximumEmc(ItemStack stack) {
        int meta = stack != null ? stack.getItemDamage() : 0;
        return StarTier.byMeta(meta).getMagnumMaxEmc() / 100.0;
    }
}
