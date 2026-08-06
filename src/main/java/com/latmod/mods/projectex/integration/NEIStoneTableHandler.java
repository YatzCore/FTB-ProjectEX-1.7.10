package com.latmod.mods.projectex.integration;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.latmod.mods.projectex.tile.AlchemyTableRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class NEIStoneTableHandler extends TemplateRecipeHandler {

    public class CachedStoneTableRecipe extends CachedRecipe {
        private final PositionedStack input;
        private final PositionedStack output;
        public final double emcCost;

        public CachedStoneTableRecipe(AlchemyTableRecipes.AlchemyTableRecipe recipe) {
            this.input = new PositionedStack(recipe.input, 44, 18);
            this.output = new PositionedStack(recipe.output, 104, 18);
            this.emcCost = recipe.emcCost;
        }

        @Override
        public PositionedStack getIngredient() {
            return input;
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("tile.projectex.stone_table_dark.name");
    }

    @Override
    public String getGuiTexture() {
        return "projectex:textures/gui/stone_table.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "projectex.stone_table";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("projectex.stone_table")) {
            for (AlchemyTableRecipes.AlchemyTableRecipe recipe : AlchemyTableRecipes.INSTANCE.getRecipes()) {
                arecipes.add(new CachedStoneTableRecipe(recipe));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (AlchemyTableRecipes.AlchemyTableRecipe recipe : AlchemyTableRecipes.INSTANCE.getRecipes()) {
            if (ItemStack.areItemStacksEqual(recipe.output, result) || 
               (recipe.output.getItem() == result.getItem() && recipe.output.getItemDamage() == result.getItemDamage())) {
                arecipes.add(new CachedStoneTableRecipe(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (AlchemyTableRecipes.AlchemyTableRecipe recipe : AlchemyTableRecipes.INSTANCE.getRecipes()) {
            if (ItemStack.areItemStacksEqual(recipe.input, ingredient) || 
               (recipe.input.getItem() == ingredient.getItem() && recipe.input.getItemDamage() == ingredient.getItemDamage())) {
                arecipes.add(new CachedStoneTableRecipe(recipe));
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        CachedStoneTableRecipe r = (CachedStoneTableRecipe) arecipes.get(recipe);
        if (r != null) {
            String text = String.format("%,.0f EMC", r.emcCost);
            int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
            Minecraft.getMinecraft().fontRenderer.drawString(text, 88 - width / 2, 6, 0x404040);
        }
    }
}
