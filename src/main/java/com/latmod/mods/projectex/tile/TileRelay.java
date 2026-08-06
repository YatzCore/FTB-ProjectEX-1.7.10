package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEXConfig;
import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.api.tile.IEmcProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileRelay extends TileEntity implements IEmcAcceptor, IEmcProvider {
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

            double transferRate = ProjectEXConfig.relayTransferEmc[meta] / 20.0; // Per tick

            if (storedEmc > 0) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (tile instanceof IEmcAcceptor && !(tile instanceof TileRelay)) {
                        IEmcAcceptor acceptor = (IEmcAcceptor) tile;
                        double needed = acceptor.getMaximumEmc() - acceptor.getStoredEmc();
                        if (needed > 0) {
                            double toSend = Math.min(storedEmc, Math.min(transferRate, needed));
                            double accepted = acceptor.acceptEMC(dir.getOpposite(), toSend);
                            storedEmc -= accepted;
                            if (storedEmc <= 0) break;
                        }
                    }
                }
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
        int meta = getBlockMetadata();
        if (meta < 0 || meta >= EnumTier.VALUES.length) meta = 0;
        return ProjectEXConfig.relayMaxEmc[meta];
    }

    // IEmcAcceptor Implementation
    @Override
    public double acceptEMC(ForgeDirection side, double amount) {
        double max = getMaximumEmc();
        double accepted = Math.min(amount, max - storedEmc);
        storedEmc += accepted;
        return accepted;
    }

    // IEmcProvider Implementation
    @Override
    public double provideEMC(ForgeDirection side, double amount) {
        double provided = Math.min(amount, storedEmc);
        storedEmc -= provided;
        return provided;
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
