package com.latmod.mods.projectex.client;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class ProjectESearchFix {

    public static void init() {
        fixNEISearchProviders();
        MinecraftForge.EVENT_BUS.register(new ProjectESearchFix());
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui != null) {
            fixNEISearchProviders();
        }
    }

    public static void fixNEISearchProviders() {
        if (Loader.isModLoaded("NotEnoughItems")) {
            try {
                if (codechicken.nei.SearchField.searchProviders == null) {
                    codechicken.nei.SearchField.searchProviders = new ArrayList<codechicken.nei.SearchField.ISearchProvider>();
                }
                if (codechicken.nei.SearchField.searchProviders.isEmpty()) {
                    codechicken.nei.SearchField.searchProviders.add(new codechicken.nei.SearchField.ISearchProvider() {
                        @Override
                        public codechicken.nei.api.ItemFilter getFilter(String searchText) {
                            return codechicken.nei.SearchField.getFilter(searchText);
                        }

                        @Override
                        public boolean isPrimary() {
                            return true;
                        }
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
