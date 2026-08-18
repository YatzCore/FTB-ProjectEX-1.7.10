package com.latmod.mods.projectex.integration.waila;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;

public class WailaIntegration {

    public static void init() {
        if (Loader.isModLoaded("Waila")) {
            FMLInterModComms.sendMessage("Waila", "register", "com.latmod.mods.projectex.integration.waila.WailaDataProvider.register");
        }
    }
}
