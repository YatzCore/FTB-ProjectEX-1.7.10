package com.latmod.mods.projectex.integration.ae2;

import com.latmod.mods.projectex.net.ProjectEXNetHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiMEEMCLink extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("projectex", "textures/gui/me_emc_link.png");

    private final ContainerMEEMCLink container;
    private GuiButton btnAccess;
    private GuiButton btnFilter;
    private GuiButton btnPrecision;
    private GuiButton btnClaim;

    public GuiMEEMCLink(InventoryPlayer playerInv, TileMEEMCLink tile) {
        super(new ContainerMEEMCLink(playerInv, tile));
        this.container = (ContainerMEEMCLink) inventorySlots;
        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        // 1. Priority Buttons (y=28)
        buttonList.add(new GuiButton(0, guiLeft + 8, guiTop + 28, 16, 13, "-10"));
        buttonList.add(new GuiButton(1, guiLeft + 25, guiTop + 28, 14, 13, "-1"));
        buttonList.add(new GuiButton(2, guiLeft + 40, guiTop + 28, 14, 13, "+1"));
        buttonList.add(new GuiButton(3, guiLeft + 55, guiTop + 28, 16, 13, "+10"));

        // 2. Access Mode Button (y=43)
        btnAccess = new GuiButton(4, guiLeft + 8, guiTop + 43, 64, 13, getAccessModeText());
        buttonList.add(btnAccess);

        // 3. Filter Mode Button (y=57)
        btnFilter = new GuiButton(5, guiLeft + 8, guiTop + 57, 64, 13, getFilterModeText());
        buttonList.add(btnFilter);

        // 4. Filter Precision Button (y=71)
        btnPrecision = new GuiButton(7, guiLeft + 8, guiTop + 71, 64, 13, getPrecisionText());
        buttonList.add(btnPrecision);

        // 5. Claim / Link Button (y=85)
        btnClaim = new GuiButton(6, guiLeft + 8, guiTop + 85, 64, 13, "Link / Claim");
        buttonList.add(btnClaim);
    }

    private String getAccessModeText() {
        int mode = container.getAccessMode();
        if (mode == 0) return EnumChatFormatting.GREEN + "Read/Write";
        if (mode == 1) return EnumChatFormatting.YELLOW + "Read Only";
        return EnumChatFormatting.RED + "Write Only";
    }

    private String getFilterModeText() {
        int mode = container.getFilterMode();
        if (mode == 0) return EnumChatFormatting.GRAY + "All Items";
        if (mode == 1) return EnumChatFormatting.AQUA + "Whitelist";
        return EnumChatFormatting.GOLD + "Blacklist";
    }

    private String getPrecisionText() {
        int prec = container.getFilterPrecision();
        if (prec == 0) return EnumChatFormatting.WHITE + "Exact";
        if (prec == 1) return EnumChatFormatting.YELLOW + "Fuzzy";
        return EnumChatFormatting.LIGHT_PURPLE + "OreDict";
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        int x = 0, y = 0, z = 0;
        int side = container.getSide();

        if (container.getTile() != null) {
            x = container.getTile().xCoord;
            y = container.getTile().yCoord;
            z = container.getTile().zCoord;
        }

        if (btn.id >= 0 && btn.id <= 3) {
            int delta = 0;
            if (btn.id == 0) delta = -10;
            if (btn.id == 1) delta = -1;
            if (btn.id == 2) delta = 1;
            if (btn.id == 3) delta = 10;
            int newPriority = container.getPriority() + delta;
            ProjectEXNetHandler.NET.sendToServer(new MessageMEEMCLink(x, y, z, side, 0, newPriority));
        } else if (btn.id == 4) {
            int newMode = (container.getAccessMode() + 1) % 3;
            ProjectEXNetHandler.NET.sendToServer(new MessageMEEMCLink(x, y, z, side, 1, newMode));
        } else if (btn.id == 5) {
            int newFilter = (container.getFilterMode() + 1) % 3;
            ProjectEXNetHandler.NET.sendToServer(new MessageMEEMCLink(x, y, z, side, 2, newFilter));
        } else if (btn.id == 6) {
            ProjectEXNetHandler.NET.sendToServer(new MessageMEEMCLink(x, y, z, side, 3, 0));
        } else if (btn.id == 7) {
            int newPrec = (container.getFilterPrecision() + 1) % 3;
            ProjectEXNetHandler.NET.sendToServer(new MessageMEEMCLink(x, y, z, side, 4, newPrec));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (btnAccess != null) btnAccess.displayString = getAccessModeText();
        if (btnFilter != null) btnFilter.displayString = getFilterModeText();
        if (btnPrecision != null) btnPrecision.displayString = getPrecisionText();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "ME EMC Link";
        fontRendererObj.drawString(title, 8, 6, 0x404040);

        String priorityStr = "Pri: " + container.getPriority();
        fontRendererObj.drawString(priorityStr, 8, 17, 0x555555);

        String owner = container.getOwnerName();
        if (owner == null || owner.isEmpty()) {
            fontRendererObj.drawString("Owner: " + EnumChatFormatting.RED + "Unbound", 78, 6, 0x404040);
        } else {
            String shortOwner = owner.length() > 10 ? owner.substring(0, 8) + ".." : owner;
            fontRendererObj.drawString("Owner: " + EnumChatFormatting.DARK_BLUE + shortOwner, 78, 6, 0x404040);
        }

        // Draw tooltips on button/slot hover
        int relX = mouseX - guiLeft;
        int relY = mouseY - guiTop;

        List<String> tooltip = new ArrayList<String>();

        // Priority hover
        if (relX >= 8 && relX <= 72 && relY >= 17 && relY <= 41) {
            tooltip.add(EnumChatFormatting.AQUA + "Storage Priority: " + EnumChatFormatting.WHITE + container.getPriority());
            tooltip.add(EnumChatFormatting.GRAY + "Higher priority drives are used first by AE2.");
        }
        // Access Mode hover
        else if (relX >= 8 && relX <= 72 && relY >= 43 && relY <= 56) {
            tooltip.add(EnumChatFormatting.AQUA + "Access Mode: " + getAccessModeText());
            int mode = container.getAccessMode();
            if (mode == 0) tooltip.add(EnumChatFormatting.GRAY + "Read/Write: AE2 can synthesize and absorb items.");
            else if (mode == 1) tooltip.add(EnumChatFormatting.GRAY + "Read Only: AE2 can synthesize items, no deposits.");
            else tooltip.add(EnumChatFormatting.GRAY + "Write Only: AE2 deposits are converted to EMC, no pulls.");
        }
        // Filter Mode hover
        else if (relX >= 8 && relX <= 72 && relY >= 57 && relY <= 70) {
            tooltip.add(EnumChatFormatting.AQUA + "Filter Mode: " + getFilterModeText());
            int fMode = container.getFilterMode();
            if (fMode == 0) tooltip.add(EnumChatFormatting.GRAY + "All Items: Exposes learned Transmutation items.");
            else if (fMode == 1) tooltip.add(EnumChatFormatting.GRAY + "Whitelist: Exposes ONLY items in the 4x4 filter grid.");
            else tooltip.add(EnumChatFormatting.GRAY + "Blacklist: Exposes all EXCEPT items in the 4x4 filter grid.");
        }
        // Filter Precision hover
        else if (relX >= 8 && relX <= 72 && relY >= 71 && relY <= 84) {
            tooltip.add(EnumChatFormatting.AQUA + "Match Precision: " + getPrecisionText());
            int prec = container.getFilterPrecision();
            if (prec == 0) tooltip.add(EnumChatFormatting.GRAY + "Exact: Matches Item, Metadata/Damage, and NBT.");
            else if (prec == 1) tooltip.add(EnumChatFormatting.GRAY + "Fuzzy: Matches Item ignoring Damage/NBT (damaged tools).");
            else tooltip.add(EnumChatFormatting.GRAY + "OreDict: Matches any item with equivalent OreDictionary tag.");
        }
        // Claim button hover
        else if (relX >= 8 && relX <= 72 && relY >= 85 && relY <= 98) {
            tooltip.add(EnumChatFormatting.AQUA + "Owner Link");
            tooltip.add(EnumChatFormatting.GRAY + "Click to bind this device to your Transmutation Table.");
        }
        // Ghost slot hover (when empty)
        else if (relX >= 80 && relX <= 152 && relY >= 22 && relY <= 94) {
            Slot hovered = getSlotAtPosition(mouseX, mouseY);
            if (hovered != null && hovered.slotNumber < 16 && !hovered.getHasStack()) {
                tooltip.add(EnumChatFormatting.YELLOW + "Ghost Filter Slot");
                tooltip.add(EnumChatFormatting.GRAY + "Left-click with an item to set filter.");
                tooltip.add(EnumChatFormatting.GRAY + "Click with empty hand to clear.");
            }
        }

        if (!tooltip.isEmpty()) {
            drawHoveringText(tooltip, relX, relY, fontRendererObj);
        }
    }

    private Slot getSlotAtPosition(int x, int y) {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(i);
            if (isMouseOverSlot(slot, x, y)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return mouseX >= guiLeft + slot.xDisplayPosition && mouseX <= guiLeft + slot.xDisplayPosition + 16
                && mouseY >= guiTop + slot.yDisplayPosition && mouseY <= guiTop + slot.yDisplayPosition + 16;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
    }
}
