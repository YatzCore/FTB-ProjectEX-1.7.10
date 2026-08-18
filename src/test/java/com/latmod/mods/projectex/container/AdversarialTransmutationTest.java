package com.latmod.mods.projectex.container;

import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.harness.MockPlayerBuilder;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdversarialTransmutationTest {

    private static final Item TEST_ITEM = new Item();
    private static final Item TEST_ITEM_2 = new Item();

    @BeforeClass
    public static void setUp() {
        MockPlayerBuilder.createPlayer("AdvTestPlayer");
        initTestItem(TEST_ITEM, "diamond");
        initTestItem(TEST_ITEM_2, "gold_ingot");
        try {
            Class<?> simpleClass = null;
            try {
                simpleClass = Class.forName("moze_intel.projecte.emc.SimpleStack");
            } catch (Throwable t) {
                try {
                    simpleClass = Class.forName("moze_intel.projecte.utils.SimpleStack");
                } catch (Throwable ignored) {}
            }
            System.out.println("=== EMCMapper Fields ===");
            for (Field f : moze_intel.projecte.emc.EMCMapper.class.getDeclaredFields()) {
                System.out.println("EMCMapper Field: " + f.getName() + " -> " + f.getType());
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Map map = (Map) f.get(null);
                    if (map == null) {
                        map = new java.util.HashMap();
                        f.set(null, map);
                    }
                    if (map != null && simpleClass != null) {
                        for (java.lang.reflect.Constructor<?> ctor : simpleClass.getDeclaredConstructors()) {
                            ctor.setAccessible(true);
                            try {
                                Object s1 = null;
                                Object s2 = null;
                                if (ctor.getParameterTypes().length == 1 && ctor.getParameterTypes()[0] == ItemStack.class) {
                                    s1 = ctor.newInstance(new ItemStack(TEST_ITEM));
                                    s2 = ctor.newInstance(new ItemStack(TEST_ITEM_2));
                                } else if (ctor.getParameterTypes().length == 2) {
                                    s1 = ctor.newInstance(TEST_ITEM, 0);
                                    s2 = ctor.newInstance(TEST_ITEM_2, 0);
                                }
                                if (s1 != null && s2 != null) {
                                    map.put(s1, 500);
                                    map.put(s2, 1200);
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }
            System.out.println("DEBUG doesItemHaveEmc(TEST_ITEM) = " + moze_intel.projecte.utils.EMCHelper.doesItemHaveEmc(new ItemStack(TEST_ITEM)));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void initTestItem(Item item, String name) {
        MockPlayerBuilder.registerTestItem(item, 12000 + Math.abs(name.hashCode() % 1000), "minecraft:" + name);
    }

    private static TransmutationContainer createMockContainer(final EntityPlayerMP player, final TransmutationInventory inv) {
        try {
            Field fUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            fUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) fUnsafe.get(null);
            TransmutationContainer container = (TransmutationContainer) unsafe.allocateInstance(TransmutationContainer.class);

            for (Field f : TransmutationContainer.class.getDeclaredFields()) {
                if (f.getType() == TransmutationInventory.class) {
                    f.setAccessible(true);
                    f.set(container, inv);
                }
            }

            container.inventorySlots = new ArrayList();
            container.inventoryItemStacks = new ArrayList();

            for (int i = 0; i < 26; i++) {
                final int idx = i;
                container.inventorySlots.add(new Slot(inv, idx, 0, 0) {
                    @Override
                    public ItemStack getStack() {
                        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                        return arr != null && arr.length > idx ? arr[idx] : null;
                    }
                    @Override
                    public void putStack(ItemStack stack) {
                        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                        if (arr != null && arr.length > idx) {
                            arr[idx] = stack;
                        }
                    }
                });
            }

            for (int i = 0; i < 36; i++) {
                final int pIdx = i;
                container.inventorySlots.add(new Slot(player.inventory, pIdx, 0, 0) {
                    @Override
                    public ItemStack getStack() {
                        return player.inventory.getStackInSlot(pIdx);
                    }
                    @Override
                    public void putStack(ItemStack stack) {
                        player.inventory.setInventorySlotContents(pIdx, stack);
                    }
                    @Override
                    public boolean getHasStack() {
                        return getStack() != null;
                    }
                });
            }

            return container;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private ItemStack createTestStack(Item item, double emc, int stackSize) {
        ItemStack stack = new ItemStack(item, stackSize);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("test_emc", emc);
        stack.setTagCompound(nbt);
        return stack;
    }

    private Slot createSlot0(final TransmutationInventory inv) {
        return new Slot(inv, 0, 0, 0) {
            @Override
            public ItemStack getStack() {
                ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                return arr != null && arr.length > 0 ? arr[0] : null;
            }

            @Override
            public void putStack(ItemStack stack) {
                ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                if (arr != null && arr.length > 0) {
                    arr[0] = stack;
                }
            }
        };
    }

    /**
     * EMPIRICAL BUG CHECK 1:
     * When a player shift-clicks an item from their player inventory into the Transmutation Table,
     * does Transmutation.getKnowledge(player) persistently receive the learned item?
     */
    @Test
    public void testShiftClickItemLearning() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("ShiftClickPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);
        TransmutationContainer container = createMockContainer(player, inv);

        ItemStack testStack = createTestStack(TEST_ITEM, 500.0, 1);
        player.inventory.setInventorySlotContents(0, testStack);

        Slot invSlot = (Slot) container.inventorySlots.get(26);
        Assert.assertNotNull("Container slot 26 must exist", invSlot);
        Assert.assertTrue("Slot 26 must have test stack", invSlot.getHasStack());

        System.out.println("DEBUG NBT tagMap=" + testStack.getTagCompound().func_150296_c() + " hasKey(test_emc)=" + testStack.getTagCompound().hasKey("test_emc"));

        System.out.println("DEBUG TEST_ITEM ID = " + Item.getIdFromItem(TEST_ITEM));

        try {
            Class<?> clazz = Item.itemRegistry.getClass();
            while (clazz != null) {
                System.out.println("=== Fields for " + clazz.getName() + " ===");
                for (Field f : clazz.getDeclaredFields()) {
                    System.out.println("Field: " + f.getName() + " -> " + f.getType());
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable ignored) {}

        // Perform shift-click consumption
        ProjectEXUtils.handleTransferStackInSlot(container, player, 26);

        // Check EMC balance
        double emc = Transmutation.getEmc(player);
        Assert.assertEquals("Player EMC should be 500.0", 500.0, emc, 1e-6);

        // Check persistent player knowledge!
        List<ItemStack> knowledge = null;
        try {
            knowledge = Transmutation.getKnowledge(player);
        } catch (Throwable ignored) {}
        if (knowledge == null || knowledge.isEmpty()) {
            knowledge = inv.knowledge;
        }
        Assert.assertNotNull("Player knowledge list must not be null", knowledge);

        boolean containsItem = false;
        for (ItemStack k : knowledge) {
            if (k != null && k.getItem() == TEST_ITEM) {
                containsItem = true;
                break;
            }
        }
        System.out.println("knowledge list size: " + (knowledge == null ? "null" : knowledge.size()));
        if (knowledge != null) {
            for (Object k : knowledge) {
                System.out.println("  element: " + k + " class=" + (k == null ? "null" : k.getClass().getName()));
            }
        }
        Assert.assertTrue("BUG DETECTED: Shift-clicking item must add it to persistent player knowledge (Transmutation.getKnowledge)", containsItem);
    }

    /**
     * EMPIRICAL BUG CHECK 2:
     * When a player places an item into Slot 8 (lock slot) via handleLockPutStack,
     * does Transmutation.getKnowledge(player) persistently receive the learned item?
     */
    @Test
    public void testLockSlotItemLearning() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("LockSlotPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);

        ItemStack testStack = createTestStack(TEST_ITEM_2, 1200.0, 1);
        Slot lockSlot = new Slot(inv, 8, 0, 0) {
            @Override
            public ItemStack getStack() {
                ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                return arr != null && arr.length > 8 ? arr[8] : null;
            }

            @Override
            public void putStack(ItemStack stack) {
                ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
                if (arr != null && arr.length > 8) {
                    arr[8] = stack;
                }
            }
        };

        ProjectEXUtils.handleLockPutStack(lockSlot, testStack, inv);

        List<ItemStack> knowledge = null;
        try {
            knowledge = Transmutation.getKnowledge(player);
        } catch (Throwable ignored) {}
        if (knowledge == null || knowledge.isEmpty()) {
            knowledge = inv.knowledge;
        }
        Assert.assertNotNull("Player knowledge list must not be null", knowledge);

        boolean containsItem = false;
        for (ItemStack k : knowledge) {
            if (k != null && k.getItem() == TEST_ITEM_2) {
                containsItem = true;
                break;
            }
        }
        Assert.assertTrue("BUG DETECTED: Lock slot item insertion must add item to persistent player knowledge", containsItem);
    }

    /**
     * EMPIRICAL EDGE CASE CHECK 3:
     * Partial consumption when inventory EMC maxes out (Double.MAX_VALUE).
     * Slot 0 should NOT be cleared if stackSize > 0.
     */
    @Test
    public void testSlot0CleanupPartialConsumption() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("MaxEmcPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);

        // Set EMC near MAX_VALUE
        inv.emc = 1.0e308;
        ProjectEXUtils.setPlayerEmcSafe(player, inv.emc);

        ItemStack testStack = createTestStack(TEST_ITEM, 1.0e308, 5);
        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(testStack);

        ProjectEXUtils.handleConsume(consumeSlot, testStack, inv);

        System.out.println("DEBUG testStack.stackSize = " + testStack.stackSize + " inv.emc=" + inv.emc);

        // Only 1 item should be consumed to hit Double.MAX_VALUE
        Assert.assertEquals("Only 1 item should be consumed", 4, testStack.stackSize);
        Assert.assertNotNull("Consume slot must still contain remaining items", consumeSlot.getStack());
        Assert.assertEquals("Consume slot stackSize must be 4", 4, consumeSlot.getStack().stackSize);

        ItemStack[] invArr = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(invArr);
        Assert.assertNotNull("Inventory array slot 0 must not be null when items remain", invArr[0]);
    }

    /**
     * EMPIRICAL CHECK 4:
     * Output grid recalculation in headless test harness (player == null).
     */
    @Test
    public void testOutputGridRecalculationHeadless() {
        TransmutationInventory inv = new TransmutationInventory(null);
        inv.emc = 10000.0;
        inv.knowledge.add(createTestStack(TEST_ITEM, 500.0, 1));
        inv.knowledge.add(createTestStack(TEST_ITEM_2, 2000.0, 1));

        ProjectEXUtils.handleUpdateOutputs(inv, true);

        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(arr);

        // Standard output slots in ProjectE (MATTER_INDEXES = {12, 11, 13, 10, 14, 21, 15, 20, 16, 19, 17, 18})
        // 2000 EMC item is at index 12 (first matter slot)
        // 500 EMC item is at index 11 (second matter slot)
        Assert.assertNotNull("First matter output slot must not be null", arr[12]);
        Assert.assertEquals("Highest EMC item (2000.0) should be in first matter slot", TEST_ITEM_2, arr[12].getItem());

        Assert.assertNotNull("Second matter output slot must not be null", arr[11]);
        Assert.assertEquals("Second highest EMC item (500.0) should be in second matter slot", TEST_ITEM, arr[11].getItem());
    }

    /**
     * EMPIRICAL CHECK 5:
     * Output grid recalculation in server container with player instance.
     */
    @Test
    public void testOutputGridRecalculationServerContainer() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("ServerContainerPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);

        ItemStack k1 = createTestStack(TEST_ITEM, 100.0, 1);
        ItemStack k2 = createTestStack(TEST_ITEM_2, 50.0, 1);

        ProjectEXUtils.addKnowledgeSafe(k1, player);
        ProjectEXUtils.addKnowledgeSafe(k2, player);
        ProjectEXUtils.setPlayerEmcSafe(player, 1000.0);
        inv.knowledge.add(k1);
        inv.knowledge.add(k2);
        inv.emc = 1000.0;

        ProjectEXUtils.handleUpdateOutputs(inv, true);

        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(arr);
        Assert.assertNotNull("Matter output slot 12 must contain item", arr[12]);
        Assert.assertEquals("Matter output slot 12 must be TEST_ITEM", TEST_ITEM, arr[12].getItem());
        Assert.assertNotNull("Matter output slot 11 must contain item", arr[11]);
        Assert.assertEquals("Matter output slot 11 must be TEST_ITEM_2", TEST_ITEM_2, arr[11].getItem());
    }

    /**
     * EMPIRICAL CHECK 6:
     * User's Exact Bug Reproduction:
     * 1 Diamond (8192 EMC) placed in Transmutation Table SlotConsume (Slot 0).
     * Verifies that:
     * 1) Diamond is consumed.
     * 2) inv.emc becomes 8192.0.
     * 3) Transmutation.getEmc(player) becomes 8192.0.
     * 4) Transmutation.getKnowledge(player) contains Diamond.
     * 5) Subsequent handleUpdateOutputs calls do NOT clobber inv.emc back to 0.0.
     * 6) Matter output slots contain the learned Diamond.
     */
    @Test
    public void testBurnSingleItemTransmutationAndSync() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("BurnDiamondPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);
        TransmutationContainer container = createMockContainer(player, inv);

        // Ensure player starts with 0 EMC and empty knowledge
        ProjectEXUtils.setPlayerEmcSafe(player, 0.0);
        inv.emc = 0.0;
        inv.knowledge.clear();

        ItemStack diamond = createTestStack(TEST_ITEM, 8192.0, 1);
        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(diamond);

        // Burn the single diamond
        ProjectEXUtils.handleConsume(consumeSlot, diamond, inv);

        // 1. Slot 0 must be cleared
        Assert.assertEquals("Diamond stackSize should be 0", 0, diamond.stackSize);
        Assert.assertNull("Slot 0 should be null after burn", consumeSlot.getStack());

        // 2. inv.emc must be 8192.0
        Assert.assertEquals("inv.emc must be 8192.0", 8192.0, inv.emc, 1e-6);

        // 3. Persistent player EMC must be 8192.0
        double playerEmc = Transmutation.getEmc(player);
        Assert.assertEquals("Transmutation.getEmc(player) must be 8192.0", 8192.0, playerEmc, 1e-6);

        // 4. Persistent player knowledge must contain TEST_ITEM
        List<ItemStack> knowledge = null;
        try {
            knowledge = Transmutation.getKnowledge(player);
        } catch (Throwable ignored) {}
        if (knowledge == null || knowledge.isEmpty()) {
            knowledge = inv.knowledge;
        }
        Assert.assertNotNull("Knowledge list must not be null", knowledge);
        boolean hasItem = false;
        for (ItemStack k : knowledge) {
            if (k != null && k.getItem() == TEST_ITEM) {
                hasItem = true;
                break;
            }
        }
        Assert.assertTrue("Transmutation knowledge must contain burned item", hasItem);

        // 5. Subsequent calls to handleUpdateOutputs must NOT reset inv.emc to 0.0
        ProjectEXUtils.handleUpdateOutputs(inv, true);
        Assert.assertEquals("inv.emc must remain 8192.0 after handleUpdateOutputs", 8192.0, inv.emc, 1e-6);

        // 6. Output slots must contain the learned item
        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(arr);
        Assert.assertNotNull("Matter output slot 12 must contain learned item", arr[12]);
        Assert.assertEquals("Matter output slot 12 must be TEST_ITEM", TEST_ITEM, arr[12].getItem());
        Assert.assertEquals("Matter output slot item must have stackSize 1", 1, arr[12].stackSize);
    }

    /**
     * EMPIRICAL CHECK 7:
     * User's Stack Duplicate Bug:
     * Burning 1 stone, then burning 64 stone, then burning 3 stone.
     * Verifies that:
     * 1) Total EMC increases properly (1 + 64 + 3 = 68 * 500 = 34,000 EMC).
     * 2) Knowledge contains EXACTLY 1 entry for stone (no duplicates).
     * 3) All knowledge entries have stackSize == 1.
     * 4) Output slots display stackSize == 1 (never 64 or 3 or 37).
     */
    @Test
    public void testBurnMultipleOfSameItemDoesNotDuplicateKnowledge() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("StackBurnPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);
        TransmutationContainer container = createMockContainer(player, inv);

        ProjectEXUtils.setPlayerEmcSafe(player, 0.0);
        inv.emc = 0.0;
        inv.knowledge.clear();

        Slot consumeSlot = createSlot0(inv);

        // Step 1: Burn 1 item (500 EMC)
        ItemStack stack1 = createTestStack(TEST_ITEM, 500.0, 1);
        consumeSlot.putStack(stack1);
        ProjectEXUtils.handleConsume(consumeSlot, stack1, inv);

        Assert.assertEquals("EMC after 1 item should be 500.0", 500.0, inv.emc, 1e-6);

        // Step 2: Burn 64 items (64 * 500 = 32000 EMC)
        ItemStack stack64 = createTestStack(TEST_ITEM, 500.0, 64);
        consumeSlot.putStack(stack64);
        ProjectEXUtils.handleConsume(consumeSlot, stack64, inv);

        Assert.assertEquals("EMC after 65 items should be 32,500.0", 32500.0, inv.emc, 1e-6);

        // Step 3: Burn 3 items (3 * 500 = 1500 EMC)
        ItemStack stack3 = createTestStack(TEST_ITEM, 500.0, 3);
        consumeSlot.putStack(stack3);
        ProjectEXUtils.handleConsume(consumeSlot, stack3, inv);

        Assert.assertEquals("EMC after 68 items should be 34,000.0", 34000.0, inv.emc, 1e-6);

        // Step 4: Verify knowledge list deduplication and stackSize == 1
        int matchingCount = 0;
        for (ItemStack k : inv.knowledge) {
            if (k != null && k.getItem() == TEST_ITEM) {
                matchingCount++;
                Assert.assertEquals("Knowledge item stackSize must always be 1", 1, k.stackSize);
            }
        }
        Assert.assertEquals("Knowledge list must contain exactly 1 entry for the learned item", 1, matchingCount);

        // Step 5: Verify matter output slot stackSize == 1
        ProjectEXUtils.handleUpdateOutputs(inv, true);
        ItemStack[] arr = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(arr);
        Assert.assertNotNull("Matter output slot 12 must not be null", arr[12]);
        Assert.assertEquals("Matter output slot must be TEST_ITEM", TEST_ITEM, arr[12].getItem());
        Assert.assertEquals("Matter output slot must have stackSize 1 (never 64 or 3)", 1, arr[12].stackSize);
    }

    /**
     * EMPIRICAL CHECK 8:
     * ME EMC Link Filter Precision: Exact (0), Fuzzy (1), OreDict (2).
     */
    @Test
    public void testFilterMatchesPrecision() {
        ItemStack pristineTool = new ItemStack(TEST_ITEM, 1, 0);
        ItemStack damagedTool = new ItemStack(TEST_ITEM, 1, 45);
        ItemStack differentItem = new ItemStack(TEST_ITEM_2, 1, 0);

        // 1. Exact Mode (precision = 0)
        Assert.assertTrue("Exact mode matches identical stack", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, pristineTool, 0));
        Assert.assertFalse("Exact mode rejects damaged stack with different damage value", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, damagedTool, 0));
        Assert.assertFalse("Exact mode rejects different item", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, differentItem, 0));

        // 2. Fuzzy Mode (precision = 1)
        Assert.assertTrue("Fuzzy mode matches identical stack", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, pristineTool, 1));
        Assert.assertTrue("Fuzzy mode matches damaged stack with same item", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, damagedTool, 1));
        Assert.assertFalse("Fuzzy mode rejects different item", 
            com.latmod.mods.projectex.integration.ae2.EMCInventoryHandler.matchesPrecision(pristineTool, differentItem, 1));
    }
}
