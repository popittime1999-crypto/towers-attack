package com.gimr12.towers_attack.datagen;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, towers_attack.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //blockWithItem(ModBlocks.BLOCKNAME);

        logBlock(((RotatedPillarBlock) ModBlocks.PEACH_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.PEACH_WOOD.get()), blockTexture(ModBlocks.PEACH_LOG.get()), blockTexture(ModBlocks.PEACH_LOG.get()));
        logBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_PEACH_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_PEACH_WOOD.get()), blockTexture(ModBlocks.STRIPPED_PEACH_LOG.get()), blockTexture(ModBlocks.STRIPPED_PEACH_LOG.get()));

        blockItem(ModBlocks.PEACH_LOG);
        blockItem(ModBlocks.STRIPPED_PEACH_LOG);
        blockItem(ModBlocks.PEACH_WOOD);
        blockItem(ModBlocks.STRIPPED_PEACH_WOOD);

        blockWithItem(ModBlocks.PEACH_PLANKS);

        leavesBlock(ModBlocks.PEACH_LEAVES);
        saplingBlock(ModBlocks.PEACH_SAPLING);
    }

    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(),
                models().cross(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    private void leavesBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().singleTexture(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), ResourceLocation.parse("minecraft:block/leaves"),
                        "all", blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("gimrstowerattack:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("gimrstowerattack:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
