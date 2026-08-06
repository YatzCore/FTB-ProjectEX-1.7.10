package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.ProjectEXUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.gameObjs.items.ItemPE;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.List;

public class ItemFinalStar extends ItemPE implements IItemEmc, IPedestalItem {

    public ItemFinalStar() {
        setMaxStackSize(1);
        setCreativeTab(ProjectEX.TAB);
        setUnlocalizedName("projectex.final_star");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.projectex.final_star";
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.projectex.final_star.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("projectex:final_star");
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(StatCollector.translateToLocal("projecte.emc.stored") + " " + String.format("%,.0f", getStoredEmc(stack)) + " / Infinite");
        list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("item.projectex.final_star.tooltip"));
        list.add(EnumChatFormatting.GRAY + "Pedestal: Right-click Pedestal to ACTIVATE. Clones items on top into adjacent chest.");
    }

    @Override
    public void updateInPedestal(World world, int x, int y, int z) {
        if (world == null || world.isRemote) {
            return;
        }

        // Run every 20 ticks (1 second)
        if (world.getTotalWorldTime() % 20L == 0L) {
            // Expanded search AABB around top of pedestal
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x - 0.5, y, z - 0.5, x + 1.5, y + 2.5, z + 1.5);
            @SuppressWarnings("unchecked")
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, box);

            if (items != null && !items.isEmpty()) {
                for (EntityItem entityItem : items) {
                    if (entityItem != null && !entityItem.isDead && entityItem.getEntityItem() != null) {
                        ItemStack originalStack = entityItem.getEntityItem();
                        ItemStack copyToInsert = originalStack.copy();
                        copyToInsert.stackSize = copyToInsert.getMaxStackSize();

                        // Search 5 adjacent directions for container (North, South, East, West, Down)
                        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                            if (dir != ForgeDirection.UP) {
                                TileEntity tile = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
                                if (tile instanceof IInventory) {
                                    IInventory inv = (IInventory) tile;
                                    ItemStack result = ProjectEXUtils.insertStackIntoInventory(inv, copyToInsert, dir.getOpposite());
                                    if (result == null || result.stackSize < copyToInsert.stackSize) {
                                        // Item cloned and inserted into inventory!
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public List<String> getPedestalDescription() {
        return Collections.singletonList(StatCollector.translateToLocal("item.projectex.final_star.tooltip"));
    }

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

    @Override
    public double extractEmc(ItemStack stack, double amount) {
        double current = getStoredEmc(stack);
        double toRemove = Math.min(amount, current);
        setStoredEmc(stack, current - toRemove);
        return toRemove;
    }

    @Override
    public double getMaximumEmc(ItemStack stack) {
        return Double.MAX_VALUE;
    }
}
