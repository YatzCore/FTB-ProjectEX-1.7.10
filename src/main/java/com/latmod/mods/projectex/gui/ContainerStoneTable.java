package com.latmod.mods.projectex.gui;

import com.latmod.mods.projectex.tile.TileStoneTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerStoneTable extends Container {
    private final TileStoneTable tile;
    private int lastProgress = -1;

    public ContainerStoneTable(InventoryPlayer playerInv, TileStoneTable tile) {
        this.tile = tile;

        // Table slots
        addSlotToContainer(new SlotTableInput(tile, 0, 56, 35));   // Input
        addSlotToContainer(new SlotTableOutput(tile, 1, 116, 35)); // Output
        addSlotToContainer(new SlotTableInput(tile, 2, 86, 57));   // Fuel/Klein Star

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar
        for (int k = 0; k < 9; ++k) {
            addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < crafters.size(); ++i) {
            ICrafting crafter = (ICrafting) crafters.get(i);
            if (lastProgress != tile.progress) {
                crafter.sendProgressBarUpdate(this, 0, tile.progress);
            }
        }
        lastProgress = tile.progress;
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            tile.progress = data;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 3) {
                // Table to player inventory
                if (!mergeItemStack(itemstack1, 3, 39, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                // Player inventory to table
                if (!mergeItemStack(itemstack1, 0, 1, false) && !mergeItemStack(itemstack1, 2, 3, false)) {
                    return null;
                }
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
