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

import java.lang.ref.WeakReference;
import java.util.UUID;

public class TileLink extends TileEntity implements IInventory, IEmcAcceptor {
    private final int tier;
    private String ownerUUID = "";
    private String ownerName = "";
    private UUID cachedUUID = null;
    private WeakReference<EntityPlayer> cachedPlayer = null;
    private double storedEmc = 0.0;
    private ItemStack[] inventory = new ItemStack[36];
    private int offset = -1;

    public TileLink() {
        this(0);
    }

    public TileLink(int tier) {
        this.tier = tier;
    }

    public void setOwner(EntityPlayer player) {
        if (player != null) {
            this.cachedUUID = player.getUniqueID();
            this.ownerUUID = cachedUUID.toString();
            this.ownerName = player.getCommandSenderName();
            this.cachedPlayer = new WeakReference<EntityPlayer>(player);
            markDirty();
            if (worldObj != null) {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    private EntityPlayer getPlayer() {
        if (cachedPlayer != null) {
            EntityPlayer p = cachedPlayer.get();
            if (p != null && !p.isDead) {
                return p;
            }
        }
        if (cachedUUID == null && !ownerUUID.isEmpty()) {
            try {
                cachedUUID = UUID.fromString(ownerUUID);
            } catch (Throwable ignored) {}
        }
        if (cachedUUID == null) return null;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) return null;

        for (Object obj : server.getConfigurationManager().playerEntityList) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                if (cachedUUID.equals(player.getUniqueID())) {
                    cachedPlayer = new WeakReference<EntityPlayer>(player);
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
            if (offset == -1) {
                offset = Math.abs(xCoord * 31 + yCoord * 17 + zCoord) % Math.max(1, ProjectEXConfig.linkCooldown);
            }

            if ((worldObj.getTotalWorldTime() + offset) % Math.max(1, ProjectEXConfig.linkCooldown) == 0) {
                boolean changed = false;
                for (int i = 0; i < inventory.length; i++) {
                    ItemStack stack = inventory[i];
                    if (stack != null && stack.stackSize > 0) {
                        double emc = ProjectEXUtils.getEmcValueDouble(stack);
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

                if (storedEmc > 0) {
                    EntityPlayer player = getPlayer();
                    if (player != null) {
                        double newEmc = Transmutation.getEmc(player) + storedEmc;
                        ProjectEXUtils.syncPlayerEMCAndKnowledge(player, newEmc, null);
                        storedEmc = 0.0;
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
        return stack != null && ProjectEXUtils.doesItemHaveEmc(stack);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerUUID = tag.getString("OwnerUUID");
        ownerName = tag.getString("OwnerName");
        storedEmc = tag.getDouble("StoredEMC");
        if (!ownerUUID.isEmpty()) {
            try {
                cachedUUID = UUID.fromString(ownerUUID);
            } catch (Throwable ignored) {}
        }

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

    @Override
    public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity pkt) {
        if (pkt != null && pkt.func_148857_g() != null) {
            readFromNBT(pkt.func_148857_g());
        }
    }
}
