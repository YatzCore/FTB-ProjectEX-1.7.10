package com.latmod.mods.projectex.integration;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.gui.GuiStoneTable;

public class ProjectEXNEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        NEIStoneTableHandler handler = new NEIStoneTableHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        API.registerGuiOverlay(GuiStoneTable.class, "projectex.stone_table");
    }

    @Override
    public String getName() {
        return ProjectEX.MOD_NAME;
    }

    @Override
    public String getVersion() {
        return ProjectEX.VERSION;
    }
}
