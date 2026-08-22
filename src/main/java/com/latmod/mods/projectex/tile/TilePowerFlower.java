package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEXConfig;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class TilePowerFlower extends TileEntity {
    private String ownerUUID = "";
    private String ownerName = "";
    private UUID cachedUUID = null;
    private WeakReference<EntityPlayer> cachedPlayer = null;
    private double storedEmc = 0.0;
    private int offset = -1;

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
                offset = Math.abs(xCoord * 31 + yCoord * 17 + zCoord) % 20;
            }

            if ((worldObj.getTotalWorldTime() + offset) % 20L == 0L) {
                int meta = getBlockMetadata();
                if (meta < 0 || meta >= EnumTier.VALUES.length) meta = 0;

                double emcPerSec = ProjectEXConfig.powerFlowerEmc[meta];
                storedEmc += emcPerSec;

                EntityPlayer player = getPlayer();
                if (player != null) {
                    double newEmc = com.latmod.mods.projectex.ProjectEXUtils.getPlayerEmcSafe(player) + storedEmc;
                    com.latmod.mods.projectex.ProjectEXUtils.syncPlayerEMCAndKnowledge(player, newEmc, null);
                    storedEmc = 0.0;
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
        if (!ownerUUID.isEmpty()) {
            try {
                cachedUUID = UUID.fromString(ownerUUID);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("OwnerUUID", ownerUUID);
        tag.setString("OwnerName", ownerName);
        tag.setDouble("StoredEMC", storedEmc);
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
