package com.latmod.mods.projectex.search;

import moze_intel.projecte.utils.ItemSearchHelper;
import net.minecraft.item.ItemStack;

public class ProjectEXSearchHelper extends ItemSearchHelper {
    private final ProjectEXSearchEngine.IQueryPredicate predicate;

    public static ItemSearchHelper create(String searchString) {
        return new ProjectEXSearchHelper(searchString);
    }

    public ProjectEXSearchHelper(String searchString) {
        super(searchString != null ? searchString : "");
        this.predicate = ProjectEXSearchEngine.parseQuery(this.searchString);
    }

    @Override
    protected boolean doesItemMatchFilter_(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        return predicate.test(stack, null, Double.MAX_VALUE);
    }

    public boolean doesItemMatchFilter(ItemStack stack, double currentEmc) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }
        return predicate.test(stack, null, currentEmc);
    }

    public ProjectEXSearchEngine.IQueryPredicate getPredicate() {
        return predicate;
    }
}
