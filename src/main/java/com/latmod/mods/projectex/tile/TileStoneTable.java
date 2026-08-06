package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.ProjectEXUtils;
import moze_intel.projecte.api.item.IItemEmc;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileStoneTable extends TileEntity implements IInventory {
    private ItemStack[] inventory = new ItemStack[3]; // 0 = Input, 1 = Output, 2 = Fuel
    public int progress = 0;
    public int maxProgress = 200;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && !worldObj.isRemote) {
            ItemStack input = inventory[0];
            ItemStack fuel = inventory[2];

            if (input != null) {
                AlchemyTableRecipes.AlchemyTableRecipe recipe = AlchemyTableRecipes.INSTANCE.findRecipe(input);
                if (recipe != null) {
                    // Check if output slot can accept recipe output
                    if (inventory[1] == null || (inventory[1].isItemEqual(recipe.output) && inventory[1].stackSize < inventory[1].getMaxStackSize())) {
                        // Check if fuel is IItemEmc and has enough EMC
                        if (fuel != null && fuel.getItem() instanceof IItemEmc) {
                            IItemEmc emcItem = (IItemEmc) fuel.getItem();
                            double emcPerTick = recipe.emcCost / maxProgress;
                            if (emcItem.getStoredEmc(fuel) >= emcPerTick) {
                                emcItem.extractEmc(fuel, emcPerTick);
                                progress++;
                                if (progress >= maxProgress) {
                                    progress = 0;
                                    decrStackSize(0, 1);
                                    if (inventory[1] == null) {
                                        inventory[1] = recipe.output.copy();
                                    } else {
                                        inventory[1].stackSize += recipe.output.stackSize;
                                    }
                                    markDirty();
                                }
                                return;
                            }
                        }
                    }
                }
            }
            if (progress > 0) {
                progress = 0;
                markDirty();
            }
        }
    }

    public void dropItems(World world, int x, int y, int z) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                ProjectEXUtils.dropItem(world, x, y, z, inventory[i]);
                inventory[i] = null;
            }
        }
    }

    // IInventory Implementation
    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= inventory.length) return null;
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (inventory[slot] != null) {
            ItemStack itemstack;
            if (inventory[slot].stackSize <= count) {
                itemstack = inventory[slot];
                inventory[slot] = null;
                markDirty();
                return itemstack;
            } else {
                itemstack = inventory[slot].splitStack(count);
                if (inventory[slot].stackSize == 0) {
                    inventory[slot] = null;
                }
                markDirty();
                return itemstack;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.length) {
            inventory[slot] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
            }
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.projectex.stone_table";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) return true;
        if (slot == 1) return false;
        if (slot == 2) return stack != null && stack.getItem() instanceof IItemEmc;
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        progress = tag.getInteger("Progress");
        NBTTagList list = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Progress", progress);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
    }
}
