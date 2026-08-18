package com.latmod.mods.projectex.harness;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.Item;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockPlayerBuilder {

    private static Unsafe unsafe;

    public static void registerTestItem(Item item, int id, String name) {
        if (item == null) return;
        try {
            Item.itemRegistry.addObject(id, name, item);
        } catch (Throwable t) {
            try {
                Field fIntMap = null;
                for (Field f : net.minecraft.util.RegistryNamespaced.class.getDeclaredFields()) {
                    if (net.minecraft.util.ObjectIntIdentityMap.class.isAssignableFrom(f.getType())) {
                        fIntMap = f;
                        break;
                    }
                }
                if (fIntMap != null) {
                    fIntMap.setAccessible(true);
                    Object intMap = fIntMap.get(Item.itemRegistry);
                    if (intMap != null) {
                        for (Method m : intMap.getClass().getDeclaredMethods()) {
                            if (m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == Object.class && m.getParameterTypes()[1] == int.class) {
                                m.setAccessible(true);
                                m.invoke(intMap, item, id);
                            }
                        }
                    }
                }
                for (Field f : net.minecraft.util.RegistrySimple.class.getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Map map = (Map) f.get(Item.itemRegistry);
                        if (map != null) {
                            map.put(name, item);
                        }
                    }
                }
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
    }

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static EntityPlayerMP createPlayer(String name) {
        try {
            EntityPlayerMP player = (EntityPlayerMP) unsafe.allocateInstance(EntityPlayerMP.class);
            GameProfile profile = new GameProfile(UUID.randomUUID(), name);

            // Set GameProfile on EntityPlayer
            Field fProfile = null;
            for (Field f : EntityPlayer.class.getDeclaredFields()) {
                if (f.getType() == GameProfile.class) {
                    fProfile = f;
                    break;
                }
            }
            if (fProfile != null) {
                fProfile.setAccessible(true);
                fProfile.set(player, profile);
            }

            // Set NBTTagCompound customEntityData on Entity
            Field fEntityData = null;
            for (Field f : Entity.class.getDeclaredFields()) {
                if (f.getType() == NBTTagCompound.class) {
                    fEntityData = f;
                    break;
                }
            }
            if (fEntityData != null) {
                fEntityData.setAccessible(true);
                fEntityData.set(player, new NBTTagCompound());
            }

            // Set uniqueID on Entity
            Field fUUID = null;
            for (Field f : Entity.class.getDeclaredFields()) {
                if (f.getType() == UUID.class) {
                    fUUID = f;
                    break;
                }
            }
            if (fUUID != null) {
                fUUID.setAccessible(true);
                fUUID.set(player, profile.getId());
            }

            // Set extendedProperties map on Entity
            for (Field f : Entity.class.getDeclaredFields()) {
                if (java.util.Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    try {
                        if (f.get(player) == null) {
                            f.set(player, new java.util.HashMap());
                        }
                    } catch (Throwable ignored) {}
                }
            }

            // Set worldObj on Entity
            Field fWorld = null;
            for (Field f : Entity.class.getDeclaredFields()) {
                if (net.minecraft.world.World.class.isAssignableFrom(f.getType())) {
                    fWorld = f;
                    break;
                }
            }
            if (fWorld != null) {
                fWorld.setAccessible(true);
                try {
                    net.minecraft.world.World fakeWorld = (net.minecraft.world.World) unsafe.allocateInstance(net.minecraft.world.WorldServer.class);
                    fWorld.set(player, fakeWorld);
                } catch (Throwable ignored) {}
            }

            try {
                moze_intel.projecte.playerData.TransmutationProps props = (moze_intel.projecte.playerData.TransmutationProps) unsafe.allocateInstance(moze_intel.projecte.playerData.TransmutationProps.class);
                for (Field f : props.getClass().getDeclaredFields()) {
                    if (f.getType() == EntityPlayer.class) {
                        f.setAccessible(true);
                        f.set(props, player);
                    }
                    if (List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        f.set(props, new ArrayList());
                    }
                }
                player.registerExtendedProperties(moze_intel.projecte.playerData.TransmutationProps.PROP_NAME, props);
            } catch (Throwable t) {
                try {
                    moze_intel.projecte.playerData.TransmutationProps.register(player);
                } catch (Throwable ignored) {}
            }

            player.inventory = new InventoryPlayer(player);
            player.inventoryContainer = new net.minecraft.inventory.Container() {
                @Override
                public boolean canInteractWith(EntityPlayer player) {
                    return true;
                }
            };
            player.openContainer = player.inventoryContainer;

            FakeMinecraftServer server = FakeMinecraftServer.getInstance();
            server.registerPlayer(player);
            return player;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to allocate mock EntityPlayerMP", t);
        }
    }
}
