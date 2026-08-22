package thaumcraft.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IGoggles {
    boolean showIngameHUD(ItemStack itemstack, EntityPlayer player);
}