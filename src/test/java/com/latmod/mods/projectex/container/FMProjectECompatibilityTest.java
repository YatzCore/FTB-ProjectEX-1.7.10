package com.latmod.mods.projectex.container;

import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.harness.MockPlayerBuilder;
import com.latmod.mods.projectex.item.*;
import com.latmod.mods.projectex.tile.TileLink;
import com.latmod.mods.projectex.tile.TileRelay;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;

public class FMProjectECompatibilityTest {

    @BeforeClass
    public static void setUp() {
        MockPlayerBuilder.createPlayer("FMCompatTestPlayer");
    }

    // Mock tile entity implementing FMProjectE style methods (long descriptors)
    public static class MockFMTile {
        public long stored = 5000000000L;
        public long max = 10000000000L;

        public long getStoredEmc() {
            return stored;
        }

        public long getMaximumEmc() {
            return max;
        }

        public long acceptEMC(ForgeDirection side, long amount) {
            long accepted = Math.min(amount, max - stored);
            stored += accepted;
            return accepted;
        }

        public long provideEMC(ForgeDirection side, long amount) {
            long provided = Math.min(amount, stored);
            stored -= provided;
            return provided;
        }
    }

    // Mock item implementing FMProjectE style methods (long descriptors)
    public static class MockFMItem implements IItemEmc {
        public long stored = 2000000000L;

        @Override
        public double addEmc(ItemStack stack, double amount) {
            stored += (long) amount;
            return amount;
        }

        public long addEmc(ItemStack stack, long amount) {
            stored += amount;
            return amount;
        }

        @Override
        public double extractEmc(ItemStack stack, double amount) {
            long toRemove = Math.min((long) amount, stored);
            stored -= toRemove;
            return (double) toRemove;
        }

        public long extractEmc(ItemStack stack, long amount) {
            long toRemove = Math.min(amount, stored);
            stored -= toRemove;
            return toRemove;
        }

        @Override
        public double getStoredEmc(ItemStack stack) {
            return (double) stored;
        }

        public long getStoredEmcLong(ItemStack stack) {
            return stored;
        }

        @Override
        public double getMaximumEmc(ItemStack stack) {
            return 10000000000.0;
        }
    }

    @Test
    public void testTileDynamicEMCHelpersWithLongSignatures() {
        MockFMTile tile = new MockFMTile();

        double stored = ProjectEXUtils.getTileStoredEmc(tile);
        Assert.assertEquals("getTileStoredEmc must correctly read long stored EMC", 5000000000.0, stored, 1e-6);

        double max = ProjectEXUtils.getTileMaximumEmc(tile);
        Assert.assertEquals("getTileMaximumEmc must correctly read long max EMC", 10000000000.0, max, 1e-6);

        double accepted = ProjectEXUtils.acceptTileEmc(tile, ForgeDirection.UP, 1000000000.0);
        Assert.assertEquals("acceptTileEmc must accept 1B EMC via long method", 1000000000.0, accepted, 1e-6);
        Assert.assertEquals("Tile stored EMC must now be 6B", 6000000000.0, ProjectEXUtils.getTileStoredEmc(tile), 1e-6);

        double provided = ProjectEXUtils.provideTileEmc(tile, ForgeDirection.DOWN, 2000000000.0);
        Assert.assertEquals("provideTileEmc must provide 2B EMC via long method", 2000000000.0, provided, 1e-6);
        Assert.assertEquals("Tile stored EMC must now be 4B", 4000000000.0, ProjectEXUtils.getTileStoredEmc(tile), 1e-6);
    }

    @Test
    public void testItemDynamicEMCHelpersWithLongSignatures() {
        MockFMItem item = new MockFMItem();
        ItemStack stack = new ItemStack(new net.minecraft.item.Item());

        double stored = ProjectEXUtils.getItemStoredEmc(item, stack);
        Assert.assertEquals("getItemStoredEmc must read 2B stored EMC", 2000000000.0, stored, 1e-6);

        double added = ProjectEXUtils.addItemEmc(item, stack, 1500000000.0);
        Assert.assertEquals("addItemEmc must add 1.5B EMC", 1500000000.0, added, 1e-6);
        Assert.assertEquals("Stored EMC must now be 3.5B", 3500000000.0, ProjectEXUtils.getItemStoredEmc(item, stack), 1e-6);

        double extracted = ProjectEXUtils.extractItemEmc(item, stack, 1000000000.0);
        Assert.assertEquals("extractItemEmc must extract 1B EMC", 1000000000.0, extracted, 1e-6);
        Assert.assertEquals("Stored EMC must now be 2.5B", 2500000000.0, ProjectEXUtils.getItemStoredEmc(item, stack), 1e-6);
    }

    @Test
    public void testTileLinkAndRelayLongOverloads() throws Exception {
        TileLink link = new TileLink();
        long acceptedLink = link.acceptEMC(ForgeDirection.NORTH, 5000000000L);
        Assert.assertEquals("TileLink must accept 5B EMC via long overload", 5000000000L, acceptedLink);
        Assert.assertEquals("TileLink stored EMC must be 5B", 5000000000.0, link.getStoredEmc(), 1e-6);

        TileRelay relay = new TileRelay();
        long acceptedRelay = relay.acceptEMC(ForgeDirection.SOUTH, 100000L);
        Assert.assertEquals("TileRelay must accept EMC via long overload", 100000L, acceptedRelay);

        long providedRelay = relay.provideEMC(ForgeDirection.SOUTH, 50000L);
        Assert.assertEquals("TileRelay must provide EMC via long overload", 50000L, providedRelay);
    }

    @Test
    public void testStarItemsLongOverloads() throws Exception {
        java.lang.reflect.Field fUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        fUnsafe.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) fUnsafe.get(null);

        ItemStar star = (ItemStar) unsafe.allocateInstance(ItemStar.class);
        ItemMagnumStar magnum = (ItemMagnumStar) unsafe.allocateInstance(ItemMagnumStar.class);
        ItemColossalStar colossal = (ItemColossalStar) unsafe.allocateInstance(ItemColossalStar.class);
        ItemFinalStar finalStar = (ItemFinalStar) unsafe.allocateInstance(ItemFinalStar.class);

        java.lang.reflect.Field fDelegate = Item.class.getDeclaredField("delegate");
        fDelegate.setAccessible(true);
        fDelegate.set(star, new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(star, Item.class));
        fDelegate.set(magnum, new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(magnum, Item.class));
        fDelegate.set(colossal, new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(colossal, Item.class));
        fDelegate.set(finalStar, new cpw.mods.fml.common.registry.RegistryDelegate.Delegate<Item>(finalStar, Item.class));

        ItemStack s1 = new ItemStack(star, 1, 0);
        star.addEmc(s1, 100000L);
        Assert.assertEquals("ItemStar must store 100k EMC", 100000.0, star.getStoredEmc(s1), 1e-6);
        star.extractEmc(s1, 50000L);
        Assert.assertEquals("ItemStar must have 50k EMC left", 50000.0, star.getStoredEmc(s1), 1e-6);

        ItemStack s2 = new ItemStack(magnum, 1, 0);
        magnum.addEmc(s2, 5000000L);
        Assert.assertEquals("ItemMagnumStar must store 5M EMC", 5000000.0, magnum.getStoredEmc(s2), 1e-6);
        magnum.extractEmc(s2, 2000000L);
        Assert.assertEquals("ItemMagnumStar must have 3M EMC left", 3000000.0, magnum.getStoredEmc(s2), 1e-6);

        ItemStack s3 = new ItemStack(colossal, 1, 0);
        colossal.addEmc(s3, 5000000000L);
        Assert.assertEquals("ItemColossalStar must store 5B EMC", 5000000000.0, colossal.getStoredEmc(s3), 1e-6);
        colossal.extractEmc(s3, 2000000000L);
        Assert.assertEquals("ItemColossalStar must have 3B EMC left", 3000000000.0, colossal.getStoredEmc(s3), 1e-6);

        ItemStack s4 = new ItemStack(finalStar);
        finalStar.addEmc(s4, 9000000000000000L);
        Assert.assertEquals("ItemFinalStar must store 9P EMC", 9000000000000000.0, finalStar.getStoredEmc(s4), 1e-6);
        finalStar.extractEmc(s4, 4000000000000000L);
        Assert.assertEquals("ItemFinalStar must have 5P EMC left", 5000000000000000.0, finalStar.getStoredEmc(s4), 1e-6);
    }

    @Test
    public void testPlayerEMCSafeLargeValues() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("FMPlayerLarge");

        double targetEmc = 8500000000.0; // 8.5 Billion (exceeds int32)
        ProjectEXUtils.setPlayerEmcSafe(player, targetEmc);

        double readEmc = ProjectEXUtils.getPlayerEmcSafe(player);
        Assert.assertEquals("Player EMC must be accurately set and retrieved for 64-bit value", targetEmc, readEmc, 1e-6);
    }

    @Test
    public void testTransmutationInventoryHelpers() {
        EntityPlayerMP player = MockPlayerBuilder.createPlayer("FMPlayerInv");
        TransmutationInventory inv = new TransmutationInventory(player);

        ProjectEXUtils.setInventoryEmc(inv, 5000000000.0);
        Assert.assertEquals("Inventory EMC must be 5B", 5000000000.0, ProjectEXUtils.getInventoryEmc(inv), 1e-6);

        ProjectEXUtils.handleInventoryAddEmc(inv, 2000000000.0);
        Assert.assertEquals("Inventory EMC must now be 7B", 7000000000.0, ProjectEXUtils.getInventoryEmc(inv), 1e-6);

        ProjectEXUtils.removeInventoryEmc(inv, 3000000000.0);
        Assert.assertEquals("Inventory EMC must now be 4B", 4000000000.0, ProjectEXUtils.getInventoryEmc(inv), 1e-6);
    }
}
