package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.ProjectEX;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemKnowledgeSharingBook extends Item {

    public ItemKnowledgeSharingBook() {
        setMaxStackSize(1);
        setCreativeTab(ProjectEX.TAB);
        setUnlocalizedName("projectex.knowledge_sharing_book");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("projectex:knowledge_sharing_book");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = stack.getTagCompound();

            if (!tag.hasKey("owner")) {
                tag.setString("owner", mp.getUniqueID().toString());
                tag.setString("owner_name", mp.getCommandSenderName());
                mp.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Knowledge Book bound to " + mp.getCommandSenderName()));
            } else {
                String ownerName = tag.getString("owner_name");
                mp.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Knowledge shared from " + ownerName));
                try {
                    Transmutation.sync(mp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("owner_name")) {
            list.add(EnumChatFormatting.GRAY + "Bound to: " + EnumChatFormatting.GOLD + stack.getTagCompound().getString("owner_name"));
        } else {
            list.add(EnumChatFormatting.GRAY + "Right-click to bind your knowledge");
        }
    }
}
