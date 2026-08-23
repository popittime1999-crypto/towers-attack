package com.gimr12.towers_attack.block;

import com.gimr12.towers_attack.item.ModItems;
import com.gimr12.towers_attack.towers_attack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Objects;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(towers_attack.MOD_ID);

    public static final DeferredBlock<Block> BADCRYSTAL_BLOCK = registerBlock("badcrystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 5).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BADCRYSTAL_BAR_BLOCK = registerBlock("badcrystal_bar_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4).sound(SoundType.DEEPSLATE_BRICKS).requiresCorrectToolForDrops()));


    public static final DeferredBlock<Block> POORCRYSTAL_BLOCK = registerBlock("poorcrystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 5).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> POORCRYSTAL_BAR_BLOCK = registerBlock("poorcrystal_bar_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).sound(SoundType.DEEPSLATE_BRICKS).requiresCorrectToolForDrops()));


    public static final DeferredBlock<Block> FINECRYSTAL_BLOCK = registerBlock("finecrystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 5).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> FINECRYSTAL_BAR_BLOCK = registerBlock("finecrystal_bar_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 5).requiresCorrectToolForDrops()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

}
