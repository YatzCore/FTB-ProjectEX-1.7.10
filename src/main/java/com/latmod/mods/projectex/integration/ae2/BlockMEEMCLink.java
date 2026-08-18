package com.latmod.mods.projectex.integration.ae2;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.gui.ProjectEXGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockMEEMCLink extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockMEEMCLink() {
        super(Material.iron);
        setHardness(3.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(ProjectEX.TAB);
        setBlockName("projectex.me_emc_link");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMEEMCLink();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileMEEMCLink && entity instanceof EntityPlayer) {
            ((TileMEEMCLink) te).setOwner((EntityPlayer) entity);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player != null && player.isSneaking()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileMEEMCLink) {
                ((TileMEEMCLink) te).setOwner(player);
            }
            return true;
        }
        if (!world.isRemote) {
            player.openGui(ProjectEX.inst, ProjectEXGuiHandler.ME_EMC_LINK, world, x, y, z);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconTop = register.registerIcon("projectex:me_emc_link_top");
        iconBottom = register.registerIcon("projectex:me_emc_link_bottom");
        iconSide = register.registerIcon("projectex:me_emc_link_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return iconBottom != null ? iconBottom : blockIcon;
        if (side == 1) return iconTop != null ? iconTop : blockIcon;
        return iconSide != null ? iconSide : blockIcon;
    }
}
