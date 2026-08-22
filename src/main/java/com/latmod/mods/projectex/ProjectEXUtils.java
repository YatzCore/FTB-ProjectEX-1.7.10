package com.latmod.mods.projectex;

import com.latmod.mods.projectex.gui.EMCFormat;
import com.latmod.mods.projectex.search.ProjectEXSearchEngine;
import com.latmod.mods.projectex.search.SearchHistoryManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.api.tile.IEmcAcceptor;
import moze_intel.projecte.api.tile.IEmcProvider;
import moze_intel.projecte.api.tile.IEmcStorage;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.NBTWhitelist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectEXUtils {
    private static final DecimalFormat FULL_FORMATTER = new DecimalFormat("#,###");

    public static final int[] MATTER_INDEXES = new int[]{12, 11, 13, 10, 14, 21, 15, 20, 16, 19, 17, 18};
    public static final int[] FUEL_INDEXES = new int[]{22, 23, 24, 25};

    private static final Map<TransmutationInventory, Integer> MATCHING_ITEM_COUNTS = new WeakHashMap<TransmutationInventory, Integer>();
    private static final Map<TransmutationInventory, Integer> TOTAL_PAGES = new WeakHashMap<TransmutationInventory, Integer>();

    private static Field FIELD_INV_PLAYER;
    private static Field FIELD_INV_INVENTORY;
    private static Field FIELD_INV_EMC;
    private static boolean FIELD_INV_EMC_RESOLVED = false;
    private static Method METHOD_INV_REMOVE_EMC;
    private static boolean METHOD_INV_REMOVE_EMC_RESOLVED = false;

    private static Field FIELD_GUI_TEXTBOX;
    private static Field FIELD_GUI_LEFT;
    private static Field FIELD_GUI_TOP;

    private static Method METHOD_GET_EMC_VALUE;
    private static boolean METHOD_GET_EMC_VALUE_RESOLVED = false;
    private static Method METHOD_DOES_ITEM_HAVE_EMC;
    private static boolean METHOD_DOES_ITEM_HAVE_EMC_RESOLVED = false;

    private static Method METHOD_TRANSMUTATION_GET_EMC;
    private static boolean METHOD_TRANSMUTATION_GET_EMC_RESOLVED = false;
    private static Method METHOD_TRANSMUTATION_SET_EMC;
    private static boolean METHOD_TRANSMUTATION_SET_EMC_RESOLVED = false;

    static {
        try {
            FIELD_INV_PLAYER = TransmutationInventory.class.getDeclaredField("player");
            FIELD_INV_PLAYER.setAccessible(true);
        } catch (Throwable ignored) {}

        try {
            FIELD_INV_INVENTORY = TransmutationInventory.class.getDeclaredField("inventory");
            FIELD_INV_INVENTORY.setAccessible(true);
        } catch (Throwable ignored) {}

        try {
            FIELD_GUI_LEFT = GuiContainer.class.getDeclaredField("guiLeft");
            FIELD_GUI_LEFT.setAccessible(true);
        } catch (Throwable t) {
            try {
                FIELD_GUI_LEFT = GuiContainer.class.getDeclaredField("field_147003_i");
                FIELD_GUI_LEFT.setAccessible(true);
            } catch (Throwable ignored) {}
        }

        try {
            FIELD_GUI_TOP = GuiContainer.class.getDeclaredField("guiTop");
            FIELD_GUI_TOP.setAccessible(true);
        } catch (Throwable t) {
            try {
                FIELD_GUI_TOP = GuiContainer.class.getDeclaredField("field_147009_r");
                FIELD_GUI_TOP.setAccessible(true);
            } catch (Throwable ignored) {}
        }
    }

    public static int getGuiLeft(GuiContainer gui) {
        if (gui == null) return 0;
        try {
            if (FIELD_GUI_LEFT != null) {
                return FIELD_GUI_LEFT.getInt(gui);
            }
        } catch (Throwable ignored) {}
        return (gui.width - 256) / 2;
    }

    public static int getGuiTop(GuiContainer gui) {
        if (gui == null) return 0;
        try {
            if (FIELD_GUI_TOP != null) {
                return FIELD_GUI_TOP.getInt(gui);
            }
        } catch (Throwable ignored) {}
        return (gui.height - 256) / 2;
    }

    // --- Dynamic Polymorphic EMC Bridge ---

    public static double getEmcValueDouble(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return 0.0;
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("test_emc")) {
            return stack.getTagCompound().getDouble("test_emc");
        }
        double custom = ProjectEXEMCRegistration.getProjectExEmc(stack);
        if (custom > 0.0) {
            return custom;
        }
        if (!METHOD_GET_EMC_VALUE_RESOLVED) {
            resolveGetEmcValueMethod();
        }
        if (METHOD_GET_EMC_VALUE != null) {
            try {
                Object res = METHOD_GET_EMC_VALUE.invoke(null, stack);
                if (res instanceof Number) {
                    return ((Number) res).doubleValue();
                }
            } catch (Throwable ignored) {
                sanitizeEmcMapper();
                try {
                    Object res = METHOD_GET_EMC_VALUE.invoke(null, stack);
                    if (res instanceof Number) {
                        return ((Number) res).doubleValue();
                    }
                } catch (Throwable ignored2) {}
            }
        }
        try {
            return (double) EMCHelper.getEmcValue(stack);
        } catch (Throwable t) {
            try {
                moze_intel.projecte.emc.SimpleStack ss = new moze_intel.projecte.emc.SimpleStack(stack);
                for (Field f : moze_intel.projecte.emc.EMCMapper.class.getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Map map = (Map) f.get(null);
                        if (map != null && map.containsKey(ss)) {
                            Object val = map.get(ss);
                            if (val instanceof Number) {
                                return ((Number) val).doubleValue();
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
            return 0.0;
        }
    }

    private static synchronized void resolveGetEmcValueMethod() {
        if (METHOD_GET_EMC_VALUE_RESOLVED) return;
        try {
            Class<?> emcHelperClass = Class.forName("moze_intel.projecte.utils.EMCHelper");
            for (Method m : emcHelperClass.getDeclaredMethods()) {
                if (m.getName().equals("getEmcValue") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                    m.setAccessible(true);
                    METHOD_GET_EMC_VALUE = m;
                    break;
                }
            }
        } catch (Throwable ignored) {}
        METHOD_GET_EMC_VALUE_RESOLVED = true;
    }

    public static boolean doesItemHaveEmc(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        if (ProjectEXEMCRegistration.getProjectExEmc(stack) > 0.0) {
            return true;
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("test_emc")) {
            return stack.getTagCompound().getDouble("test_emc") > 0.0;
        }
        if (!METHOD_DOES_ITEM_HAVE_EMC_RESOLVED) {
            resolveDoesItemHaveEmcMethod();
        }
        if (METHOD_DOES_ITEM_HAVE_EMC != null) {
            try {
                Object res = METHOD_DOES_ITEM_HAVE_EMC.invoke(null, stack);
                if (res instanceof Boolean) {
                    return (Boolean) res;
                }
            } catch (Throwable ignored) {
                sanitizeEmcMapper();
                try {
                    Object res = METHOD_DOES_ITEM_HAVE_EMC.invoke(null, stack);
                    if (res instanceof Boolean) {
                        return (Boolean) res;
                    }
                } catch (Throwable ignored2) {}
            }
        }
        try {
            return EMCHelper.doesItemHaveEmc(stack);
        } catch (Throwable t) {
            return getEmcValueDouble(stack) > 0.0;
        }
    }

    private static synchronized void resolveDoesItemHaveEmcMethod() {
        if (METHOD_DOES_ITEM_HAVE_EMC_RESOLVED) return;
        try {
            Class<?> emcHelperClass = Class.forName("moze_intel.projecte.utils.EMCHelper");
            for (Method m : emcHelperClass.getDeclaredMethods()) {
                if (m.getName().equals("doesItemHaveEmc") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                    m.setAccessible(true);
                    METHOD_DOES_ITEM_HAVE_EMC = m;
                    break;
                }
            }
        } catch (Throwable ignored) {}
        METHOD_DOES_ITEM_HAVE_EMC_RESOLVED = true;
    }

    public static double getPlayerEmcSafe(EntityPlayer player) {
        if (player == null) return 0.0;

        if (!METHOD_TRANSMUTATION_GET_EMC_RESOLVED) {
            resolveTransmutationGetEmc();
        }
        if (METHOD_TRANSMUTATION_GET_EMC != null) {
            try {
                Object res = METHOD_TRANSMUTATION_GET_EMC.invoke(null, player);
                if (res instanceof Number) {
                    return ((Number) res).doubleValue();
                }
            } catch (Throwable ignored) {}
        }

        try {
            moze_intel.projecte.playerData.TransmutationProps props = moze_intel.projecte.playerData.TransmutationProps.getDataFor(player);
            if (props != null) {
                for (Field f : props.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType() == double.class) {
                        return f.getDouble(props);
                    } else if (f.getType() == long.class) {
                        return (double) f.getLong(props);
                    } else if (f.getType() == int.class) {
                        return (double) f.getInt(props);
                    } else if (Number.class.isAssignableFrom(f.getType())) {
                        Object val = f.get(props);
                        if (val instanceof Number) {
                            return ((Number) val).doubleValue();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        try {
            return Transmutation.getEmc(player);
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    private static synchronized void resolveTransmutationGetEmc() {
        if (METHOD_TRANSMUTATION_GET_EMC_RESOLVED) return;
        try {
            Class<?> clazz = Class.forName("moze_intel.projecte.playerData.Transmutation");
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals("getEmc") && m.getParameterTypes().length == 1 && EntityPlayer.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    m.setAccessible(true);
                    METHOD_TRANSMUTATION_GET_EMC = m;
                    break;
                }
            }
        } catch (Throwable ignored) {}
        METHOD_TRANSMUTATION_GET_EMC_RESOLVED = true;
    }

    public static void setPlayerEmcSafe(EntityPlayer player, double emc) {
        if (player == null) return;
        if (emc < 0.0 || Double.isNaN(emc)) emc = 0.0;
        if (Double.isInfinite(emc) || emc > Double.MAX_VALUE) emc = Double.MAX_VALUE;

        // 1. Update TransmutationProps fields
        try {
            moze_intel.projecte.playerData.TransmutationProps props = moze_intel.projecte.playerData.TransmutationProps.getDataFor(player);
            if (props != null) {
                for (Field f : props.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType() == double.class) {
                        f.setDouble(props, emc);
                    } else if (f.getType() == long.class) {
                        long longVal = (long) Math.min((double) Long.MAX_VALUE, emc);
                        f.setLong(props, longVal);
                    } else if (f.getType() == int.class) {
                        int intVal = (int) Math.min((double) Integer.MAX_VALUE, emc);
                        f.setInt(props, intVal);
                    } else if (f.getType() == float.class) {
                        f.setFloat(props, (float) emc);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 2. Call Transmutation.setEmc dynamically
        if (!METHOD_TRANSMUTATION_SET_EMC_RESOLVED) {
            resolveTransmutationSetEmc();
        }
        if (METHOD_TRANSMUTATION_SET_EMC != null) {
            try {
                Class<?> paramType = METHOD_TRANSMUTATION_SET_EMC.getParameterTypes()[1];
                if (paramType == double.class) {
                    METHOD_TRANSMUTATION_SET_EMC.invoke(null, player, emc);
                } else if (paramType == long.class) {
                    long longVal = (long) Math.min((double) Long.MAX_VALUE, emc);
                    METHOD_TRANSMUTATION_SET_EMC.invoke(null, player, longVal);
                } else if (paramType == int.class) {
                    int intVal = (int) Math.min((double) Integer.MAX_VALUE, emc);
                    METHOD_TRANSMUTATION_SET_EMC.invoke(null, player, intVal);
                } else if (paramType == Number.class) {
                    METHOD_TRANSMUTATION_SET_EMC.invoke(null, player, Double.valueOf(emc));
                }
            } catch (Throwable ignored) {}
        } else {
            try {
                Transmutation.setEmc(player, emc);
            } catch (Throwable ignored) {}
        }
    }

    private static synchronized void resolveTransmutationSetEmc() {
        if (METHOD_TRANSMUTATION_SET_EMC_RESOLVED) return;
        try {
            Class<?> clazz = Class.forName("moze_intel.projecte.playerData.Transmutation");
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals("setEmc") && m.getParameterTypes().length == 2 && EntityPlayer.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    m.setAccessible(true);
                    METHOD_TRANSMUTATION_SET_EMC = m;
                    break;
                }
            }
        } catch (Throwable ignored) {}
        METHOD_TRANSMUTATION_SET_EMC_RESOLVED = true;
    }

    // --- Transmutation Inventory 64-bit Helpers ---

    public static double getInventoryEmc(TransmutationInventory inv) {
        if (inv == null) return 0.0;
        if (!FIELD_INV_EMC_RESOLVED) {
            resolveInvEmcField();
        }
        if (FIELD_INV_EMC != null) {
            try {
                Class<?> t = FIELD_INV_EMC.getType();
                if (t == double.class) {
                    return FIELD_INV_EMC.getDouble(inv);
                } else if (t == long.class) {
                    return (double) FIELD_INV_EMC.getLong(inv);
                } else if (t == int.class) {
                    return (double) FIELD_INV_EMC.getInt(inv);
                } else if (Number.class.isAssignableFrom(t)) {
                    Object val = FIELD_INV_EMC.get(inv);
                    if (val instanceof Number) {
                        return ((Number) val).doubleValue();
                    }
                }
            } catch (Throwable ignored) {}
        }
        try {
            return inv.emc;
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    public static void setInventoryEmc(TransmutationInventory inv, double emc) {
        if (inv == null) return;
        if (emc < 0.0 || Double.isNaN(emc)) emc = 0.0;
        if (Double.isInfinite(emc) || emc > Double.MAX_VALUE) emc = Double.MAX_VALUE;

        if (!FIELD_INV_EMC_RESOLVED) {
            resolveInvEmcField();
        }
        if (FIELD_INV_EMC != null) {
            try {
                Class<?> t = FIELD_INV_EMC.getType();
                if (t == double.class) {
                    FIELD_INV_EMC.setDouble(inv, emc);
                } else if (t == long.class) {
                    long longVal = (long) Math.min((double) Long.MAX_VALUE, emc);
                    FIELD_INV_EMC.setLong(inv, longVal);
                } else if (t == int.class) {
                    int intVal = (int) Math.min((double) Integer.MAX_VALUE, emc);
                    FIELD_INV_EMC.setInt(inv, intVal);
                } else if (t == float.class) {
                    FIELD_INV_EMC.setFloat(inv, (float) emc);
                }
            } catch (Throwable ignored) {}
        } else {
            try {
                inv.emc = emc;
            } catch (Throwable ignored) {}
        }
    }

    public static void removeInventoryEmc(TransmutationInventory inv, double amount) {
        if (inv == null || amount <= 0.0) return;
        boolean invoked = false;
        if (!METHOD_INV_REMOVE_EMC_RESOLVED) {
            resolveInvRemoveEmc();
        }
        if (METHOD_INV_REMOVE_EMC != null) {
            try {
                Class<?> pType = METHOD_INV_REMOVE_EMC.getParameterTypes()[0];
                if (pType == double.class) {
                    METHOD_INV_REMOVE_EMC.invoke(inv, amount);
                    invoked = true;
                } else if (pType == long.class) {
                    long longAmount = (long) Math.min((double) Long.MAX_VALUE, amount);
                    METHOD_INV_REMOVE_EMC.invoke(inv, longAmount);
                    invoked = true;
                } else if (pType == int.class) {
                    int intAmount = (int) Math.min((double) Integer.MAX_VALUE, amount);
                    METHOD_INV_REMOVE_EMC.invoke(inv, intAmount);
                    invoked = true;
                }
            } catch (Throwable ignored) {}
        }
        if (!invoked) {
            double current = getInventoryEmc(inv);
            double newEmc = Math.max(0.0, current - amount);
            setInventoryEmc(inv, newEmc);
        }
        EntityPlayer player = getPlayerFromInventory(inv);
        if (player != null) {
            setPlayerEmcSafe(player, getInventoryEmc(inv));
        }
    }

    private static synchronized void resolveInvEmcField() {
        if (FIELD_INV_EMC_RESOLVED) return;
        try {
            try {
                Field f = TransmutationInventory.class.getDeclaredField("emc");
                f.setAccessible(true);
                FIELD_INV_EMC = f;
            } catch (Throwable t) {
                for (Field f : TransmutationInventory.class.getDeclaredFields()) {
                    if (f.getType() == double.class || f.getType() == long.class || f.getType() == int.class) {
                        f.setAccessible(true);
                        FIELD_INV_EMC = f;
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {}
        FIELD_INV_EMC_RESOLVED = true;
    }

    private static synchronized void resolveInvRemoveEmc() {
        if (METHOD_INV_REMOVE_EMC_RESOLVED) return;
        try {
            for (Method m : TransmutationInventory.class.getDeclaredMethods()) {
                if (m.getName().equals("removeEmc") && m.getParameterTypes().length == 1) {
                    m.setAccessible(true);
                    METHOD_INV_REMOVE_EMC = m;
                    break;
                }
            }
        } catch (Throwable ignored) {}
        METHOD_INV_REMOVE_EMC_RESOLVED = true;
    }

    public static void handleInventoryAddEmc(TransmutationInventory inv, double value) {
        if (inv == null || value <= 0.0) return;
        double current = getInventoryEmc(inv);
        double newEmc = current + value;
        if (newEmc < 0.0 || Double.isNaN(newEmc) || Double.isInfinite(newEmc) || newEmc >= Double.MAX_VALUE) {
            newEmc = Double.MAX_VALUE;
        }
        setInventoryEmc(inv, newEmc);
        EntityPlayer player = getPlayerFromInventory(inv);
        if (player != null) {
            setPlayerEmcSafe(player, newEmc);
        }
    }

    public static boolean handleInventoryHasMaxedEmc(TransmutationInventory inv) {
        if (inv == null) return false;
        return getInventoryEmc(inv) >= Double.MAX_VALUE;
    }

    public static EntityPlayer getPlayerFromInventory(TransmutationInventory inv) {
        if (inv == null) return null;
        try {
            if (FIELD_INV_PLAYER != null) {
                EntityPlayer p = (EntityPlayer) FIELD_INV_PLAYER.get(inv);
                if (p != null) return p;
            }
            for (Field f : TransmutationInventory.class.getDeclaredFields()) {
                if (EntityPlayer.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    EntityPlayer p = (EntityPlayer) f.get(inv);
                    if (p != null) return p;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static ItemStack[] getInventoryArray(TransmutationInventory inv) {
        if (inv == null) return null;
        try {
            if (FIELD_INV_INVENTORY != null) {
                ItemStack[] arr = (ItemStack[]) FIELD_INV_INVENTORY.get(inv);
                if (arr != null) return arr;
            }
            for (Field f : TransmutationInventory.class.getDeclaredFields()) {
                if (f.getType() == ItemStack[].class) {
                    f.setAccessible(true);
                    ItemStack[] arr = (ItemStack[]) f.get(inv);
                    if (arr != null) return arr;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static final ConcurrentHashMap<UUID, Object> PLAYER_LOCKS = new ConcurrentHashMap<UUID, Object>();

    public static Object getPlayerLock(UUID uuid) {
        if (uuid == null) return new Object();
        Object lock = PLAYER_LOCKS.get(uuid);
        if (lock == null) {
            lock = new Object();
            Object prev = PLAYER_LOCKS.putIfAbsent(uuid, lock);
            if (prev != null) {
                lock = prev;
            }
        }
        return lock;
    }

    public static boolean areKnowledgeStacksEqual(ItemStack s1, ItemStack s2) {
        if (s1 == s2) return true;
        if (s1 == null || s2 == null) return false;
        if (s1.getItem() != s2.getItem()) return false;
        if (s1.getItemDamage() != s2.getItemDamage()) return false;
        return ItemStack.areItemStackTagsEqual(s1, s2);
    }

    public static ItemStack normalizeKnowledgeStack(ItemStack stack) {
        if (stack == null) return null;
        ItemStack copy = stack.copy();
        copy.stackSize = 1;
        return copy;
    }

    public static void syncPlayerEMCAndKnowledge(EntityPlayer player, double newEmc, ItemStack newlyLearnedStack) {
        if (player == null) return;
        UUID uuid = player.getUniqueID();
        Object lock = uuid != null ? getPlayerLock(uuid) : new Object();
        synchronized (lock) {
            setPlayerEmcSafe(player, newEmc);
            ItemStack singleLearned = newlyLearnedStack != null ? normalizeKnowledgeStack(newlyLearnedStack) : null;
            if (singleLearned != null) {
                addKnowledgeSafe(singleLearned, player);
            }
            if (player.openContainer instanceof TransmutationContainer) {
                TransmutationContainer tc = (TransmutationContainer) player.openContainer;
                if (tc.transmutationInventory != null) {
                    setInventoryEmc(tc.transmutationInventory, newEmc);
                    if (singleLearned != null) {
                        if (tc.transmutationInventory.knowledge == null) {
                            tc.transmutationInventory.knowledge = new ArrayList<ItemStack>();
                        }
                        boolean found = false;
                        for (ItemStack k : tc.transmutationInventory.knowledge) {
                            if (k != null && areKnowledgeStacksEqual(k, singleLearned)) {
                                found = true;
                                k.stackSize = 1;
                                break;
                            }
                        }
                        if (!found) {
                            tc.transmutationInventory.knowledge.add(singleLearned.copy());
                        }
                    }
                    handleUpdateOutputs(tc.transmutationInventory, true);
                    try {
                        tc.detectAndSendChanges();
                    } catch (Throwable ignored) {}
                }
            }
            if (player instanceof EntityPlayerMP) {
                try {
                    Transmutation.sync(player);
                } catch (Throwable ignored) {}
            }
            if (uuid != null) {
                try {
                    if (cpw.mods.fml.common.Loader.isModLoaded("appliedenergistics2")) {
                        com.latmod.mods.projectex.integration.ae2.AE2Integration.notifyHandlersForPlayer(uuid);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    public static int getMatchingItemCount(TransmutationInventory inv) {
        if (inv == null) return 0;
        Integer count = MATCHING_ITEM_COUNTS.get(inv);
        return count != null ? count : 0;
    }

    public static int getTotalPages(TransmutationInventory inv) {
        if (inv == null) return 1;
        Integer pages = TOTAL_PAGES.get(inv);
        return pages != null && pages > 0 ? pages : 1;
    }

    public static void handleUpdateOutputs(TransmutationInventory inv, boolean isSearchPage) {
        if (inv == null) return;
        EntityPlayer player = getPlayerFromInventory(inv);
        double invEmc = getInventoryEmc(inv);
        if (player != null) {
            double playerEmc = getPlayerEmcSafe(player);
            if (playerEmc > invEmc || invEmc == 0.0) {
                setInventoryEmc(inv, playerEmc);
                invEmc = playerEmc;
            }
            List<ItemStack> rawKnowledge = null;
            try {
                rawKnowledge = Transmutation.getKnowledge(player);
            } catch (Throwable ignored) {}
            if (rawKnowledge != null && !rawKnowledge.isEmpty()) {
                inv.knowledge = new ArrayList<ItemStack>(rawKnowledge);
            }
        }

        if (inv.knowledge == null) {
            inv.knowledge = new ArrayList<ItemStack>();
        }

        // Deduplicate knowledge list and enforce stackSize = 1
        List<ItemStack> cleanKnowledge = new ArrayList<ItemStack>();
        for (ItemStack k : inv.knowledge) {
            if (k == null || k.getItem() == null) continue;
            k.stackSize = 1;
            boolean alreadyPresent = false;
            for (ItemStack existing : cleanKnowledge) {
                if (areKnowledgeStacksEqual(existing, k)) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) {
                cleanKnowledge.add(k);
            }
        }
        inv.knowledge = cleanKnowledge;
        List<ItemStack> knowledge = inv.knowledge;

        ItemStack[] inventory = getInventoryArray(inv);
        if (inventory == null) return;

        // Clear matter and fuel slots
        for (int idx : MATTER_INDEXES) {
            if (idx < inventory.length) {
                inventory[idx] = null;
            }
        }
        for (int idx : FUEL_INDEXES) {
            if (idx < inventory.length) {
                inventory[idx] = null;
            }
        }

        // 64-bit descending EMC sort
        Collections.sort(knowledge, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack s1, ItemStack s2) {
                double e1 = getEmcValueDouble(s1);
                double e2 = getEmcValueDouble(s2);
                return Double.compare(e2, e1);
            }
        });

        ProjectEXSearchEngine.IQueryPredicate predicate = ProjectEXSearchEngine.parseQuery(inv.filter);

        ItemStack lock = inventory.length > 8 ? inventory[8] : null;
        List<ItemStack> matching = new ArrayList<ItemStack>();

        if (lock != null) {
            double lockEmc = getEmcValueDouble(lock);
            if (invEmc < lockEmc) {
                MATCHING_ITEM_COUNTS.put(inv, 0);
                TOTAL_PAGES.put(inv, 1);
                return;
            }

            ItemStack normalizedLock = null;
            try {
                normalizedLock = ItemHelper.getNormalizedStack(lock);
            } catch (Throwable ignored) {
                normalizedLock = lock.copy();
            }
            if (normalizedLock != null && normalizedLock.hasTagCompound() && !shouldDupeWithNBTSafe(normalizedLock)) {
                normalizedLock.setTagCompound(new NBTTagCompound());
            }

            for (ItemStack stack : knowledge) {
                if (stack == null) continue;
                double stackEmc = getEmcValueDouble(stack);
                if (stackEmc > lockEmc) continue;
                if (normalizedLock != null && ItemHelper.basicAreStacksEqual(normalizedLock, stack)) continue;
                if (!predicate.test(stack, player, invEmc)) continue;
                matching.add(stack);
            }
        } else {
            for (ItemStack stack : knowledge) {
                if (stack == null) continue;
                double stackEmc = getEmcValueDouble(stack);
                if (stackEmc > invEmc) continue;
                if (!predicate.test(stack, player, invEmc)) continue;
                matching.add(stack);
            }
        }

        int totalMatching = matching.size();
        int totalPages = totalMatching > 0 ? (int) Math.ceil((double) totalMatching / 12.0) : 1;
        if (inv.searchpage < 0) inv.searchpage = 0;
        if (inv.searchpage >= totalPages && totalPages > 0) {
            inv.searchpage = totalPages - 1;
        }

        MATCHING_ITEM_COUNTS.put(inv, totalMatching);
        TOTAL_PAGES.put(inv, totalPages);

        int skip = inv.searchpage * 12;
        int matterIdx = 0;
        int fuelIdx = 0;

        for (int i = skip; i < matching.size(); i++) {
            ItemStack stack = matching.get(i);
            if (stack == null) continue;
            ItemStack displayStack = stack.copy();
            displayStack.stackSize = 1;

            if (isStackFuelSafe(displayStack)) {
                if (fuelIdx < FUEL_INDEXES.length && FUEL_INDEXES[fuelIdx] < inventory.length) {
                    inventory[FUEL_INDEXES[fuelIdx++]] = displayStack;
                }
            } else {
                if (matterIdx < MATTER_INDEXES.length && MATTER_INDEXES[matterIdx] < inventory.length) {
                    inventory[MATTER_INDEXES[matterIdx++]] = displayStack;
                }
            }

            if (matterIdx >= MATTER_INDEXES.length && fuelIdx >= FUEL_INDEXES.length) {
                break;
            }
        }
    }

    // --- Transmutation GUI & Slot 64-bit Helpers ---

    @SideOnly(Side.CLIENT)
    public static void handleKnowledgeSync() {
        try {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                double currentEmc = getPlayerEmcSafe(player);
                if (player.openContainer instanceof TransmutationContainer) {
                    TransmutationContainer container = (TransmutationContainer) player.openContainer;
                    if (container.transmutationInventory != null) {
                        setInventoryEmc(container.transmutationInventory, currentEmc);
                        handleUpdateOutputs(container.transmutationInventory, true);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    @SideOnly(Side.CLIENT)
    public static void drawTransmutationForeground(TransmutationInventory inv) {
        if (inv == null) return;
        try {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            String title = StatCollector.translateToLocal("pe.transmutation.transmute");
            font.drawString(title, 6, 8, 4210752);
            String emcStr = EMCFormat.formatGuiEmc(getInventoryEmc(inv));
            font.drawString(emcStr, 6, 102, 4210752);
        } catch (Throwable ignored) {}
    }

    @SideOnly(Side.CLIENT)
    public static void drawTransmutationForeground(GuiContainer gui, int mouseX, int mouseY) {
        if (gui == null) return;
        try {
            TransmutationInventory inv = null;
            if (gui.inventorySlots instanceof TransmutationContainer) {
                inv = ((TransmutationContainer) gui.inventorySlots).transmutationInventory;
            }
            if (inv == null) return;
            double invEmc = getInventoryEmc(inv);
            if (Minecraft.getMinecraft().thePlayer != null) {
                EntityPlayer clientPlayer = Minecraft.getMinecraft().thePlayer;
                double currentEmc = getPlayerEmcSafe(clientPlayer);
                List<ItemStack> currentKnowledge = Transmutation.getKnowledge(clientPlayer);
                int knowledgeSize = currentKnowledge != null ? currentKnowledge.size() : 0;
                if ((currentEmc > 0.0 && Math.abs(currentEmc - invEmc) > 1e-4) || inv.knowledge == null || (knowledgeSize > 0 && inv.knowledge.size() != knowledgeSize)) {
                    setInventoryEmc(inv, currentEmc);
                    invEmc = currentEmc;
                    handleUpdateOutputs(inv, true);
                }
            }

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            String title = StatCollector.translateToLocal("pe.transmutation.transmute");
            font.drawString(title, 6, 8, 4210752);

            String emcStr = EMCFormat.formatGuiEmc(invEmc);
            font.drawString(emcStr, 6, 102, 4210752);

            int totalMatching = getMatchingItemCount(inv);
            int totalPages = getTotalPages(inv);

            // If user has a search query, render matching items counter
            if (inv.filter != null && !inv.filter.trim().isEmpty()) {
                String matchStr = totalMatching + " items";
                font.drawString(matchStr, 88, 20, 0x555555);
            } else if (totalPages > 1) {
                String pageStr = "Page " + (inv.searchpage + 1) + "/" + totalPages;
                font.drawString(pageStr, 88, 20, 0x555555);
            }

            // Check if mouse is hovering over search box: x: 88, y: 8, width: 45, height: 10
            int guiLeft = getGuiLeft(gui);
            int guiTop = getGuiTop(gui);
            int relX = mouseX - guiLeft;
            int relY = mouseY - guiTop;

            if (relX >= 86 && relX <= 135 && relY >= 6 && relY <= 20) {
                if (isShiftKeyDownCached()) {
                    drawSearchHelpTooltip(relX, relY, font);
                } else {
                    drawSearchHintTooltip(relX, relY, font);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static volatile long lastShiftCheckTime = 0L;
    private static volatile boolean cachedShiftState = false;

    public static boolean isShiftKeyDownCached() {
        long now = Minecraft.getSystemTime();
        if (now - lastShiftCheckTime > 50L) {
            lastShiftCheckTime = now;
            try {
                if (org.lwjgl.input.Keyboard.isCreated()) {
                    cachedShiftState = org.lwjgl.input.Keyboard.isKeyDown(42) || org.lwjgl.input.Keyboard.isKeyDown(54);
                } else {
                    cachedShiftState = false;
                }
            } catch (Throwable ignored) {
                cachedShiftState = false;
            }
        }
        return cachedShiftState;
    }

    @SideOnly(Side.CLIENT)
    private static void drawSearchHintTooltip(int x, int y, FontRenderer font) {
        List<String> list = new ArrayList<String>(1);
        list.add(EnumChatFormatting.GRAY + "Hold " + EnumChatFormatting.YELLOW + "SHIFT" + EnumChatFormatting.GRAY + " for search syntax");
        drawCustomHoveringText(list, x + 8, y + 8, font);
    }

    @SideOnly(Side.CLIENT)
    private static void drawSearchHelpTooltip(int x, int y, FontRenderer font) {
        List<String> list = new ArrayList<String>();
        list.add(EnumChatFormatting.GOLD + "Transmutation Search Filters:");
        list.add(EnumChatFormatting.YELLOW + "@<mod> " + EnumChatFormatting.GRAY + "- Mod filter (" + EnumChatFormatting.AQUA + "@projectex" + EnumChatFormatting.GRAY + ")");
        list.add(EnumChatFormatting.YELLOW + "#<range> " + EnumChatFormatting.GRAY + "- EMC budget (" + EnumChatFormatting.AQUA + "#1k-50k" + EnumChatFormatting.GRAY + ", " + EnumChatFormatting.AQUA + "#<10k" + EnumChatFormatting.GRAY + ", " + EnumChatFormatting.AQUA + "#>1M" + EnumChatFormatting.GRAY + ")");
        list.add(EnumChatFormatting.YELLOW + "#aff " + EnumChatFormatting.GRAY + "- Affordable with current EMC");
        list.add(EnumChatFormatting.YELLOW + "$<ore> " + EnumChatFormatting.GRAY + "- OreDict tag (" + EnumChatFormatting.AQUA + "$ingot" + EnumChatFormatting.GRAY + ", " + EnumChatFormatting.AQUA + "$ore" + EnumChatFormatting.GRAY + ")");
        list.add(EnumChatFormatting.YELLOW + "%<text> " + EnumChatFormatting.GRAY + "- Search in tooltips (" + EnumChatFormatting.AQUA + "%energy" + EnumChatFormatting.GRAY + ")");
        list.add(EnumChatFormatting.YELLOW + "!<filter> " + EnumChatFormatting.GRAY + "- Exclude/Negate (" + EnumChatFormatting.AQUA + "!@minecraft" + EnumChatFormatting.GRAY + ")");
        list.add(EnumChatFormatting.YELLOW + "^fuel " + EnumChatFormatting.GRAY + "/ " + EnumChatFormatting.YELLOW + "^matter " + EnumChatFormatting.GRAY + "- Item category");
        list.add(EnumChatFormatting.DARK_GRAY + "Tip: Space = AND, | = OR, quotes for phrases");
        list.add(EnumChatFormatting.DARK_GRAY + "Use Up/Down arrows for search history");

        drawCustomHoveringText(list, x + 8, y + 8, font);
    }

    @SideOnly(Side.CLIENT)
    private static void drawCustomHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
        if (textLines.isEmpty()) return;

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int tooltipWidth = 0;
        for (String line : textLines) {
            int lineLen = font.getStringWidth(line);
            if (lineLen > tooltipWidth) {
                tooltipWidth = lineLen;
            }
        }

        int tooltipX = x + 12;
        int tooltipY = y - 12;
        int tooltipHeight = 8;
        if (textLines.size() > 1) {
            tooltipHeight += 2 + (textLines.size() - 1) * 10;
        }

        // Keep inside bounds if possible
        if (tooltipX + tooltipWidth > 240) {
            tooltipX -= 28 + tooltipWidth;
        }
        if (tooltipY + tooltipHeight + 6 > 230) {
            tooltipY = 230 - tooltipHeight - 6;
        }

        int zLevel = 300;
        int bgColor = 0xF0100010;
        drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, bgColor, bgColor, zLevel);
        drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, bgColor, bgColor, zLevel);
        drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, bgColor, bgColor, zLevel);
        drawGradientRect(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, bgColor, bgColor, zLevel);
        drawGradientRect(tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, bgColor, bgColor, zLevel);

        int borderColor1 = 0x505000FF;
        int borderColor2 = (borderColor1 & 0xFEFEFE) >> 1 | borderColor1 & 0xFF000000;
        drawGradientRect(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColor1, borderColor2, zLevel);
        drawGradientRect(tooltipX + tooltipWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColor1, borderColor2, zLevel);
        drawGradientRect(tooltipX - 3, tooltipY - 3, tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, borderColor1, borderColor1, zLevel);
        drawGradientRect(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, borderColor2, borderColor2, zLevel);

        for (int i = 0; i < textLines.size(); ++i) {
            String line = textLines.get(i);
            font.drawStringWithShadow(line, tooltipX, tooltipY, -1);
            if (i == 0) {
                tooltipY += 2;
            }
            tooltipY += 10;
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    private static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, int zLevel) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex((double)right, (double)top, (double)zLevel);
        tessellator.addVertex((double)left, (double)top, (double)zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex((double)left, (double)bottom, (double)zLevel);
        tessellator.addVertex((double)right, (double)bottom, (double)zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static GuiTextField getTextBoxFilter(GuiContainer gui) {
        if (gui == null) return null;
        try {
            if (FIELD_GUI_TEXTBOX == null) {
                FIELD_GUI_TEXTBOX = gui.getClass().getDeclaredField("textBoxFilter");
                FIELD_GUI_TEXTBOX.setAccessible(true);
            }
            return (GuiTextField) FIELD_GUI_TEXTBOX.get(gui);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void handleTransmutationInitGui(GuiContainer gui) {
        if (gui == null) return;
        GuiTextField textBox = getTextBoxFilter(gui);
        if (textBox != null) {
            textBox.setMaxStringLength(128);
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean handleTransmutationKeyTyped(GuiContainer gui, char typedChar, int keyCode) {
        if (gui == null) return false;
        GuiTextField textBox = getTextBoxFilter(gui);
        if (textBox == null || !textBox.isFocused()) {
            return false;
        }

        TransmutationInventory inv = null;
        if (gui.inventorySlots instanceof TransmutationContainer) {
            inv = ((TransmutationContainer) gui.inventorySlots).transmutationInventory;
        }
        if (inv == null) return false;

        // Up arrow: Navigate back in history
        if (keyCode == Keyboard.KEY_UP) {
            String prev = SearchHistoryManager.navigateUp(textBox.getText());
            textBox.setText(prev);
            inv.filter = prev.toLowerCase(Locale.ROOT);
            inv.searchpage = 0;
            inv.updateOutputs(true);
            return true;
        }

        // Down arrow: Navigate forward in history
        if (keyCode == Keyboard.KEY_DOWN) {
            String next = SearchHistoryManager.navigateDown(textBox.getText());
            textBox.setText(next);
            inv.filter = next.toLowerCase(Locale.ROOT);
            inv.searchpage = 0;
            inv.updateOutputs(true);
            return true;
        }

        // Enter: Save search query to history and unfocus
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            String text = textBox.getText();
            if (text != null && !text.trim().isEmpty()) {
                SearchHistoryManager.addHistory(text);
            }
            textBox.setFocused(false);
            return true;
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void handleTransmutationGuiClosed(GuiContainer gui) {
        if (gui == null) return;
        GuiTextField textBox = getTextBoxFilter(gui);
        if (textBox != null) {
            String text = textBox.getText();
            if (text != null && !text.trim().isEmpty()) {
                SearchHistoryManager.addHistory(text);
            }
        }
        SearchHistoryManager.resetCursor();
    }

    public static ItemStack handleOutputTake(Slot slot, int amount, TransmutationInventory inv) {
        if (slot == null || inv == null) return null;
        ItemStack stack = slot.getStack();
        if (stack == null) return null;
        ItemStack copy = stack.copy();
        copy.stackSize = amount;
        double cost = getEmcValueDouble(copy) * (double) amount;
        double currentEmc = getInventoryEmc(inv);
        if (cost > currentEmc) {
            copy.stackSize = 0;
            return copy;
        }
        EntityPlayer player = getPlayerFromInventory(inv);
        UUID uuid = player != null ? player.getUniqueID() : null;
        Object lock = uuid != null ? getPlayerLock(uuid) : new Object();

        synchronized (lock) {
            removeInventoryEmc(inv, cost);
            inv.checkForUpdates();

            if (player != null) {
                syncPlayerEMCAndKnowledge(player, getInventoryEmc(inv), null);
            } else {
                handleUpdateOutputs(inv, true);
            }
        }
        return copy;
    }

    public static boolean canTakeOutput(Slot slot, TransmutationInventory inv) {
        if (slot == null || !slot.getHasStack() || inv == null) return true;
        ItemStack stack = slot.getStack();
        if (stack == null) return true;
        double emc = getEmcValueDouble(stack);
        return emc <= getInventoryEmc(inv);
    }

    public static void handleConsume(Slot slot, ItemStack stack, TransmutationInventory inv) {
        if (stack == null || inv == null) return;
        ItemStack copy = normalizeKnowledgeStack(stack);
        EntityPlayer player = getPlayerFromInventory(inv);
        UUID uuid = player != null ? player.getUniqueID() : null;
        Object lock = uuid != null ? getPlayerLock(uuid) : new Object();

        synchronized (lock) {
            while (!handleInventoryHasMaxedEmc(inv) && stack.stackSize > 0) {
                double itemEmc = getEmcValueDouble(stack);
                if (itemEmc <= 0.0 && !isTome(stack.getItem())) break;
                handleInventoryAddEmc(inv, itemEmc);
                stack.stackSize--;
            }

            if (stack.stackSize <= 0) {
                if (slot != null) {
                    try {
                        slot.putStack(null);
                    } catch (Throwable ignored) {}
                }
                ItemStack[] invArr = getInventoryArray(inv);
                if (invArr != null && invArr.length > 0) {
                    invArr[0] = null;
                }
            } else if (slot != null) {
                try {
                    slot.onSlotChanged();
                } catch (Throwable ignored) {}
            }

            if (inv.knowledge == null) {
                inv.knowledge = new ArrayList<ItemStack>();
            }
            boolean foundK = false;
            for (ItemStack k : inv.knowledge) {
                if (k != null && areKnowledgeStacksEqual(k, copy)) {
                    foundK = true;
                    k.stackSize = 1;
                    break;
                }
            }
            if (!foundK) {
                inv.knowledge.add(copy.copy());
            }

            if (player != null) {
                addKnowledgeSafe(copy, player);
                syncPlayerEMCAndKnowledge(player, getInventoryEmc(inv), copy);
            } else {
                handleUpdateOutputs(inv, true);
            }
        }
    }

    public static boolean isConsumeValid(ItemStack stack, TransmutationInventory inv) {
        if (inv != null && handleInventoryHasMaxedEmc(inv)) return false;
        if (stack == null) return false;
        return doesItemHaveEmc(stack) || isTome(stack.getItem());
    }

    public static void handleLockPutStack(Slot slot, ItemStack stack, TransmutationInventory inv) {
        if (stack == null || inv == null) return;
        ItemStack single = normalizeKnowledgeStack(stack);
        EntityPlayer player = getPlayerFromInventory(inv);
        UUID uuid = player != null ? player.getUniqueID() : null;
        Object lock = uuid != null ? getPlayerLock(uuid) : new Object();

        synchronized (lock) {
            if (slot != null) {
                try {
                    slot.putStack(stack);
                } catch (Throwable ignored) {}
            }
            if (isItemEmc(stack)) {
                IItemEmc itemEmc = (IItemEmc) stack.getItem();
                double stored = getItemStoredEmc(itemEmc, stack);
                handleInventoryAddEmc(inv, stored);
                extractItemEmc(itemEmc, stack, stored);
            }

            if (inv.knowledge == null) {
                inv.knowledge = new ArrayList<ItemStack>();
            }
            boolean foundK = false;
            for (ItemStack k : inv.knowledge) {
                if (k != null && areKnowledgeStacksEqual(k, single)) {
                    foundK = true;
                    k.stackSize = 1;
                    break;
                }
            }
            if (!foundK) {
                inv.knowledge.add(single.copy());
            }

            if (player != null) {
                addKnowledgeSafe(single, player);
                syncPlayerEMCAndKnowledge(player, getInventoryEmc(inv), single.copy());
            } else {
                handleUpdateOutputs(inv, true);
            }
        }
    }

    public static ItemStack handleTransferStackInSlot(TransmutationContainer container, EntityPlayer player, int slotIndex) {
        if (container == null || player == null || slotIndex < 0 || slotIndex >= container.inventorySlots.size()) {
            return null;
        }
        Slot slot = (Slot) container.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack()) {
            return null;
        }
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        TransmutationInventory inv = container.transmutationInventory;
        if (inv == null) {
            return null;
        }

        UUID uuid = player.getUniqueID();
        Object lock = uuid != null ? getPlayerLock(uuid) : new Object();

        synchronized (lock) {
            // Output slots (10..25)
            if (slotIndex >= 10 && slotIndex <= 25) {
                double cost = getEmcValueDouble(copy);
                if (cost <= 0.0) {
                    return null;
                }
                double currentEmc = getInventoryEmc(inv);
                int maxPossible = (int) Math.min(copy.getMaxStackSize(), Math.floor(currentEmc / cost));
                if (maxPossible <= 0) {
                    return null;
                }
                int crafted = 0;
                for (int i = 0; i < maxPossible; i++) {
                    if (getInventoryEmc(inv) < cost) break;
                    ItemStack single = copy.copy();
                    single.stackSize = 1;
                    if (player.inventory.addItemStackToInventory(single)) {
                        removeInventoryEmc(inv, cost);
                        crafted++;
                    } else {
                        break;
                    }
                }
                if (crafted > 0) {
                    syncPlayerEMCAndKnowledge(player, getInventoryEmc(inv), null);
                    if (player.inventoryContainer != null) {
                        try {
                            player.inventoryContainer.detectAndSendChanges();
                        } catch (Throwable ignored) {}
                    }
                }
                return null;
            }

            // Player inventory (26..61) -> Consume item
            if (slotIndex >= 26) {
                double itemEmc = getEmcValueDouble(stack);
                if (itemEmc <= 0.0 && !isTome(stack.getItem())) {
                    return null;
                }

                ItemStack learnedCopy = normalizeKnowledgeStack(stack);
                while (!handleInventoryHasMaxedEmc(inv) && stack.stackSize > 0) {
                    handleInventoryAddEmc(inv, itemEmc);
                    stack.stackSize--;
                }

                if (inv.knowledge == null) {
                    inv.knowledge = new ArrayList<ItemStack>();
                }
                boolean foundK = false;
                for (ItemStack k : inv.knowledge) {
                    if (k != null && areKnowledgeStacksEqual(k, learnedCopy)) {
                        foundK = true;
                        k.stackSize = 1;
                        break;
                    }
                }
                if (!foundK) {
                    inv.knowledge.add(learnedCopy.copy());
                }

                if (stack.stackSize <= 0) {
                    try {
                        slot.putStack(null);
                    } catch (Throwable ignored) {}
                } else {
                    try {
                        slot.onSlotChanged();
                    } catch (Throwable ignored) {}
                }

                addKnowledgeSafe(learnedCopy, player);
                syncPlayerEMCAndKnowledge(player, getInventoryEmc(inv), learnedCopy);
                if (player.inventoryContainer != null) {
                    try {
                        player.inventoryContainer.detectAndSendChanges();
                    } catch (Throwable ignored) {}
                }
                return null;
            }
        }

        return null;
    }

    // --- TileEntity and Item Dynamic EMC Helpers ---

    public static double getTileStoredEmc(Object tile) {
        if (tile == null) return 0.0;
        try {
            for (Method m : tile.getClass().getMethods()) {
                if (m.getName().equals("getStoredEmc") && m.getParameterTypes().length == 0) {
                    Object res = m.invoke(tile);
                    if (res instanceof Number) {
                        return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (tile instanceof IEmcStorage) {
            try {
                return ((IEmcStorage) tile).getStoredEmc();
            } catch (Throwable ignored) {}
        }
        return 0.0;
    }

    public static double getTileMaximumEmc(Object tile) {
        if (tile == null) return 0.0;
        try {
            for (Method m : tile.getClass().getMethods()) {
                if (m.getName().equals("getMaximumEmc") && m.getParameterTypes().length == 0) {
                    Object res = m.invoke(tile);
                    if (res instanceof Number) {
                        return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (tile instanceof IEmcStorage) {
            try {
                return ((IEmcStorage) tile).getMaximumEmc();
            } catch (Throwable ignored) {}
        }
        return Double.MAX_VALUE;
    }

    public static double acceptTileEmc(Object tile, ForgeDirection side, double amount) {
        if (tile == null || amount <= 0.0) return 0.0;
        try {
            for (Method m : tile.getClass().getMethods()) {
                if (m.getName().equals("acceptEMC") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == ForgeDirection.class) {
                    Class<?> p2 = m.getParameterTypes()[1];
                    if (p2 == double.class) {
                        Object res = m.invoke(tile, side, amount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == long.class) {
                        long longAmount = (long) Math.min((double) Long.MAX_VALUE, amount);
                        Object res = m.invoke(tile, side, longAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == int.class) {
                        int intAmount = (int) Math.min((double) Integer.MAX_VALUE, amount);
                        Object res = m.invoke(tile, side, intAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (tile instanceof IEmcAcceptor) {
            try {
                return ((IEmcAcceptor) tile).acceptEMC(side, amount);
            } catch (Throwable ignored) {}
        }
        return 0.0;
    }

    public static double provideTileEmc(Object tile, ForgeDirection side, double amount) {
        if (tile == null || amount <= 0.0) return 0.0;
        try {
            for (Method m : tile.getClass().getMethods()) {
                if (m.getName().equals("provideEMC") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == ForgeDirection.class) {
                    Class<?> p2 = m.getParameterTypes()[1];
                    if (p2 == double.class) {
                        Object res = m.invoke(tile, side, amount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == long.class) {
                        long longAmount = (long) Math.min((double) Long.MAX_VALUE, amount);
                        Object res = m.invoke(tile, side, longAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == int.class) {
                        int intAmount = (int) Math.min((double) Integer.MAX_VALUE, amount);
                        Object res = m.invoke(tile, side, intAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (tile instanceof IEmcProvider) {
            try {
                return ((IEmcProvider) tile).provideEMC(side, amount);
            } catch (Throwable ignored) {}
        }
        return 0.0;
    }

    public static double getItemStoredEmc(IItemEmc item, ItemStack stack) {
        if (item == null || stack == null) return 0.0;
        try {
            for (Method m : item.getClass().getMethods()) {
                if (m.getName().equals("getStoredEmc") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                    Object res = m.invoke(item, stack);
                    if (res instanceof Number) return ((Number) res).doubleValue();
                }
            }
        } catch (Throwable ignored) {}
        try {
            return item.getStoredEmc(stack);
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    public static double getItemMaximumEmc(IItemEmc item, ItemStack stack) {
        if (item == null || stack == null) return 0.0;
        try {
            for (Method m : item.getClass().getMethods()) {
                if (m.getName().equals("getMaximumEmc") && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == ItemStack.class) {
                    Object res = m.invoke(item, stack);
                    if (res instanceof Number) return ((Number) res).doubleValue();
                }
            }
        } catch (Throwable ignored) {}
        try {
            return item.getMaximumEmc(stack);
        } catch (Throwable ignored) {
            return Double.MAX_VALUE;
        }
    }

    public static double extractItemEmc(IItemEmc item, ItemStack stack, double amount) {
        if (item == null || stack == null || amount <= 0.0) return 0.0;
        try {
            for (Method m : item.getClass().getMethods()) {
                if (m.getName().equals("extractEmc") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == ItemStack.class) {
                    Class<?> p2 = m.getParameterTypes()[1];
                    if (p2 == double.class) {
                        Object res = m.invoke(item, stack, amount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == long.class) {
                        long longAmount = (long) Math.min((double) Long.MAX_VALUE, amount);
                        Object res = m.invoke(item, stack, longAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == int.class) {
                        int intAmount = (int) Math.min((double) Integer.MAX_VALUE, amount);
                        Object res = m.invoke(item, stack, intAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        try {
            return item.extractEmc(stack, amount);
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    public static double addItemEmc(IItemEmc item, ItemStack stack, double amount) {
        if (item == null || stack == null || amount <= 0.0) return 0.0;
        try {
            for (Method m : item.getClass().getMethods()) {
                if (m.getName().equals("addEmc") && m.getParameterTypes().length == 2 && m.getParameterTypes()[0] == ItemStack.class) {
                    Class<?> p2 = m.getParameterTypes()[1];
                    if (p2 == double.class) {
                        Object res = m.invoke(item, stack, amount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == long.class) {
                        long longAmount = (long) Math.min((double) Long.MAX_VALUE, amount);
                        Object res = m.invoke(item, stack, longAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    } else if (p2 == int.class) {
                        int intAmount = (int) Math.min((double) Integer.MAX_VALUE, amount);
                        Object res = m.invoke(item, stack, intAmount);
                        if (res instanceof Number) return ((Number) res).doubleValue();
                    }
                }
            }
        } catch (Throwable ignored) {}
        try {
            return item.addEmc(stack, amount);
        } catch (Throwable ignored) {
            return 0.0;
        }
    }

    // --- General Utility Methods ---

    public static String formatEmc(double emc) {
        return FULL_FORMATTER.format(emc);
    }

    public static String formatEmcCompact(double emc) {
        return EMCFormat.formatEmc(emc, false);
    }

    public static String formatEmcSmart(double emc) {
        try {
            if (isShiftKeyDownCached()) {
                return formatEmc(emc);
            }
        } catch (Throwable ignored) {
            // Dedicated server or headless
        }
        return formatEmcCompact(emc);
    }

    public static void dropItem(World world, double x, double y, double z, ItemStack stack) {
        if (world != null && !world.isRemote && stack != null && stack.stackSize > 0) {
            EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
            item.motionX = 0;
            item.motionY = 0;
            item.motionZ = 0;
            item.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(item);
        }
    }

    public static ItemStack fixOutput(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        ItemStack copy = stack.copy();
        if (copy.getItem().isDamageable() && !copy.getItem().getHasSubtypes()) {
            copy.setItemDamage(0);
        }
        return copy;
    }

    public static ItemStack insertStackIntoInventory(IInventory inv, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null || stack.stackSize <= 0) {
            return stack;
        }

        ItemStack remaining = stack.copy();
        int sideOrdinal = side != null ? side.ordinal() : 0;

        if (inv instanceof ISidedInventory) {
            ISidedInventory sided = (ISidedInventory) inv;
            int[] slots = sided.getAccessibleSlotsFromSide(sideOrdinal);
            if (slots != null) {
                for (int slot : slots) {
                    ItemStack inSlot = sided.getStackInSlot(slot);
                    if (inSlot != null && inSlot.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(inSlot, remaining)) {
                        if (sided.canInsertItem(slot, remaining, sideOrdinal)) {
                            int max = Math.min(sided.getInventoryStackLimit(), inSlot.getMaxStackSize());
                            int space = max - inSlot.stackSize;
                            if (space > 0) {
                                int transfer = Math.min(space, remaining.stackSize);
                                inSlot.stackSize += transfer;
                                remaining.stackSize -= transfer;
                                sided.markDirty();
                                if (remaining.stackSize <= 0) {
                                    return null;
                                }
                            }
                        }
                    }
                }

                for (int slot : slots) {
                    ItemStack inSlot = sided.getStackInSlot(slot);
                    if (inSlot == null) {
                        if (sided.canInsertItem(slot, remaining, sideOrdinal)) {
                            int max = Math.min(sided.getInventoryStackLimit(), remaining.getMaxStackSize());
                            int transfer = Math.min(max, remaining.stackSize);
                            ItemStack newStack = remaining.copy();
                            newStack.stackSize = transfer;
                            sided.setInventorySlotContents(slot, newStack);
                            remaining.stackSize -= transfer;
                            sided.markDirty();
                            if (remaining.stackSize <= 0) {
                                return null;
                            }
                        }
                    }
                }
            }
        } else {
            int size = inv.getSizeInventory();

            for (int i = 0; i < size; i++) {
                if (!inv.isItemValidForSlot(i, remaining)) continue;
                ItemStack inSlot = inv.getStackInSlot(i);
                if (inSlot != null && inSlot.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(inSlot, remaining)) {
                    int max = Math.min(inv.getInventoryStackLimit(), inSlot.getMaxStackSize());
                    int space = max - inSlot.stackSize;
                    if (space > 0) {
                        int transfer = Math.min(space, remaining.stackSize);
                        inSlot.stackSize += transfer;
                        remaining.stackSize -= transfer;
                        inv.markDirty();
                        if (remaining.stackSize <= 0) {
                            return null;
                        }
                    }
                }
            }

            for (int i = 0; i < size; i++) {
                if (!inv.isItemValidForSlot(i, remaining)) continue;
                ItemStack inSlot = inv.getStackInSlot(i);
                if (inSlot == null) {
                    int max = Math.min(inv.getInventoryStackLimit(), remaining.getMaxStackSize());
                    int transfer = Math.min(max, remaining.stackSize);
                    ItemStack newStack = remaining.copy();
                    newStack.stackSize = transfer;
                    inv.setInventorySlotContents(i, newStack);
                    remaining.stackSize -= transfer;
                    inv.markDirty();
                    if (remaining.stackSize <= 0) {
                        return null;
                    }
                }
            }
        }

        return remaining.stackSize > 0 ? remaining : null;
    }

    public static boolean isStackFuelSafe(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        try {
            return FuelMapper.isStackFuel(stack);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isItemEmc(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        try {
            return stack.getItem() instanceof IItemEmc;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isTome(net.minecraft.item.Item item) {
        if (item == null) return false;
        try {
            return item == ObjHandler.tome;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean shouldDupeWithNBTSafe(ItemStack stack) {
        if (stack == null) return false;
        try {
            return NBTWhitelist.shouldDupeWithNBT(stack);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void sanitizeEmcMapper() {
        try {
            for (Field f : moze_intel.projecte.emc.EMCMapper.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Map map = (Map) f.get(null);
                    if (map != null && !map.isEmpty()) {
                        boolean hasLong = false;
                        boolean hasInt = false;
                        for (Object val : map.values()) {
                            if (val instanceof Long) hasLong = true;
                            if (val instanceof Integer) hasInt = true;
                        }
                        if (hasLong && hasInt) {
                            for (Map.Entry<?, ?> entry : new ArrayList<Map.Entry<?, ?>>(map.entrySet())) {
                                if (entry.getValue() instanceof Integer) {
                                    ((Map) map).put(entry.getKey(), Long.valueOf(((Integer) entry.getValue()).longValue()));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void ensureEmcRegistered(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return;
        try {
            double value = getEmcValueDouble(stack);
            if (value > 0.0) {
                for (Field f : moze_intel.projecte.emc.EMCMapper.class.getDeclaredFields()) {
                    if (Map.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Map map = (Map) f.get(null);
                        if (map != null) {
                            boolean isLongMap = false;
                            for (Object val : map.values()) {
                                if (val instanceof Long) {
                                    isLongMap = true;
                                    break;
                                }
                            }
                            moze_intel.projecte.emc.SimpleStack ss = new moze_intel.projecte.emc.SimpleStack(stack);
                            if (!map.containsKey(ss)) {
                                if (isLongMap) {
                                    map.put(ss, (long) Math.min((double) Long.MAX_VALUE, value));
                                } else {
                                    map.put(ss, (int) Math.min((double) Integer.MAX_VALUE, value));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void addKnowledgeSafe(ItemStack stack, EntityPlayer player) {
        if (stack == null || player == null) return;
        ItemStack single = normalizeKnowledgeStack(stack);
        ensureEmcRegistered(single);
        try {
            Transmutation.addKnowledge(single, player);
        } catch (Throwable ignored) {}
        try {
            moze_intel.projecte.playerData.TransmutationProps props = moze_intel.projecte.playerData.TransmutationProps.getDataFor(player);
            if (props != null) {
                for (Field f : props.getClass().getDeclaredFields()) {
                    if (List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        List list = (List) f.get(props);
                        if (list == null) {
                            list = new ArrayList();
                            f.set(props, list);
                        }
                        boolean found = false;
                        for (Object obj : list) {
                            if (obj instanceof ItemStack) {
                                ItemStack is = (ItemStack) obj;
                                if (areKnowledgeStacksEqual(is, single)) {
                                    found = true;
                                    is.stackSize = 1;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            list.add(single.copy());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
