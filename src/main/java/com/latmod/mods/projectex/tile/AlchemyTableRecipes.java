package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.EnumMatter;
import com.latmod.mods.projectex.ProjectEXUtils;
import com.latmod.mods.projectex.item.ProjectEXItems;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<String, AlchemyTableRecipe> recipeMap = new HashMap<String, AlchemyTableRecipe>();

    private static String getRecipeKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }

    public List<AlchemyTableRecipe> getRecipes() {
        return recipes;
    }

    public void add(ItemStack input, ItemStack output) {
        if (input != null && output != null) {
            double inEmc = ProjectEAPI.getEMCProxy().getValue(input);
            double outEmc = ProjectEAPI.getEMCProxy().getValue(output);
            if (inEmc > 0 && outEmc > inEmc) {
                double diff = outEmc - inEmc;
                AlchemyTableRecipe recipe = new AlchemyTableRecipe(ProjectEXUtils.fixOutput(input), ProjectEXUtils.fixOutput(output), diff);
                recipes.add(recipe);
                recipeMap.put(getRecipeKey(recipe.input), recipe);
            }
        }
    }

    public void addDefaultRecipes() {
        recipes.clear();
        recipeMap.clear();
        for (int i = 0; i < EnumMatter.VALUES.length - 1; i++) {
            add(new ItemStack(ProjectEXItems.MATTER, 1, i), new ItemStack(ProjectEXItems.MATTER, 1, i + 1));
        }
    }

    public AlchemyTableRecipe findRecipe(ItemStack input) {
        if (input == null || input.getItem() == null) return null;
        return recipeMap.get(getRecipeKey(input));
    }
}
