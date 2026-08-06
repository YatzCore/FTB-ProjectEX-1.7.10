package com.latmod.mods.projectex.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ProjectEXNetHandler {
    public static final SimpleNetworkWrapper NET = NetworkRegistry.INSTANCE.newSimpleChannel("projectex");

    public static void init() {
        NET.registerMessage(MessageSetGUIStoneTable.class, MessageSetGUIStoneTable.class, 0, Side.CLIENT);
    }
}
