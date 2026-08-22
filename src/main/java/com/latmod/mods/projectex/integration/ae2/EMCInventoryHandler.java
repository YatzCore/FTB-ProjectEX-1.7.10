package com.latmod.mods.projectex.integration.ae2;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import com.latmod.mods.projectex.ProjectEXUtils;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

/**
 * High performance AE2 storage handler backed by a player's Transmutation Table.
 */
@cpw.mods.fml.common.Optional.Interface(iface = "appeng.api.storage.IMEInventoryHandler", modid = "appliedenergistics2")
public class EMCInventoryHandler implements IMEInventoryHandler<IAEItemStack> {

    private final TileMEEMCLink tile;
    private UUID ownerUUID;
    private String ownerName;
    private AccessRestriction access = AccessRestriction.READ_WRITE;
    private int priority = 0;

    // === Player reference cache ===
    private EntityPlayer cachedPlayer = null;
    private long cachedPlayerTimestamp = 0L;
    private static final long PLAYER_CACHE_MS = 2000L;

    public EMCInventoryHandler() {
        this(null);
    }

    public EMCInventoryHandler(TileMEEMCLink tile) {
        this.tile = tile;
    }

    public void setOwner(UUID uuid, String name) {
        if ((this.ownerUUID == null && uuid != null) || (this.ownerUUID != null && !this.ownerUUID.equals(uuid))) {
            this.cachedPlayer = null;
        }
        this.ownerUUID = uuid;
        this.ownerName = name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setAccess(AccessRestriction access) {
        this.access = access != null ? access : AccessRestriction.READ_WRITE;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public EntityPlayer getPlayer() {
        long now = System.currentTimeMillis();
        if (cachedPlayer != null && (now - cachedPlayerTimestamp < PLAYER_CACHE_MS)) {
            if (!cachedPlayer.isDead) {
                return cachedPlayer;
            }
            cachedPlayer = null;
        }

        EntityPlayer found = resolvePlayer();
        cachedPlayer = found;
        cachedPlayerTimestamp = now;
        return found;
    }

    private EntityPlayer resolvePlayer() {
        if (ownerUUID == null && (ownerName == null || ownerName.isEmpty())) {
            return null;
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return null;
        }

        // 1. Search online players by UUID
        if (ownerUUID != null) {
            for (Object obj : server.getConfigurationManager().playerEntityList) {
                if (obj instanceof EntityPlayer) {
                    EntityPlayer p = (EntityPlayer) obj;
                    if (ownerUUID.equals(p.getUniqueID())) {
                        return p;
                    }
                }
            }
        }

        // 2. Search online players by name
        if (ownerName != null && !ownerName.isEmpty()) {
            EntityPlayer p = server.getConfigurationManager().func_152612_a(ownerName);
            if (p != null) {
                return p;
            }
        }

        // Do NOT use FakePlayer as fallback: FakePlayers do not share IExtendedEntityProperties
        return null;
    }

    private void syncPlayerToClient(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            try {
                Transmutation.sync(player);
            } catch (Throwable ignored) {}
        }
    }

    private int getFilterMode() {
        return tile != null ? tile.getFilterMode() : 0;
    }

    private int getFilterPrecision() {
        return tile != null ? tile.getFilterPrecision() : 0;
    }

    private ItemStack[] getFilterSlots() {
        return tile != null ? tile.getFilterSlots() : null;
    }

    public boolean filterMatches(ItemStack stack) {
        if (stack == null) return true;
        int mode = getFilterMode();
        if (mode == 0) return true;

        ItemStack[] filter = getFilterSlots();
        if (filter == null || filter.length == 0) return true;

        int precision = getFilterPrecision();
        boolean found = false;
        for (ItemStack f : filter) {
            if (f != null && matchesPrecision(f, stack, precision)) {
                found = true;
                break;
            }
        }
        return mode == 1 ? found : !found;
    }

    public static boolean matchesPrecision(ItemStack filterStack, ItemStack targetStack, int precision) {
        if (filterStack == null || targetStack == null) return false;
        if (precision == 1) {
            // Fuzzy: matches Item ignoring Damage and NBT (e.g. damaged weapons/tools/armor)
            return filterStack.getItem() == targetStack.getItem();
        } else if (precision == 2) {
            // OreDict: matches if stacks share any OreDictionary entry, falling back to exact
            if (ProjectEXUtils.areKnowledgeStacksEqual(filterStack, targetStack)) return true;
            int[] fIds = net.minecraftforge.oredict.OreDictionary.getOreIDs(filterStack);
            int[] tIds = net.minecraftforge.oredict.OreDictionary.getOreIDs(targetStack);
            if (fIds != null && tIds != null && fIds.length > 0 && tIds.length > 0) {
                for (int fId : fIds) {
                    for (int tId : tIds) {
                        if (fId == tId) return true;
                    }
                }
            }
            return false;
        } else {
            // Exact (0): matches Item, Damage, and NBT
            return ProjectEXUtils.areKnowledgeStacksEqual(filterStack, targetStack);
        }
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src) {
        if (input == null || access == AccessRestriction.READ || access == AccessRestriction.NO_ACCESS) {
            return input;
        }

        EntityPlayer player = getPlayer();
        if (player == null) return input;

        ItemStack inStack = input.getItemStack();
        if (inStack == null) return input;
        if (!filterMatches(inStack)) return input;

        double itemEmc = ProjectEXUtils.getEmcValueDouble(inStack);
        if (itemEmc <= 0.0) return input;

        if (mode == Actionable.MODULATE) {
            UUID uuid = player.getUniqueID();
            Object lock = uuid != null ? ProjectEXUtils.getPlayerLock(uuid) : new Object();
            synchronized (lock) {
                double totalAdd = itemEmc * (double) input.getStackSize();
                double currentEmc = ProjectEXUtils.getPlayerEmcSafe(player);
                double newEmc = currentEmc + totalAdd;
                if (newEmc < 0.0 || Double.isInfinite(newEmc) || Double.isNaN(newEmc)) {
                    newEmc = Double.MAX_VALUE;
                }
                ProjectEXUtils.syncPlayerEMCAndKnowledge(player, newEmc, inStack.copy());
            }
        }

        return null;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
        if (request == null || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS) {
            return null;
        }

        EntityPlayer player = getPlayer();
        if (player == null) return null;

        ItemStack reqStack = request.getItemStack();
        if (reqStack == null) return null;
        if (!filterMatches(reqStack)) return null;
        if (!Transmutation.hasKnowledgeForStack(reqStack, player)) return null;

        double cost = ProjectEXUtils.getEmcValueDouble(reqStack);
        if (cost <= 0.0) return null;

        UUID uuid = player.getUniqueID();
        Object lock = uuid != null ? ProjectEXUtils.getPlayerLock(uuid) : new Object();

        synchronized (lock) {
            double playerEmc = ProjectEXUtils.getPlayerEmcSafe(player);
            if (playerEmc < cost) return null;

            long affordable = (long) Math.floor(playerEmc / cost);
            long toExtract = Math.min(request.getStackSize(), affordable);
            if (toExtract <= 0) return null;

            if (mode == Actionable.MODULATE) {
                double totalCost = cost * (double) toExtract;
                double newEmc = Math.max(0.0, playerEmc - totalCost);
                ProjectEXUtils.syncPlayerEMCAndKnowledge(player, newEmc, null);
            }

            IAEItemStack result = request.copy();
            result.setStackSize(toExtract);
            return result;
        }
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        if (access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS) {
            return out;
        }

        EntityPlayer player = getPlayer();
        if (player == null) return out;

        double playerEmc = ProjectEXUtils.getPlayerEmcSafe(player);
        if (playerEmc <= 0.0) return out;

        int mode = getFilterMode();
        int precision = getFilterPrecision();

        // 1. Whitelist with Exact mode & configured filter slots
        if (mode == 1 && precision == 0) {
            ItemStack[] filter = getFilterSlots();
            if (filter != null) {
                for (ItemStack f : filter) {
                    if (f == null || f.getItem() == null) continue;
                    if (!Transmutation.hasKnowledgeForStack(f, player)) continue;

                    double cost = ProjectEXUtils.getEmcValueDouble(f);
                    if (cost <= 0.0 || cost > playerEmc) continue;

                    long count = (long) Math.min((double) Integer.MAX_VALUE, Math.floor(playerEmc / cost));
                    if (count > 0) {
                        IAEItemStack aeStack = AEApi.instance().storage().createItemStack(f);
                        if (aeStack != null) {
                            aeStack.setStackSize(count);
                            out.add(aeStack);
                        }
                    }
                }
                return out;
            }
        }

        // 2. All Items, Blacklist mode, or Whitelist with Fuzzy/OreDict mode:
        List<ItemStack> knowledge = Transmutation.getKnowledge(player);
        if (knowledge == null || knowledge.isEmpty()) return out;

        int countAdded = 0;
        for (ItemStack stack : knowledge) {
            if (stack == null || stack.getItem() == null) continue;
            if (!filterMatches(stack)) continue;

            double cost = ProjectEXUtils.getEmcValueDouble(stack);
            if (cost <= 0.0 || cost > playerEmc) continue;

            long count = (long) Math.min((double) Integer.MAX_VALUE, Math.floor(playerEmc / cost));
            if (count > 0) {
                IAEItemStack aeStack = AEApi.instance().storage().createItemStack(stack);
                if (aeStack != null) {
                    aeStack.setStackSize(count);
                    out.add(aeStack);
                    countAdded++;
                    if (countAdded >= 128) break;
                }
            }
        }

        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public AccessRestriction getAccess() {
        return access;
    }

    @Override
    public boolean isPrioritized(IAEItemStack stack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEItemStack stack) {
        if (access == AccessRestriction.READ || access == AccessRestriction.NO_ACCESS || stack == null) {
            return false;
        }
        ItemStack itemStack = stack.getItemStack();
        return itemStack != null && filterMatches(itemStack) && ProjectEXUtils.getEmcValueDouble(itemStack) > 0.0;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }
}
