package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEXConfig;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class TilePowerFlower extends TileEntity {
    private String ownerUUID = "";
    private String ownerName = "";
    private double storedEmc = 0.0;

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
        if (worldObj != null && !worldObj.isRemote) {
            if (worldObj.getTotalWorldTime() % 20L == 0L) {
                int meta = getBlockMetadata();
                if (meta < 0 || meta >= EnumTier.VALUES.length) meta = 0;

                double emcPerSec = ProjectEXConfig.powerFlowerEmc[meta];
                storedEmc += emcPerSec;

                if (!ownerUUID.isEmpty()) {
                    try {
                        UUID uuid = UUID.fromString(ownerUUID);
                        EntityPlayer player = getPlayerByUUID(uuid);
                        if (player != null) {
                            Transmutation.setEmc(player, Transmutation.getEmc(player) + storedEmc);
                            storedEmc = 0.0;
                        }
                    } catch (Exception e) {
                        // Player offline, keep accumulated EMC in storedEmc
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ownerUUID = tag.getString("OwnerUUID");
        ownerName = tag.getString("OwnerName");
        storedEmc = tag.getDouble("StoredEMC");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("OwnerUUID", ownerUUID);
        tag.setString("OwnerName", ownerName);
        tag.setDouble("StoredEMC", storedEmc);
    }
}
