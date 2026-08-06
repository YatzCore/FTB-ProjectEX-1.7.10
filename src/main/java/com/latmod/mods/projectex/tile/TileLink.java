package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.ProjectEXConfig;
import com.latmod.mods.projectex.ProjectEXUtils;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.UUID;

public class TileLink extends TileEntity implements IInventory, IEmcAcceptor {
    private final int tier;
    private String ownerUUID = "";
    private String ownerName = "";
    private double storedEmc = 0.0;
    private ItemStack[] inventory = new ItemStack[36];

    public TileLink() {
        this(0);
    }

    public TileLink(int tier) {
        this.tier = tier;
    }

    public void setOwner(EntityPlayer player) {
        if (player != null) {
            this.ownerUUID = player.getUniqueID().toString();
            this.ownerName = player.getCommandSenderName();
            markDirty();
        }
    }

    private EntityPlayer getPlayerByUUID(UUID uuid) {
        if (uuid == null || MinecraftServer.getServer() == null || MinecraftServer.getServer().getConfigurationManager() == null) {
            return null;
        }
        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                if (player.getUniqueID().equals(uuid)) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && !worldObj.isRemote && !ownerUUID.isEmpty()) {
            if (worldObj.getTotalWorldTime() % ProjectEXConfig.linkCooldown == 0) {
                // Convert inventory items to EMC
                boolean changed = false;
                for (int i = 0; i < inventory.length; i++) {
                    ItemStack stack = inventory[i];
                    if (stack != null && stack.stackSize > 0) {
                        double emc = ProjectEAPI.getEMCProxy().getValue(stack);
                        if (emc > 0) {
                            storedEmc += emc * stack.stackSize;
                            inventory[i] = null;
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    markDirty();
                }

                // Deposit stored EMC to owner
                if (storedEmc > 0) {
                    try {
                        UUID uuid = UUID.fromString(ownerUUID);
                        EntityPlayer player = getPlayerByUUID(uuid);
                        if (player != null) {
                            Transmutation.setEmc(player, Transmutation.getEmc(player) + storedEmc);
                            storedEmc = 0.0;
                        }
                    } catch (Exception e) {
                        // Player offline, keep in buffer
                    }
                }
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

    // IEmcStorage Implementation
    @Override
    public double getStoredEmc() {
        return storedEmc;
    }

    @Override
    public double getMaximumEmc() {
        return Double.MAX_VALUE;
    }

    // IEmcAcceptor Implementation
    @Override
    public double acceptEMC(ForgeDirection side, double amount) {
        storedEmc += amount;
        return amount;
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
        return "container.projectex.link";
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
        return stack != null && ProjectEAPI.getEMCProxy().getValue(stack) > 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerUUID = tag.getString("OwnerUUID");
        ownerName = tag.getString("OwnerName");
        storedEmc = tag.getDouble("StoredEMC");

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
        tag.setString("OwnerUUID", ownerUUID);
        tag.setString("OwnerName", ownerName);
        tag.setDouble("StoredEMC", storedEmc);

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
