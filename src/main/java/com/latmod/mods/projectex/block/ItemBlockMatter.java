package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.EnumTier;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMatter extends ItemBlock {

    public ItemBlockMatter(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack != null ? stack.getItemDamage() : 0;
        return field_150939_a.getUnlocalizedName() + "." + EnumTier.byMeta(meta).getName();
    }
}
