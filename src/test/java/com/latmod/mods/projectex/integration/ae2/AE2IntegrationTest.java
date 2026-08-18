package com.latmod.mods.projectex.integration.ae2;

import appeng.api.config.AccessRestriction;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class AE2IntegrationTest {

    @Test
    public void testEMCInventoryHandlerBasics() {
        EMCInventoryHandler handler = new EMCInventoryHandler();
        UUID testUUID = UUID.randomUUID();
        String testName = "TestPlayer";

        handler.setOwner(testUUID, testName);
        Assert.assertEquals(testUUID, handler.getOwnerUUID());
        Assert.assertEquals(testName, handler.getOwnerName());

        handler.setAccess(AccessRestriction.READ);
        Assert.assertEquals(AccessRestriction.READ, handler.getAccess());

        handler.setAccess(AccessRestriction.WRITE);
        Assert.assertEquals(AccessRestriction.WRITE, handler.getAccess());

        handler.setAccess(AccessRestriction.READ_WRITE);
        Assert.assertEquals(AccessRestriction.READ_WRITE, handler.getAccess());

        handler.setPriority(42);
        Assert.assertEquals(42, handler.getPriority());
    }

    @Test
    public void testProjectEClassPresence() {
        Class<?>[] classes = new Class<?>[] {
            moze_intel.projecte.gameObjs.container.slots.transmutation.SlotConsume.class,
            moze_intel.projecte.gameObjs.container.slots.transmutation.SlotLock.class,
            moze_intel.projecte.gameObjs.container.slots.transmutation.SlotOutput.class,
            moze_intel.projecte.gameObjs.container.TransmutationContainer.class,
            moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory.class,
            moze_intel.projecte.gameObjs.gui.GUITransmutation.class,
            moze_intel.projecte.playerData.TransmutationProps.class,
            moze_intel.projecte.playerData.Transmutation.class
        };
        for (Class<?> c : classes) {
            Assert.assertNotNull("ProjectE class should be loadable: " + c.getName(), c);
        }
    }
}
