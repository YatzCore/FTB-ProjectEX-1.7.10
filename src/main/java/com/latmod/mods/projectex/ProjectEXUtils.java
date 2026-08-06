package com.latmod.mods.projectex;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ProjectEXUtils {

    public static void dropItem(World world, double x, double y, double z, ItemStack stack) {
        if (world != null && !world.isRemote && stack != null && stack.stackSize > 0) {
            EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
            item.motionX = 0;
            item.motionY = 0;
            item.motionZ = 0;
            item.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(item);
        }
    }

    public static ItemStack fixOutput(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        ItemStack copy = stack.copy();
        if (copy.getItem().isDamageable() && !copy.getItem().getHasSubtypes()) {
            copy.setItemDamage(0);
        }
        return copy;
    }

    public static ItemStack insertStackIntoInventory(IInventory inv, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null || stack.stackSize <= 0) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        int sideOrdinal = side != null ? side.ordinal() : 0;

        if (inv instanceof ISidedInventory) {
            ISidedInventory sided = (ISidedInventory) inv;
            int[] slots = sided.getAccessibleSlotsFromSide(sideOrdinal);
            if (slots != null) {
                // First pass: try merging with existing stacks
                for (int slot : slots) {
                    ItemStack inSlot = sided.getStackInSlot(slot);
                    if (inSlot != null && inSlot.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(inSlot, remaining)) {
                        if (sided.canInsertItem(slot, remaining, sideOrdinal)) {
                            int max = Math.min(sided.getInventoryStackLimit(), inSlot.getMaxStackSize());
                            int space = max - inSlot.stackSize;
                            if (space > 0) {
                                int transfer = Math.min(space, remaining.stackSize);
                                inSlot.stackSize += transfer;
                                remaining.stackSize -= transfer;
                                sided.markDirty();
                                if (remaining.stackSize <= 0) {
                                    return null;
                                }
                            }
                        }
                    }
                }

                // Second pass: put into empty slots
                for (int slot : slots) {
                    ItemStack inSlot = sided.getStackInSlot(slot);
                    if (inSlot == null) {
                        if (sided.canInsertItem(slot, remaining, sideOrdinal)) {
                            int max = Math.min(sided.getInventoryStackLimit(), remaining.getMaxStackSize());
                            int transfer = Math.min(max, remaining.stackSize);
                            ItemStack newStack = remaining.copy();
                            newStack.stackSize = transfer;
                            sided.setInventorySlotContents(slot, newStack);
                            remaining.stackSize -= transfer;
                            sided.markDirty();
                            if (remaining.stackSize <= 0) {
                                return null;
                            }
                        }
                    }
                }
            }
        } else {
            // Standard IInventory
            int size = inv.getSizeInventory();

            // First pass: merge with existing stacks
            for (int i = 0; i < size; i++) {
                if (!inv.isItemValidForSlot(i, remaining)) continue;
                ItemStack inSlot = inv.getStackInSlot(i);
                if (inSlot != null && inSlot.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(inSlot, remaining)) {
                    int max = Math.min(inv.getInventoryStackLimit(), inSlot.getMaxStackSize());
                    int space = max - inSlot.stackSize;
                    if (space > 0) {
                        int transfer = Math.min(space, remaining.stackSize);
                        inSlot.stackSize += transfer;
                        remaining.stackSize -= transfer;
                        inv.markDirty();
                        if (remaining.stackSize <= 0) {
                            return null;
                        }
                    }
                }
            }

            // Second pass: empty slots
            for (int i = 0; i < size; i++) {
                if (!inv.isItemValidForSlot(i, remaining)) continue;
                ItemStack inSlot = inv.getStackInSlot(i);
                if (inSlot == null) {
                    int max = Math.min(inv.getInventoryStackLimit(), remaining.getMaxStackSize());
                    int transfer = Math.min(max, remaining.stackSize);
                    ItemStack newStack = remaining.copy();
                    newStack.stackSize = transfer;
                    inv.setInventorySlotContents(i, newStack);
                    remaining.stackSize -= transfer;
                    inv.markDirty();
                    if (remaining.stackSize <= 0) {
                        return null;
                    }
                }
            }
        }

        return remaining.stackSize > 0 ? remaining : null;
    }
}
