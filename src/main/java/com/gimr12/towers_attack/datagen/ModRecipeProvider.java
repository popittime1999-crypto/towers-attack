package com.gimr12.towers_attack.datagen;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LOWXPSCROLL.get())
                .pattern("EPE")
                .pattern("PDP")
                .pattern("EPE")
                .define('E', Items.EMERALD)
                .define('D', Items.DIAMOND_BLOCK)
                .define('P', Items.PAPER)
                .unlockedBy("has_diamond", has(Items.DIAMOND_BLOCK)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MIDXPSCROLL.get())
                .pattern("DPD")
                .pattern("PNP")
                .pattern("DPD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .define('P', Items.PAPER)
                .unlockedBy("has_netherite", has(Items.NETHERITE_INGOT)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HIGHXPSCROLL.get())
                .pattern("NPN")
                .pattern("PSP")
                .pattern("NPN")
                .define('N', Items.NETHERITE_INGOT)
                .define('S', Items.NETHER_STAR)
                .define('P', Items.PAPER)
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_PEACH.get())
                .pattern("GGG")
                .pattern("GPG")
                .pattern("GGG")
                .define('G', Items.GOLD_INGOT)
                .define('P', ModItems.PEACH.get())
                .unlockedBy("has_peach", has(ModItems.PEACH)).save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.PEACH_PLANKS.get(), 4)
                .pattern("LL")
                .pattern("LL")
                .define('L', ModBlocks.PEACH_LOG.get())
                .unlockedBy("has_peach_log", has(ModBlocks.PEACH_LOG)).save(recipeOutput);

    }
}
