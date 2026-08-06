package com.latmod.mods.projectex.gui;

import com.latmod.mods.projectex.tile.TileStoneTable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiStoneTable extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("projectex", "textures/gui/alchemy_table.png");
    private final TileStoneTable tile;

    public GuiStoneTable(InventoryPlayer playerInv, TileStoneTable tile) {
        super(new ContainerStoneTable(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int meta = tile.getBlockMetadata();
        String name = StatCollector.translateToLocal("tile.projectex.stone_table." + com.latmod.mods.projectex.EnumTier.byMeta(meta).getName() + ".name");
        fontRendererObj.drawString(name, xSize / 2 - fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);

        // Progress Arrow (22px max width)
        if (tile.maxProgress > 0 && tile.progress > 0) {
            int progressWidth = (tile.progress * 22) / tile.maxProgress;
            drawTexturedModalRect(k + 79, l + 34, 176, 14, progressWidth, 16);
        }
    }
}
