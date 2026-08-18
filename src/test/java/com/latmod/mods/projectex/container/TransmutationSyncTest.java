package com.latmod.mods.projectex.container;

import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.harness.MockPlayerBuilder;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.playerData.Transmutation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransmutationSyncTest {

    private static final Item TEST_ITEM = new Item();

    @BeforeClass
    public static void setUp() {
        MockPlayerBuilder.createPlayer("TestPlayer");
        try {
            System.out.println("=== TransmutationContainer Fields ===");
            for (java.lang.reflect.Field f : moze_intel.projecte.gameObjs.container.TransmutationContainer.class.getDeclaredFields()) {
                System.out.println("TC Field: " + f.getName() + " -> " + f.getType());
            }
            System.out.println("=== KnowledgeSyncPKT classes ===");
            for (Class<?> c : moze_intel.projecte.network.packets.KnowledgeSyncPKT.class.getDeclaredClasses()) {
                System.out.println("Nested class: " + c.getName());
            }
            for (java.lang.reflect.Method m : moze_intel.projecte.network.packets.KnowledgeSyncPKT.class.getDeclaredMethods()) {
                System.out.println("KnowledgeSyncPKT method: " + m.getName() + " desc: " + m);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private ItemStack createTestStack(double emc, int stackSize) {
        ItemStack stack = new ItemStack(TEST_ITEM, stackSize);
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

    @Test
    public void testHandleConsumeSingleItem() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("ConsumePlayer1");
        TransmutationInventory inv = new TransmutationInventory(player);

        double emcValue = 1000.0;
        ItemStack testItem = createTestStack(emcValue, 1);
        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(testItem);

        Assert.assertTrue("Slot 0 must have stack", consumeSlot.getHasStack());

        ProjectEXUtils.handleConsume(consumeSlot, testItem, inv);

        Assert.assertFalse("Consume slot must be empty after consumption", consumeSlot.getHasStack());
        Assert.assertNull("Consume slot stack must be null", consumeSlot.getStack());

        ItemStack[] invArray = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(invArray);
        Assert.assertNull("Inventory array slot 0 must be null", invArray[0]);

        double playerEmc = Transmutation.getEmc(player);
        Assert.assertEquals("Player EMC balance must match consumed item EMC", emcValue, playerEmc, 1e-6);
    }

    @Test
    public void testHandleConsumeFullStack() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("ConsumePlayer64");
        TransmutationInventory inv = new TransmutationInventory(player);

        double singleEmc = 500.0;
        ItemStack testStack = createTestStack(singleEmc, 64);

        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(testStack);

        ProjectEXUtils.handleConsume(consumeSlot, testStack, inv);

        Assert.assertFalse("Consume slot must be empty after full stack consumption", consumeSlot.getHasStack());
        Assert.assertEquals("Stack size must be 0", 0, testStack.stackSize);
        Assert.assertNull("Consume slot stack must be null", consumeSlot.getStack());

        double playerEmc = Transmutation.getEmc(player);
        Assert.assertEquals("Player EMC balance must equal 64 * singleEmc", singleEmc * 64.0, playerEmc, 1e-6);
    }

    @Test
    public void testItemLearningUnlearnedVsLearned() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("LearnerPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);

        double emcValue = 8192.0;
        ItemStack testItem = createTestStack(emcValue, 1);

        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(testItem);

        ProjectEXUtils.handleConsume(consumeSlot, testItem, inv);

        Assert.assertEquals("Player EMC must match consumed item EMC", emcValue, Transmutation.getEmc(player), 1e-6);
    }

    @Test
    public void testSlotZeroCleanupNullCheck() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("CleanupPlayer");
        TransmutationInventory inv = new TransmutationInventory(player);

        ItemStack testItem = createTestStack(256.0, 1);
        Slot consumeSlot = createSlot0(inv);
        consumeSlot.putStack(testItem);

        ProjectEXUtils.handleConsume(consumeSlot, testItem, inv);

        ItemStack[] invArray = ProjectEXUtils.getInventoryArray(inv);
        Assert.assertNotNull(invArray);
        Assert.assertNull("Slot 0 in internal inventory array must be explicitly set to null", invArray[0]);
    }
}
