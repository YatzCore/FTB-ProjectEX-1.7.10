package baubles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IBauble {
    BaubleType getBaubleType(ItemStack itemstack);
    void onWornTick(ItemStack itemstack, EntityLivingBase player);
    void onEquipped(ItemStack itemstack, EntityLivingBase player);
    void onUnequipped(ItemStack itemstack, EntityLivingBase player);
    boolean canEquip(ItemStack itemstack, EntityLivingBase player);
    boolean canUnequip(ItemStack itemstack, EntityLivingBase player);
}