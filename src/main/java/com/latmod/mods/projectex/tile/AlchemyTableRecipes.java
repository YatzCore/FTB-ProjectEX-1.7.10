package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumMatter;
import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.item.ProjectEXItems;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTableRecipes {
    public static final AlchemyTableRecipes INSTANCE = new AlchemyTableRecipes();

    public static class AlchemyTableRecipe {
        public final ItemStack input;
        public final ItemStack output;
        public final double emcCost;

        public AlchemyTableRecipe(ItemStack input, ItemStack output, double emcCost) {
            this.input = input;
            this.output = output;
            this.emcCost = emcCost;
        }
    }

    private final List<AlchemyTableRecipe> recipes = new ArrayList<AlchemyTableRecipe>();

    public List<AlchemyTableRecipe> getRecipes() {
        return recipes;
    }

    public void add(ItemStack input, ItemStack output) {
        if (input != null && output != null) {
            double inEmc = ProjectEAPI.getEMCProxy().getValue(input);
            double outEmc = ProjectEAPI.getEMCProxy().getValue(output);
            if (inEmc > 0 && outEmc > inEmc) {
                double diff = outEmc - inEmc;
                recipes.add(new AlchemyTableRecipe(ProjectEXUtils.fixOutput(input), ProjectEXUtils.fixOutput(output), diff));
            }
        }
    }

    public void addDefaultRecipes() {
        recipes.clear();
        // Register default matter progression recipes (Magenta through Fading)
        for (int i = 0; i < EnumMatter.VALUES.length - 1; i++) {
            add(new ItemStack(ProjectEXItems.MATTER, 1, i), new ItemStack(ProjectEXItems.MATTER, 1, i + 1));
        }
    }

    public AlchemyTableRecipe findRecipe(ItemStack input) {
        if (input == null) return null;
        for (AlchemyTableRecipe recipe : recipes) {
            if (ItemStack.areItemStacksEqual(recipe.input, input) || 
               (recipe.input.getItem() == input.getItem() && recipe.input.getItemDamage() == input.getItemDamage())) {
                return recipe;
            }
        }
        return null;
    }
}
