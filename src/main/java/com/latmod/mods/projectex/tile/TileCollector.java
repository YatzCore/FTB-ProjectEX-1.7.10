package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEXConfig;
import com.latmod.mods.projectex.ProjectEXUtils;
import moze_intel.projecte.api.tile.IEmcAcceptor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileCollector extends TileEntity {
    private double storedEmc = 0.0;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (worldObj != null && !worldObj.isRemote) {
            int meta = getBlockMetadata();
            if (meta < 0 || meta >= EnumTier.VALUES.length) meta = 0;

            double sunGen = ProjectEXConfig.collectorSunEmc[meta] / 20.0; // Per tick
            double maxEmc = ProjectEXConfig.collectorMaxEmc[meta];

            boolean canSeeSky = worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord);
            if (canSeeSky && worldObj.isDaytime()) {
                storedEmc = Math.min(maxEmc, storedEmc + sunGen);
            } else if (canSeeSky) {
                storedEmc = Math.min(maxEmc, storedEmc + (sunGen * 0.5));
            } else {
                storedEmc = Math.min(maxEmc, storedEmc + (sunGen * 0.1));
            }

            // Push to adjacent acceptors
            if (storedEmc > 0) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (tile != null) {
                        double max = ProjectEXUtils.getTileMaximumEmc(tile);
                        double stored = ProjectEXUtils.getTileStoredEmc(tile);
                        double needed = max - stored;
                        if (needed > 0) {
                            double toSend = Math.min(storedEmc, needed);
                            double accepted = ProjectEXUtils.acceptTileEmc(tile, dir.getOpposite(), toSend);
                            storedEmc -= accepted;
                            if (storedEmc <= 0) break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        storedEmc = tag.getDouble("StoredEMC");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("StoredEMC", storedEmc);
    }
}
