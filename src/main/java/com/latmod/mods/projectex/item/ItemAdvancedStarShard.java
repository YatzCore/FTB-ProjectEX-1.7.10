package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.ProjectEX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class ItemAdvancedStarShard extends Item {

    public ItemAdvancedStarShard() {
        setCreativeTab(ProjectEX.TAB);
        setUnlocalizedName("projectex.final_star_shard");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("projectex:final_star_shard");
    }
}
