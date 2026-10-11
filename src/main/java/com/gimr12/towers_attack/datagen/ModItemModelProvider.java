package com.gimr12.towers_attack.datagen;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.item.ModItems;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, towers_attack.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.PEACH.get());
        basicItem(ModItems.GOLDEN_PEACH.get());
        saplingItem(ModBlocks.PEACH_SAPLING);
        basicItem(ModItems.LOWXPSCROLL.get());
        basicItem(ModItems.MIDXPSCROLL.get());
        basicItem(ModItems.HIGHXPSCROLL.get());
        basicItem(ModItems.GODXPSCROLL.get());
        withExistingParent(ModItems.GOBLIN_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));

    }

    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "block/" + item.getId().getPath()));
    }

}
