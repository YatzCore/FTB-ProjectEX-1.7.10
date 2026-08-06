package com.latmod.mods.projectex.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotTableInput extends Slot {
    public SlotTableInput(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
