package com.latmod.mods.projectex;

import com.latmod.mods.projectex.block.ProjectEXBlocks;
import com.latmod.mods.projectex.gui.ProjectEXGuiHandler;
import com.latmod.mods.projectex.integration.ae2.AE2Integration;
import com.latmod.mods.projectex.item.ProjectEXItems;
import com.latmod.mods.projectex.net.ProjectEXNetHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mod(
    modid = ProjectEX.MOD_ID,
    name = ProjectEX.MOD_NAME,
    version = ProjectEX.VERSION,
    dependencies = "required-after:ProjectE;after:appliedenergistics2"
)
public class ProjectEX {
    public static final String MOD_ID = "projectex";
    public static final String MOD_NAME = "Project EX";
    public static final String VERSION = "1.5.1";

    @Mod.Instance(MOD_ID)
    public static ProjectEX INSTANCE;
    public static ProjectEX inst;

    @SidedProxy(
        clientSide = "com.latmod.mods.projectex.client.ProjectEXClient",
        serverSide = "com.latmod.mods.projectex.ProjectEXCommon"
    )
    public static ProjectEXCommon PROXY;

    public static final CreativeTabs TAB = new CreativeTabs(MOD_ID) {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return Item.getItemFromBlock(ProjectEXBlocks.PERSONAL_LINK);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getIconItemStack() {
            return new ItemStack(getTabIconItem());
        }
    };

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        inst = this;
        INSTANCE = this;
        ProjectEXConfig.init(event.getSuggestedConfigurationFile());
        ProjectEXBlocks.init();
        ProjectEXItems.init();

        if (Loader.isModLoaded("appliedenergistics2")) {
            AE2Integration.preInit();
        }

        ProjectEXNetHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ProjectEXGuiHandler());
        PROXY.preInit();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        ProjectEXEventHandler.init();
        ProjectEXEMCRegistration.registerEMCValues();

        if (Loader.isModLoaded("appliedenergistics2")) {
            AE2Integration.init();
        }

        if (Loader.isModLoaded("Waila")) {
            com.latmod.mods.projectex.integration.waila.WailaIntegration.init();
        }

        if (ProjectEXConfig.blacklistPowerFlowerFromWatch) {
            FMLInterModComms.sendMessage("ProjectE", "timewatchblacklist", "com.latmod.mods.projectex.tile.TilePowerFlower");
        }
        PROXY.init();
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        PROXY.postInit();
    }
}
