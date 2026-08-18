package com.latmod.mods.projectex.integration.waila;

import com.latmod.mods.projectex.EnumTier;
import com.latmod.mods.projectex.ProjectEXConfig;
import com.latmod.mods.projectex.block.BlockLink;
import com.latmod.mods.projectex.block.BlockPowerFlower;
import com.latmod.mods.projectex.integration.ae2.BlockMEEMCLink;
import com.latmod.mods.projectex.integration.ae2.TileMEEMCLink;
import com.latmod.mods.projectex.tile.TileLink;
import com.latmod.mods.projectex.tile.TilePowerFlower;
import com.latmod.mods.projectex.gui.EMCFormat;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "Waila")
public class WailaDataProvider implements IWailaDataProvider {

    public static final WailaDataProvider INSTANCE = new WailaDataProvider();

    public static void register(IWailaRegistrar registrar) {
        // Power Flowers
        registrar.registerBodyProvider(INSTANCE, BlockPowerFlower.class);
        registrar.registerNBTProvider(INSTANCE, TilePowerFlower.class);

        // Links
        registrar.registerBodyProvider(INSTANCE, BlockLink.class);
        registrar.registerNBTProvider(INSTANCE, TileLink.class);

        // AE2 ME EMC Link (if AE2 is installed)
        if (Loader.isModLoaded("appliedenergistics2")) {
            registrar.registerBodyProvider(INSTANCE, BlockMEEMCLink.class);
            registrar.registerNBTProvider(INSTANCE, TileMEEMCLink.class);
        }
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        NBTTagCompound tag = accessor.getNBTData();

        if (te instanceof TilePowerFlower) {
            String owner = tag.getString("OwnerName");
            if (owner != null && !owner.isEmpty()) {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.WHITE + owner);
            } else {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.RED + "Unbound");
            }

            int meta = accessor.getMetadata();
            if (meta < 0 || meta >= EnumTier.VALUES.length) meta = 0;
            double gen = ProjectEXConfig.powerFlowerEmc[meta];
            currenttip.add(EnumChatFormatting.GREEN + "Generation: " + EnumChatFormatting.GOLD + "+" + EMCFormat.formatCompact(gen) + " EMC/s");

            double buffer = tag.getDouble("StoredEMC");
            if (buffer > 0.0) {
                currenttip.add(EnumChatFormatting.GRAY + "Buffer: " + EnumChatFormatting.YELLOW + EMCFormat.formatCompact(buffer) + " EMC");
            }
        } else if (te instanceof TileLink) {
            String owner = tag.getString("OwnerName");
            if (owner != null && !owner.isEmpty()) {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.WHITE + owner);
            } else {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.RED + "Unbound");
            }
            double buffer = tag.getDouble("StoredEMC");
            if (buffer > 0.0) {
                currenttip.add(EnumChatFormatting.GRAY + "Stored: " + EnumChatFormatting.YELLOW + EMCFormat.formatCompact(buffer) + " EMC");
            }
        } else if (Loader.isModLoaded("appliedenergistics2") && te instanceof TileMEEMCLink) {
            String owner = tag.getString("OwnerName");
            if (owner != null && !owner.isEmpty()) {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.WHITE + owner);
            } else {
                currenttip.add(EnumChatFormatting.AQUA + "Owner: " + EnumChatFormatting.RED + "Unbound");
            }

            int access = tag.getInteger("AccessMode");
            String accessStr = access == 0 ? EnumChatFormatting.GREEN + "Read/Write"
                             : access == 1 ? EnumChatFormatting.YELLOW + "Read Only"
                             : EnumChatFormatting.RED + "Write Only";
            currenttip.add(EnumChatFormatting.GRAY + "Mode: " + accessStr);

            int prio = tag.getInteger("Priority");
            currenttip.add(EnumChatFormatting.GRAY + "Priority: " + EnumChatFormatting.WHITE + prio);

            int filterMode = tag.getInteger("FilterMode");
            int precision = tag.getInteger("FilterPrecision");
            String precStr = precision == 0 ? "Exact" : precision == 1 ? "Fuzzy" : "OreDict";

            if (filterMode == 0) {
                currenttip.add(EnumChatFormatting.GRAY + "Filter: " + EnumChatFormatting.WHITE + "All Items");
            } else if (filterMode == 1) {
                currenttip.add(EnumChatFormatting.GRAY + "Filter: " + EnumChatFormatting.AQUA + "Whitelist (" + precStr + ")");
            } else {
                currenttip.add(EnumChatFormatting.GRAY + "Filter: " + EnumChatFormatting.GOLD + "Blacklist (" + precStr + ")");
            }
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z) {
        if (te != null) {
            te.writeToNBT(tag);
        }
        return tag;
    }
}
