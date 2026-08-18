package com.latmod.mods.projectex.harness;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Headless fake MinecraftServer implementation for unit testing.
 * Provides player resolution by UUID and username required for AE2 integration
 * and ProjectE Transmutation operations.
 */
public class FakeMinecraftServer extends MinecraftServer {

    private static FakeMinecraftServer activeInstance = null;
    private static MinecraftServer originalServerInstance = null;

    private FakeServerConfigurationManager configManager;
    private final List<EntityPlayerMP> playerList = new ArrayList<>();
    private final Map<UUID, EntityPlayerMP> playerByUUID = new HashMap<>();
    private final Map<String, EntityPlayerMP> playerByName = new HashMap<>();

    public FakeMinecraftServer() {
        super(new File("."), Proxy.NO_PROXY);
        this.worldServers = new WorldServer[3];
    }

    public static synchronized FakeMinecraftServer setup() {
        if (activeInstance != null) {
            return activeInstance;
        }
        try {
            Class<?> logClass = cpw.mods.fml.relauncher.FMLRelaunchLog.class;
            Class<?> sideClass = cpw.mods.fml.relauncher.Side.class;
            @SuppressWarnings("unchecked")
            Object serverSide = Enum.valueOf((Class<Enum>) sideClass, "SERVER");
            org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("FML");

            for (Field f : logClass.getDeclaredFields()) {
                f.setAccessible(true);
                if (Modifier.isStatic(f.getModifiers())) {
                    if (f.getType() == sideClass) {
                        f.set(null, serverSide);
                    } else if (org.apache.logging.log4j.Logger.class.isAssignableFrom(f.getType())) {
                        f.set(null, logger);
                    }
                }
            }

            Field staticInstanceField = null;
            for (Field f : logClass.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == logClass) {
                    staticInstanceField = f;
                    break;
                }
            }
            
            Object logInstance = null;
            if (staticInstanceField != null) {
                logInstance = staticInstanceField.get(null);
                if (logInstance == null) {
                    Constructor<?> ctor = logClass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    logInstance = ctor.newInstance();
                    staticInstanceField.set(null, logInstance);
                }
            }
            if (logInstance != null) {
                for (Field f : logClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (!Modifier.isStatic(f.getModifiers()) && org.apache.logging.log4j.Logger.class.isAssignableFrom(f.getType())) {
                        f.set(logInstance, logger);
                    }
                }
            }

            Class<?> fmlLogClass = cpw.mods.fml.common.FMLLog.class;
            for (Field f : fmlLogClass.getDeclaredFields()) {
                f.setAccessible(true);
                if (Modifier.isStatic(f.getModifiers()) && org.apache.logging.log4j.Logger.class.isAssignableFrom(f.getType())) {
                    f.set(null, logger);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            cpw.mods.fml.common.FMLCommonHandler.instance();
        } catch (Throwable ignored) {}

        try {
            net.minecraft.init.Bootstrap.func_151354_b();
        } catch (Throwable ignored) {}



        originalServerInstance = MinecraftServer.getServer();
        activeInstance = new FakeMinecraftServer();
        setStaticServerInstance(activeInstance);
        return activeInstance;
    }

    public static synchronized FakeMinecraftServer getInstance() {
        if (activeInstance == null) {
            setup();
        }
        return activeInstance;
    }

    public static synchronized void teardown() {
        setStaticServerInstance(originalServerInstance);
        if (activeInstance != null) {
            activeInstance.clearPlayers();
            activeInstance = null;
        }
        originalServerInstance = null;
    }

    private static void setStaticServerInstance(MinecraftServer server) {
        try {
            System.out.println("=== Bootstrap Methods ===");
            for (Method m : net.minecraft.init.Bootstrap.class.getDeclaredMethods()) {
                System.out.println("Bootstrap method: " + m.getName() + " params=" + java.util.Arrays.toString(m.getParameterTypes()));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        for (Field field : MinecraftServer.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && MinecraftServer.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    field.set(null, server);
                } catch (Exception ignored) {}
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void registerPlayer(EntityPlayerMP player) {
        if (player == null) return;
        if (!playerList.contains(player)) {
            playerList.add(player);
        }
        if (this.configManager != null && this.configManager.playerEntityList != null) {
            if (!this.configManager.playerEntityList.contains(player)) {
                this.configManager.playerEntityList.add(player);
            }
        }
        if (player.getUniqueID() != null) {
            playerByUUID.put(player.getUniqueID(), player);
        }
        if (player.getCommandSenderName() != null) {
            playerByName.put(player.getCommandSenderName(), player);
        }
    }

    public void unregisterPlayer(EntityPlayerMP player) {
        if (player == null) return;
        playerList.remove(player);
        if (this.configManager != null && this.configManager.playerEntityList != null) {
            this.configManager.playerEntityList.remove(player);
        }
        if (player.getUniqueID() != null) {
            playerByUUID.remove(player.getUniqueID());
        }
        if (player.getCommandSenderName() != null) {
            playerByName.remove(player.getCommandSenderName());
        }
    }

    public void clearPlayers() {
        playerList.clear();
        if (this.configManager != null && this.configManager.playerEntityList != null) {
            this.configManager.playerEntityList.clear();
        }
        playerByUUID.clear();
        playerByName.clear();
    }

    public EntityPlayerMP getPlayerByUUID(UUID uuid) {
        return playerByUUID.get(uuid);
    }

    public EntityPlayerMP getPlayerByName(String name) {
        return playerByName.get(name);
    }

    @Override
    public ServerConfigurationManager getConfigurationManager() {
        if (this.configManager == null) {
            this.configManager = new FakeServerConfigurationManager(this);
            for (EntityPlayerMP p : playerList) {
                if (!this.configManager.playerEntityList.contains(p)) {
                    this.configManager.playerEntityList.add(p);
                }
            }
        }
        return this.configManager;
    }

    @Override
    public WorldServer worldServerForDimension(int dim) {
        if (this.worldServers != null && this.worldServers.length > 0) {
            return this.worldServers[0];
        }
        return null;
    }

    @Override
    public boolean startServer() {
        return true;
    }

    @Override
    public boolean canStructuresSpawn() {
        return false;
    }

    @Override
    public WorldSettings.GameType getGameType() {
        return WorldSettings.GameType.SURVIVAL;
    }

    @Override
    public net.minecraft.world.EnumDifficulty func_147135_j() {
        return net.minecraft.world.EnumDifficulty.NORMAL;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public int getOpPermissionLevel() {
        return 4;
    }

    @Override
    public boolean isSnooperEnabled() {
        return false;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return false;
    }

    @Override
    public String shareToLAN(WorldSettings.GameType type, boolean allowCommands) {
        return "";
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public boolean func_152363_m() {
        return false;
    }

    public class FakeServerConfigurationManager extends ServerConfigurationManager {
        public FakeServerConfigurationManager(FakeMinecraftServer server) {
            super(server);
        }

        @Override
        public EntityPlayerMP func_152612_a(String username) {
            return FakeMinecraftServer.this.playerByName.get(username);
        }

        public EntityPlayerMP func_152602_a(UUID uuid) {
            return FakeMinecraftServer.this.playerByUUID.get(uuid);
        }
    }
}
