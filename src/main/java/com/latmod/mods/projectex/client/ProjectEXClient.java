package com.latmod.mods.projectex.client;

import com.latmod.mods.projectex.ProjectEXCommon;

public class ProjectEXClient extends ProjectEXCommon {
    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
        ProjectESearchFix.init();
    }

    @Override
    public void postInit() {
        super.postInit();
    }
}
