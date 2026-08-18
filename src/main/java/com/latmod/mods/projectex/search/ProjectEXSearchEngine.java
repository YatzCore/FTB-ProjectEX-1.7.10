package com.latmod.mods.projectex.search;

import com.latmod.mods.projectex.ProjectEXUtils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.emc.FuelMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectEXSearchEngine {

    public interface IQueryPredicate {
        boolean test(ItemStack stack, EntityPlayer player, double currentEmc);
    }

    private static final Map<Item, String> ITEM_MOD_ID_CACHE = new ConcurrentHashMap<Item, String>();
    private static final Map<Item, String> ITEM_MOD_NAME_CACHE = new ConcurrentHashMap<Item, String>();
    private static final Map<Item, String> ITEM_REG_NAME_CACHE = new ConcurrentHashMap<Item, String>();
    private static final Map<String, String> MOD_ID_TO_NAME_CACHE = new ConcurrentHashMap<String, String>();

    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    public static IQueryPredicate parseQuery(String query) {
        if (query == null) {
            return AlwaysTruePredicate.INSTANCE;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return AlwaysTruePredicate.INSTANCE;
        }

        // Support top-level OR operations: "a | b" or "a OR b"
        if (trimmed.contains("|") || trimmed.contains(" OR ")) {
            String[] orParts;
            if (trimmed.contains("|")) {
                orParts = trimmed.split("\\|");
            } else {
                orParts = trimmed.split(" OR ");
            }
            List<IQueryPredicate> orList = new ArrayList<IQueryPredicate>();
            for (String part : orParts) {
                IQueryPredicate p = parseSingleClause(part.trim());
                if (p != null && !(p instanceof AlwaysFalsePredicate)) {
                    orList.add(p);
                }
            }
            if (orList.isEmpty()) {
                return AlwaysFalsePredicate.INSTANCE;
            }
            if (orList.size() == 1) {
                return orList.get(0);
            }
            return new OrPredicate(orList);
        }

        return parseSingleClause(trimmed);
    }

    private static IQueryPredicate parseSingleClause(String clause) {
        if (clause == null || clause.isEmpty()) {
            return AlwaysTruePredicate.INSTANCE;
        }

        List<IQueryPredicate> andList = new ArrayList<IQueryPredicate>();
        Matcher matcher = QUOTE_PATTERN.matcher(clause);

        while (matcher.find()) {
            String token;
            if (matcher.group(1) != null) {
                token = matcher.group(1); // Quoted token without quotes
            } else {
                token = matcher.group(2); // Regular token
            }

            if (token == null || token.trim().isEmpty()) {
                continue;
            }

            token = token.trim();
            IQueryPredicate pred = parseToken(token);
            if (pred != null) {
                andList.add(pred);
            }
        }

        if (andList.isEmpty()) {
            return AlwaysTruePredicate.INSTANCE;
        }
        if (andList.size() == 1) {
            return andList.get(0);
        }
        return new AndPredicate(andList);
    }

    private static IQueryPredicate parseToken(String token) {
        boolean negate = false;
        if (token.startsWith("!") && token.length() > 1) {
            negate = true;
            token = token.substring(1).trim();
        }

        IQueryPredicate basePred = null;

        if (token.startsWith("@")) {
            // Mod ID or Mod Name filter
            String modQuery = token.substring(1).toLowerCase(Locale.ROOT);
            basePred = new ModFilter(modQuery);
        } else if (token.startsWith("#")) {
            // EMC Range / Budget filter
            String emcQuery = token.substring(1).trim();
            basePred = parseEmcFilter(emcQuery);
        } else if (token.startsWith("$")) {
            // Ore Dictionary tag filter
            String oreQuery = token.substring(1).toLowerCase(Locale.ROOT);
            basePred = new OreDictFilter(oreQuery);
        } else if (token.startsWith("%")) {
            // Tooltip text search
            String tipQuery = token.substring(1).toLowerCase(Locale.ROOT);
            basePred = new TooltipFilter(tipQuery);
        } else if (token.startsWith("*") || token.toLowerCase(Locale.ROOT).startsWith("id:")) {
            // Item registry / unlocalized name
            String idQuery;
            if (token.startsWith("*")) {
                idQuery = token.substring(1).toLowerCase(Locale.ROOT);
            } else {
                idQuery = token.substring(3).toLowerCase(Locale.ROOT);
            }
            basePred = new RegistryIdFilter(idQuery);
        } else if (token.startsWith("^") || token.toLowerCase(Locale.ROOT).startsWith("is:")) {
            // Type / classification filter
            String typeQuery;
            if (token.startsWith("^")) {
                typeQuery = token.substring(1).toLowerCase(Locale.ROOT);
            } else {
                typeQuery = token.substring(3).toLowerCase(Locale.ROOT);
            }
            basePred = parseTypeFilter(typeQuery);
        } else {
            // Default: display name search
            basePred = new DisplayNameFilter(token.toLowerCase(Locale.ROOT));
        }

        if (negate && basePred != null) {
            return new NegatedPredicate(basePred);
        }
        return basePred;
    }

    private static IQueryPredicate parseEmcFilter(String raw) {
        if (raw.isEmpty()) {
            return AlwaysTruePredicate.INSTANCE;
        }

        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.equals("aff") || lower.equals("affordable") || lower.equals("canbuy") || lower.equals("buy")) {
            return new AffordableFilter();
        }
        if (lower.equals("fuel")) {
            return new FuelFilter(true);
        }
        if (lower.equals("matter")) {
            return new FuelFilter(false);
        }

        // Min-Max range: e.g. "1000-50000" or "1k-50k" or "10M-5G"
        if (raw.contains("-") && !raw.startsWith("-")) {
            String[] parts = raw.split("-", 2);
            double min = parseEmcNumber(parts[0]);
            double max = parseEmcNumber(parts[1]);
            return new EmcRangeFilter(min, max);
        }

        // Less than or equal: "<5000", "<=10k"
        if (raw.startsWith("<=") || raw.startsWith("=<")) {
            double max = parseEmcNumber(raw.substring(2));
            return new EmcRangeFilter(0.0, max);
        }
        if (raw.startsWith("<")) {
            double max = parseEmcNumber(raw.substring(1));
            return new EmcRangeFilter(0.0, max);
        }

        // Greater than or equal: ">10000", ">=1M", "500k+"
        if (raw.startsWith(">=") || raw.startsWith("=>")) {
            double min = parseEmcNumber(raw.substring(2));
            return new EmcRangeFilter(min, Double.MAX_VALUE);
        }
        if (raw.startsWith(">")) {
            double min = parseEmcNumber(raw.substring(1));
            return new EmcRangeFilter(min, Double.MAX_VALUE);
        }
        if (raw.endsWith("+")) {
            double min = parseEmcNumber(raw.substring(0, raw.length() - 1));
            return new EmcRangeFilter(min, Double.MAX_VALUE);
        }

        // Exact match: "#8192"
        double exact = parseEmcNumber(raw);
        return new EmcExactFilter(exact);
    }

    private static IQueryPredicate parseTypeFilter(String type) {
        if (type.equals("fuel")) {
            return new FuelFilter(true);
        } else if (type.equals("matter") || type.equals("nonfuel")) {
            return new FuelFilter(false);
        } else if (type.equals("block")) {
            return new BlockFilter();
        } else if (type.equals("tool")) {
            return new ToolFilter();
        } else if (type.equals("armor")) {
            return new ArmorFilter();
        }
        return new DisplayNameFilter(type);
    }

    public static double parseEmcNumber(String str) {
        if (str == null) return 0.0;
        str = str.trim().toLowerCase(Locale.ROOT).replace(",", "");
        if (str.isEmpty()) return 0.0;

        double multiplier = 1.0;
        if (str.endsWith("k")) {
            multiplier = 1_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("m")) {
            multiplier = 1_000_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("b") || str.endsWith("g")) {
            multiplier = 1_000_000_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("t")) {
            multiplier = 1_000_000_000_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("p")) {
            multiplier = 1_000_000_000_000_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("e")) {
            multiplier = 1_000_000_000_000_000_000.0;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("z")) {
            multiplier = 1e21;
            str = str.substring(0, str.length() - 1);
        } else if (str.endsWith("y")) {
            multiplier = 1e24;
            str = str.substring(0, str.length() - 1);
        }

        try {
            return Double.parseDouble(str.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String getModId(Item item) {
        if (item == null) return "minecraft";
        String cached = ITEM_MOD_ID_CACHE.get(item);
        if (cached != null) return cached;

        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
        String modId = uid != null ? uid.modId.toLowerCase(Locale.ROOT) : "minecraft";
        ITEM_MOD_ID_CACHE.put(item, modId);
        return modId;
    }

    public static String getModDisplayName(Item item) {
        if (item == null) return "Minecraft";
        String cached = ITEM_MOD_NAME_CACHE.get(item);
        if (cached != null) return cached;

        String modId = getModId(item);
        String name = MOD_ID_TO_NAME_CACHE.get(modId);
        if (name == null) {
            if ("minecraft".equals(modId)) {
                name = "Minecraft";
            } else {
                ModContainer mc = Loader.instance().getIndexedModList().get(modId);
                name = mc != null ? mc.getName().toLowerCase(Locale.ROOT) : modId;
            }
            MOD_ID_TO_NAME_CACHE.put(modId, name);
        }
        ITEM_MOD_NAME_CACHE.put(item, name);
        return name;
    }

    public static String getRegistryName(Item item) {
        if (item == null) return "";
        String cached = ITEM_REG_NAME_CACHE.get(item);
        if (cached != null) return cached;

        GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
        String name = uid != null ? uid.name.toLowerCase(Locale.ROOT) : "";
        ITEM_REG_NAME_CACHE.put(item, name);
        return name;
    }

    // --- Predicates ---

    public static class AlwaysTruePredicate implements IQueryPredicate {
        public static final AlwaysTruePredicate INSTANCE = new AlwaysTruePredicate();
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            return true;
        }
    }

    public static class AlwaysFalsePredicate implements IQueryPredicate {
        public static final AlwaysFalsePredicate INSTANCE = new AlwaysFalsePredicate();
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            return false;
        }
    }

    public static class NegatedPredicate implements IQueryPredicate {
        private final IQueryPredicate predicate;
        public NegatedPredicate(IQueryPredicate predicate) {
            this.predicate = predicate;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            return !predicate.test(stack, player, currentEmc);
        }
    }

    public static class AndPredicate implements IQueryPredicate {
        private final List<IQueryPredicate> predicates;
        public AndPredicate(List<IQueryPredicate> predicates) {
            this.predicates = predicates;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            for (IQueryPredicate p : predicates) {
                if (!p.test(stack, player, currentEmc)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class OrPredicate implements IQueryPredicate {
        private final List<IQueryPredicate> predicates;
        public OrPredicate(List<IQueryPredicate> predicates) {
            this.predicates = predicates;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            for (IQueryPredicate p : predicates) {
                if (p.test(stack, player, currentEmc)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class DisplayNameFilter implements IQueryPredicate {
        private final String term;
        public DisplayNameFilter(String term) {
            this.term = term;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null || stack.getItem() == null) return false;
            try {
                String name = stack.getDisplayName();
                return name != null && name.toLowerCase(Locale.ROOT).contains(term);
            } catch (Throwable t) {
                return false;
            }
        }
    }

    public static class ModFilter implements IQueryPredicate {
        private final String modQuery;
        public ModFilter(String modQuery) {
            this.modQuery = modQuery;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null || stack.getItem() == null) return false;
            Item item = stack.getItem();
            String modId = getModId(item);
            if (modId.contains(modQuery)) return true;
            String modName = getModDisplayName(item);
            return modName.contains(modQuery);
        }
    }

    public static class EmcRangeFilter implements IQueryPredicate {
        private final double min;
        private final double max;
        public EmcRangeFilter(double min, double max) {
            this.min = min;
            this.max = max;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            double emc = ProjectEXUtils.getEmcValueDouble(stack);
            return emc >= min && emc <= max;
        }
    }

    public static class EmcExactFilter implements IQueryPredicate {
        private final double exact;
        public EmcExactFilter(double exact) {
            this.exact = exact;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            double emc = ProjectEXUtils.getEmcValueDouble(stack);
            return Math.abs(emc - exact) < 0.0001;
        }
    }

    public static class AffordableFilter implements IQueryPredicate {
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            double emc = ProjectEXUtils.getEmcValueDouble(stack);
            return emc > 0.0 && emc <= currentEmc;
        }
    }

    public static class OreDictFilter implements IQueryPredicate {
        private final String oreQuery;
        public OreDictFilter(String oreQuery) {
            this.oreQuery = oreQuery;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null) return false;
            int[] ids = OreDictionary.getOreIDs(stack);
            if (ids == null || ids.length == 0) return false;
            for (int id : ids) {
                String oreName = OreDictionary.getOreName(id);
                if (oreName != null && oreName.toLowerCase(Locale.ROOT).contains(oreQuery)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class TooltipFilter implements IQueryPredicate {
        private final String tipQuery;
        public TooltipFilter(String tipQuery) {
            this.tipQuery = tipQuery;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null) return false;
            try {
                return testTooltip(stack, player);
            } catch (Throwable t) {
                return false;
            }
        }

        @SideOnly(Side.CLIENT)
        private boolean testTooltip(ItemStack stack, EntityPlayer player) {
            EntityPlayer thePlayer = player != null ? player : Minecraft.getMinecraft().thePlayer;
            List tooltip = stack.getTooltip(thePlayer, false);
            if (tooltip == null || tooltip.isEmpty()) return false;
            for (Object line : tooltip) {
                if (line != null && line.toString().toLowerCase(Locale.ROOT).contains(tipQuery)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class RegistryIdFilter implements IQueryPredicate {
        private final String idQuery;
        public RegistryIdFilter(String idQuery) {
            this.idQuery = idQuery;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null || stack.getItem() == null) return false;
            Item item = stack.getItem();
            String regName = getRegistryName(item);
            if (regName.contains(idQuery)) return true;
            String unloc = item.getUnlocalizedName();
            return unloc != null && unloc.toLowerCase(Locale.ROOT).contains(idQuery);
        }
    }

    public static class FuelFilter implements IQueryPredicate {
        private final boolean mustBeFuel;
        public FuelFilter(boolean mustBeFuel) {
            this.mustBeFuel = mustBeFuel;
        }
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null) return false;
            boolean isFuel = FuelMapper.isStackFuel(stack);
            return mustBeFuel == isFuel;
        }
    }

    public static class BlockFilter implements IQueryPredicate {
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            return stack != null && stack.getItem() instanceof ItemBlock;
        }
    }

    public static class ToolFilter implements IQueryPredicate {
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            if (stack == null || stack.getItem() == null) return false;
            Item item = stack.getItem();
            return item instanceof ItemTool || item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemShears || item instanceof ItemHoe;
        }
    }

    public static class ArmorFilter implements IQueryPredicate {
        @Override
        public boolean test(ItemStack stack, EntityPlayer player, double currentEmc) {
            return stack != null && stack.getItem() instanceof ItemArmor;
        }
    }
}
