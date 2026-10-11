package com.gimr12.towers_attack.datagen;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, towers_attack.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE);
//                .add(ModBlocks.BLOCKNAME.get());

        this.tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.PEACH_LOG.get())
                .add(ModBlocks.STRIPPED_PEACH_WOOD.get())
                .add(ModBlocks.STRIPPED_PEACH_LOG.get())
                .add(ModBlocks.PEACH_WOOD.get());
    }
}
