package thaumcraft.api.nodes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IRevealer {
    boolean showNodes(ItemStack itemstack, EntityPlayer player);
}