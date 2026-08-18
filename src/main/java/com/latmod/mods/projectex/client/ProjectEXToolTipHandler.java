package com.latmod.mods.projectex.client;

import com.latmod.mods.projectex.ProjectEXConfig;
import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.gui.EMCFormat;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IItemEmc;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

@SideOnly(Side.CLIENT)
public class ProjectEXToolTipHandler {

    private static String rawEmcPrefix = "";
    private static String rawStackPrefix = "";
    private static String rawStoredPrefix = "";

    public static void init() {
        refreshCache();
        MinecraftForge.EVENT_BUS.register(new ProjectEXToolTipHandler());
    }

    public static void refreshCache() {
        try {
            rawEmcPrefix = EnumChatFormatting.YELLOW + StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix");
            rawStackPrefix = EnumChatFormatting.YELLOW + StatCollector.translateToLocal("pe.emc.stackemc_tooltip_prefix");
            rawStoredPrefix = EnumChatFormatting.YELLOW + StatCollector.translateToLocal("pe.emc.storedemc_tooltip");
        } catch (Throwable ignored) {}
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (!ProjectEXConfig.overrideEmcFormatter || event == null || event.toolTip == null || event.toolTip.isEmpty() || event.itemStack == null) {
            return;
        }

        boolean hasEmc = ProjectEXUtils.doesItemHaveEmc(event.itemStack);
        boolean isEmcItem = event.itemStack.getItem() instanceof IItemEmc;

        if (!hasEmc && !isEmcItem) {
            return;
        }

        if (rawEmcPrefix.isEmpty()) {
            refreshCache();
        }

        boolean shift = EMCFormat.isShiftDownCached();
        double unitEmc = hasEmc ? ProjectEXUtils.getEmcValueDouble(event.itemStack) : 0.0;
        int stackSize = event.itemStack.stackSize;

        int size = event.toolTip.size();
        for (int i = 0; i < size; i++) {
            Object obj = event.toolTip.get(i);
            if (!(obj instanceof String)) continue;
            String line = (String) obj;

            // Direct start matching with zero string allocations
            if (hasEmc && stackSize > 1 && (line.startsWith(rawStackPrefix) || line.contains("Stack EMC:"))) {
                double totalEmc = unitEmc * (double) stackSize;
                String formatted = shift ? EMCFormat.formatFull(totalEmc) : EMCFormat.formatCompact(totalEmc);
                event.toolTip.set(i, rawStackPrefix + " " + EnumChatFormatting.WHITE + formatted);
            } else if (hasEmc && (line.startsWith(rawEmcPrefix) || line.startsWith("EMC:"))) {
                String formatted = shift ? EMCFormat.formatFull(unitEmc) : EMCFormat.formatCompact(unitEmc);
                event.toolTip.set(i, rawEmcPrefix + " " + EnumChatFormatting.WHITE + formatted);
            } else if (isEmcItem && (line.startsWith(rawStoredPrefix) || line.contains("Stored EMC:"))) {
                IItemEmc itemEmc = (IItemEmc) event.itemStack.getItem();
                double stored = itemEmc.getStoredEmc(event.itemStack);
                double max = itemEmc.getMaximumEmc(event.itemStack);
                String formattedStored = shift ? EMCFormat.formatFull(stored) : EMCFormat.formatCompact(stored);
                String formattedMax = shift ? EMCFormat.formatFull(max) : EMCFormat.formatCompact(max);

                event.toolTip.set(i, rawStoredPrefix + " " + EnumChatFormatting.RESET + formattedStored + " / " + formattedMax);
            }
        }
    }
}
